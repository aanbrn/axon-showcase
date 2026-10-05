import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import java.time.LocalDate

/**
 * The rules the web UI `npmAudit` gate applies to an `npm audit --json` report and the suppression list in
 * `showcase-web-ui/npm-audit-ignores.json`.
 *
 * `npm audit` fails on any high-severity finding, fixable or not, and offers no ignore flag; the Snyk scan's `.snyk`
 * policy has no npm equivalent. These rules therefore compute the verdict: a listed advisory with no available fix is
 * suppressed until its `expires` date, and the unsuppressed high-severity remainder fails. The logic is pure so it is
 * testable without Gradle: the task reads the two files, calls these rules, and turns each violation into a Gradle
 * exception.
 *
 * Only the leaf package of a flagged chain carries the advisory object in its `via`; the packages that depend on it
 * name it by string, so a finding's advisories are collected by walking `via` downward until an advisory object is
 * reached. A finding is suppressed only when every advisory it reaches that way is listed and unexpired.
 */
internal object NpmAuditRules {

    private const val HIGH = "high"
    private const val CRITICAL = "critical"
    private const val URL_MARKER = "/"

    /**
     * Strict parsing: `readTree` accepts trailing or a second document and returns a missing node for empty input, so
     * trailing tokens are rejected and blank input is treated as a violation rather than a clean report.
     */
    private val mapper = JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build()

    /** A suppression entry: an advisory id, why it is accepted, and when the suppression lapses. */
    data class Suppression(val id: String, val reason: String, val expires: LocalDate)

    /**
     * The gate's verdict for one report: the high-severity findings that remain unsuppressed, the suppressions that
     * applied (for logging), and any structural violations (a bad suppression entry, a listed id matching nothing, or
     * an unreadable report). Violations are fail-closed and fail the gate even when [unsuppressed] is empty.
     */
    data class Result(
        val unsuppressed: List<String>,
        val applied: List<Suppression>,
        val violations: List<String>,
    ) {
        /** Whether the gate passes: nothing unsuppressed and no structural violation. */
        fun isClean(): Boolean = unsuppressed.isEmpty() && violations.isEmpty()
    }

    /**
     * Evaluates [reportJson] against [suppressionsJson] as of [today]. A blank or unparseable input yields a violation,
     * never a silent pass — a report that cannot be read is a failure, not an empty finding set.
     */
    fun evaluate(reportJson: String, suppressionsJson: String, today: LocalDate): Result {
        val suppressions = parseSuppressions(suppressionsJson)
        val violations = suppressions.violations.toMutableList()
        val listed = suppressions.entries

        val report = readJson(reportJson)
        if (report == null) {
            violations += "the npm audit report is blank or not valid JSON"
            return Result(emptyList(), emptyList(), violations)
        }

        val vulnerabilities = report.path("vulnerabilities")
        val advisoriesByPackage =
            vulnerabilities.fieldNames().asSequence().associateWith { pkg ->
                advisoryIds(vulnerabilities.path(pkg), vulnerabilities, emptySet())
            }
        listed.forEach { entry ->
            if (advisoriesByPackage.values.none { entry.id in it }) {
                violations += "suppression entry '${entry.id}' matches no finding in the audit report"
            }
        }

        val applied = mutableListOf<Suppression>()
        val unsuppressed = mutableListOf<String>()
        vulnerabilities.properties().forEach { (pkg, node) ->
            if (node.path("severity").asText() !in setOf(HIGH, CRITICAL)) {
                return@forEach
            }
            val advisories = advisoriesByPackage.getValue(pkg)
            val suppressing = listed.filter { it.id in advisories && !it.expires.isBefore(today) }
            val covered =
                advisories.isNotEmpty() &&
                    advisories.all { id ->
                        listed.any { it.id == id && !it.expires.isBefore(today) }
                    }
            if (covered) {
                applied += suppressing
            } else {
                unsuppressed += "$pkg (${node.path("severity").asText()})"
            }
        }
        return Result(unsuppressed.distinct().sorted(), applied.distinct(), violations)
    }

    /** The parsed suppression list, plus a violation for each entry that is malformed or expired. */
    private data class ParsedSuppressions(val entries: List<Suppression>, val violations: List<String>)

    private fun parseSuppressions(json: String): ParsedSuppressions {
        val root = readJson(json)
        if (root == null) {
            return ParsedSuppressions(emptyList(), listOf("the suppression list is blank or not valid JSON"))
        }
        val array = root.path("suppressions")
        if (!array.isArray) {
            return ParsedSuppressions(emptyList(), listOf("the suppression list has no 'suppressions' array"))
        }
        val entries = mutableListOf<Suppression>()
        val violations = mutableListOf<String>()
        array.forEachIndexed { index, node ->
            val id = node.path("id").asText().takeIf { it.isNotBlank() }
            val reason = node.path("reason").asText().takeIf { it.isNotBlank() }
            val expires =
                node
                    .path("expires")
                    .asText()
                    .takeIf { it.isNotBlank() }
                    ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            if (id == null || reason == null || expires == null) {
                violations += "suppression entry #${index + 1} is missing a valid 'id', 'reason', or 'expires'"
            } else {
                entries += Suppression(id, reason, expires)
            }
        }
        return ParsedSuppressions(entries, violations)
    }

    /** The advisory ids [node] reaches, walking its `via` package names downward to the advisory objects. */
    private fun advisoryIds(node: JsonNode, all: JsonNode, seen: Set<String>): Set<String> {
        val ids = mutableSetOf<String>()
        node.path("via").forEach { via ->
            if (via.isTextual) {
                val name = via.asText()
                if (name !in seen) {
                    ids += advisoryIds(all.path(name), all, seen + name)
                }
            } else {
                via.path("url").asText().substringAfterLast(URL_MARKER).takeIf { it.isNotBlank() }?.let(ids::add)
            }
        }
        return ids
    }

    /** The parsed JSON of [text], or null when it is blank or malformed. */
    private fun readJson(text: String): JsonNode? =
        text.takeIf { it.isNotBlank() }?.let { runCatching { mapper.readTree(it) }.getOrNull() }
}
