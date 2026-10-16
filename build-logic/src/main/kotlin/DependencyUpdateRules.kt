/**
 * The rules `dependencyUpdates` applies to decide whether a candidate version is rejected.
 *
 * The suppression files under `config/dependency-updates/` are line lists rather than Java properties: an entry may be
 * a `group:module` coordinate, whose colon `Properties` would treat as a key/value separator (collapsing an exact entry
 * to a group prefix), so the lines are parsed here on their first `=` instead.
 *
 * The version catalog's `coordinate -> version` pairs are read here for the same reason: the report treats the
 * catalog's declared version as the project's version authority, so a row whose candidate it is not older than reports
 * nothing to update, whatever "current" the plugin derived from a constraint floor.
 */
internal object DependencyUpdateRules {

    /** A catalog library entry's `group`, `name`, and the `[versions]` key it references. */
    private val LIBRARY_ENTRY = Regex("""= \{ group = "([^"]+)", name = "([^"]+)", version.ref = "([^"]+)""")

    /** The same entry with its alias, for a reader that selects a library by the alias a build script consumes. */
    private val LIBRARY_ALIAS_ENTRY =
        Regex("""^([A-Za-z0-9._-]+) = \{ group = "([^"]+)", name = "([^"]+)", version.ref = "([^"]+)"""")

    /** A `[versions]` entry's key and quoted value. */
    private val VERSION_ENTRY = Regex("""^([A-Za-z0-9._-]+)\s*=\s*"([^"]+)"$""")

    /** The catalog's `[versions]` values in [lines], keyed by entry name. */
    private fun declaredVersions(lines: List<String>): Map<String, String> =
        lines
            .asSequence()
            .map { it.trim() }
            .dropWhile { it != "[versions]" }
            .drop(1)
            .takeWhile { !it.startsWith("[") }
            .mapNotNull { VERSION_ENTRY.find(it) }
            .associate { it.groupValues[1] to it.groupValues[2] }

    /**
     * The catalog's `coordinate -> version` pairs in [lines], from the same `version.ref` entries the report scopes
     * ownership by: each `group`/`name`/`version.ref` triple becomes `group:name` paired with the `[versions]` value
     * its reference names. An entry whose referenced version is absent is skipped, so a broken reference cannot report
     * a version the catalog does not declare.
     */
    fun catalogVersions(lines: List<String>): Map<String, String> {
        val versions = declaredVersions(lines)
        return lines
            .mapNotNull { LIBRARY_ENTRY.find(it) }
            .mapNotNull {
                versions[it.groupValues[3]]?.let { version -> "${it.groupValues[1]}:${it.groupValues[2]}" to version }
            }
            .toMap()
    }

    /**
     * The catalog's `alias -> entry` libraries in [lines], for a reader that selects a library by the alias a build
     * script consumes rather than by its coordinate. An entry whose referenced version is absent is skipped, as in
     * [catalogVersions].
     */
    fun catalogLibraries(lines: List<String>): Map<String, CatalogLibrary> {
        val versions = declaredVersions(lines)
        return lines
            .mapNotNull { LIBRARY_ALIAS_ENTRY.find(it) }
            .mapNotNull {
                versions[it.groupValues[4]]?.let { version ->
                    it.groupValues[1] to CatalogLibrary(it.groupValues[2], it.groupValues[3], version)
                }
            }
            .toMap()
    }

    /**
     * The suppression entries in [lines]: blank lines and `#` comments are skipped, and an optional `=value` is
     * dropped.
     */
    fun disabledEntries(lines: List<String>): Set<String> = lines.mapNotNull { entryLine(it)?.first }.toSet()

    /** The held coordinates in [lines] mapped to their version line; entries without a value are ignored. */
    fun holdBackEntries(lines: List<String>): Map<String, String> =
        lines.mapNotNull { entryLine(it) }.filter { it.second.isNotEmpty() }.toMap()

    /** The `entry to value` a content line declares, with the entry read as the text before the first `=`. */
    private fun entryLine(line: String): Pair<String, String>? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null
        }
        val separator = trimmed.indexOf('=')
        val entry = (if (separator < 0) trimmed else trimmed.substring(0, separator)).trim()
        val value = if (separator < 0) "" else trimmed.substring(separator + 1).trim()
        return if (entry.isEmpty()) null else entry to value
    }

    /** The markers that make a version a stable release whatever else it carries. */
    private val STABLE_KEYWORDS = listOf("RELEASE", "FINAL", "GA")

    /** The shape of a stable numeric release. */
    private val STABLE_VERSION = Regex("^[0-9,.v-]+(-r)?$")

    /** Whether [version] is not a stable release (a snapshot, milestone, RC, or a non-numeric shape). */
    fun isNonStable(version: String): Boolean {
        val stableKeyword = STABLE_KEYWORDS.any { version.uppercase().contains(it) }
        return (stableKeyword || STABLE_VERSION.matches(version)).not()
    }

    /** Whether [version] is calendar-versioned (its leading segment is a 4-digit year). */
    fun isCalendarVersioned(version: String): Boolean = version.takeWhile { it.isDigit() }.length == 4

    /** Whether [candidateVersion] is a major bump from [currentVersion]; a calendar coordinate compares its train. */
    fun isMajorBump(currentVersion: String, candidateVersion: String): Boolean =
        if (isCalendarVersioned(currentVersion)) {
            versionTrain(currentVersion) != versionTrain(candidateVersion)
        } else {
            (leadingInteger(candidateVersion) ?: 0) > (leadingInteger(currentVersion) ?: 0)
        }

    /** The first two segments of [version] (the release train). */
    fun versionTrain(version: String): String = version.split('.').take(2).joinToString(".")

    /** The leading integer of [version], or null when it declares none. */
    fun leadingInteger(version: String): Int? = version.takeWhile { it.isDigit() }.toIntOrNull()

    /** Whether [entry] (an exact `group:module` or a group prefix) covers the [group]:[name] coordinate. */
    fun matchesDisabled(entry: String, group: String, name: String): Boolean =
        if (entry.contains(":")) {
            "$group:$name" == entry
        } else {
            group == entry || group.startsWith("$entry.")
        }

    /**
     * Whether [candidateVersion] is held back by [heldLine]: it shares the held line's leading integer and sits on a
     * newer `major.minor` train. A patch within the held line, and any major jump, are not held back.
     */
    fun isHeldBack(candidateVersion: String, heldLine: String): Boolean {
        val heldMajor = leadingInteger(heldLine) ?: return false
        return leadingInteger(candidateVersion) == heldMajor &&
            Versions.isNewer(versionTrain(candidateVersion), versionTrain(heldLine))
    }

    /**
     * Whether `dependencyUpdates` rejects the candidate: a non-stable version without a stable current release, a
     * coordinate the catalog does not own, a candidate no newer than the version the catalog declares while the
     * plugin's own "current" disagrees with that version (the signature of a current read from a constraint floor —
     * nothing to update, and rejecting an up-to-date coordinate's own version would move it out of the report's
     * up-to-date section), a major-blocked entry, or a held-back coordinate.
     */
    fun shouldReject(
        group: String,
        module: String,
        candidateVersion: String,
        currentVersion: String,
        catalogVersions: Map<String, String>,
        majorDisabled: Set<String>,
        holdBack: Map<String, String>,
    ): Boolean {
        val coordinate = "$group:$module"
        val notOwned = coordinate !in catalogVersions && candidateVersion != currentVersion
        val declaredVersion = catalogVersions[coordinate]
        val staleCurrentVersion =
            declaredVersion != null &&
                currentVersion != declaredVersion &&
                !Versions.isNewer(candidateVersion, declaredVersion)
        val blockedMajor =
            majorDisabled.any { matchesDisabled(it, group, module) } && isMajorBump(currentVersion, candidateVersion)
        val heldBack = holdBack[coordinate]?.let { isHeldBack(candidateVersion, it) } ?: false
        return (isNonStable(candidateVersion) && !isNonStable(currentVersion)) ||
            notOwned ||
            staleCurrentVersion ||
            blockedMajor ||
            heldBack
    }
}

/** A catalog library's coordinate and declared version, as the alias it is declared under names them. */
internal data class CatalogLibrary(val group: String, val module: String, val version: String)
