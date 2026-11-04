import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import java.io.File

/**
 * The rules the bundled Grafana dashboard gate applies to the chart's dashboards directory.
 *
 * The logic is pure so it is testable without Gradle: [VerifyDashboardJsonTask] resolves the files, calls these rules,
 * and turns each failure into a Gradle exception. The dashboard files are emitted by the chart as opaque strings, so a
 * malformed one is invisible to `helm lint` and must be parsed here.
 *
 * Beyond the document's shape, the rules cover the panel defects a hand re-layout produces and a live Grafana would
 * otherwise show: a repeated panel id, two panels on the same grid positions, a target whose query was emptied, and a
 * panel that queries without saying what its number means.
 */
internal object DashboardJsonRules {

    /**
     * Strict by default: `readTree` alone accepts a trailing or second document (`{"a":1}x`) and returns a missing node
     * for empty input, so the mapper is configured to reject trailing content explicitly.
     */
    private val mapper = JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build()

    /**
     * A failure message for each of [files] that is not a valid dashboard, naming the file. An empty [files] set is
     * itself a failure, named once, so a renamed dashboards directory cannot disable the gate silently.
     */
    fun violations(files: List<File>): List<String> {
        if (files.isEmpty()) {
            return listOf("no bundled Grafana dashboard was found")
        }
        return files.flatMap(::violationsFor)
    }

    /** The reasons [file] is not a valid dashboard; empty when it is. */
    private fun violationsFor(file: File): List<String> {
        if (file.readText().isBlank()) {
            return listOf("dashboard '$file' is empty")
        }
        val tree =
            try {
                mapper.readTree(file)
            } catch (error: Exception) {
                return listOf("dashboard '$file' is not valid JSON: $error")
            }
        return panelViolations(file, tree)
    }

    /** The panel-level failures of [tree], each naming [file] and the offending panel. */
    private fun panelViolations(file: File, tree: JsonNode): List<String> {
        val panels = tree.get("panels")?.takeIf { it.isArray }?.filter(JsonNode::isObject) ?: emptyList()
        return repeatedIds(file, panels) +
            unplacedOrOverlapping(file, panels) +
            emptyExpressions(file, panels) +
            undescribed(file, panels)
    }

    /** A failure for each panel id that appears more than once, at any depth, so a collapsed row's child counts. */
    private fun repeatedIds(file: File, panels: List<JsonNode>): List<String> {
        val seen = mutableMapOf<Int, String>()
        val found = mutableListOf<String>()
        fun visit(panel: JsonNode) {
            idOf(panel)?.let { id ->
                val title = titleOf(panel)
                val previous = seen.putIfAbsent(id, title)
                if (previous != null) {
                    found += "dashboard '$file' repeats panel id $id ('$previous' and '$title')"
                }
            }
            panel.get("panels")?.takeIf { it.isArray }?.filter(JsonNode::isObject)?.forEach(::visit)
        }
        panels.forEach(::visit)
        return found
    }

    /**
     * A failure for a top-level panel with no readable grid position and for each pair of panels whose rectangles
     * overlap. Row panels take part, because a row occupies grid space and a forgotten row shift leaves one behind.
     */
    private fun unplacedOrOverlapping(file: File, panels: List<JsonNode>): List<String> {
        val found = mutableListOf<String>()
        val placed = mutableListOf<Pair<Rect, String>>()
        for (panel in panels) {
            val title = titleOf(panel)
            val rect = panel.get("gridPos")?.takeIf { it.isObject }?.let(::rectOf)
            if (rect == null) {
                found += "dashboard '$file' gives panel '$title' no grid position"
            } else {
                placed += rect to title
            }
        }
        for (index in placed.indices) {
            for (other in index + 1 until placed.size) {
                if (placed[index].first.overlaps(placed[other].first)) {
                    found +=
                        "dashboard '$file' places panels '${placed[index].second}' and " +
                            "'${placed[other].second}' on overlapping grid positions"
                }
            }
        }
        return found
    }

    /**
     * A failure for each top-level panel target whose query expression is blank. A panel that declares no target is
     * left alone: a text panel has none, and a data panel added without one renders "No data" in front of its author.
     */
    private fun emptyExpressions(file: File, panels: List<JsonNode>): List<String> {
        val found = mutableListOf<String>()
        for (panel in panels) {
            val targets = panel.get("targets")?.takeIf { it.isArray }?.filter(JsonNode::isObject) ?: continue
            if (targets.any { target -> target.get("expr")?.asText().orEmpty().isBlank() }) {
                found += "dashboard '$file' gives panel '${titleOf(panel)}' a target with no query expression"
            }
        }
        return found
    }

    /**
     * A failure for each panel that declares a target and no readable description, at any depth. A row renders no query
     * and a target-less panel is legitimate, so both are exempt; the rule asserts presence, not quality.
     */
    private fun undescribed(file: File, panels: List<JsonNode>): List<String> {
        val found = mutableListOf<String>()
        fun visit(panel: JsonNode) {
            val targets = panel.get("targets")?.takeIf { it.isArray }?.filter(JsonNode::isObject) ?: emptyList()
            val described = panel.get("description")?.takeIf { it.isTextual }?.asText()?.isNotBlank() == true
            if (panel.get("type")?.asText() != "row" && targets.isNotEmpty() && !described) {
                found += "dashboard '$file' gives panel '${titleOf(panel)}' no description"
            }
            panel.get("panels")?.takeIf { it.isArray }?.filter(JsonNode::isObject)?.forEach(::visit)
        }
        panels.forEach(::visit)
        return found
    }

    /** The panel's id, or null when it declares none this gate can compare. */
    private fun idOf(panel: JsonNode): Int? = panel.get("id")?.takeIf { it.isNumber }?.asInt()

    /** The panel's title; an untitled panel is named by its id. */
    private fun titleOf(panel: JsonNode): String =
        panel.get("title")?.asText()?.takeIf(String::isNotBlank) ?: "untitled panel ${idOf(panel) ?: "?"}"

    /** The panel's grid rectangle, or null when any of its four fields is missing or not a number. */
    private fun rectOf(gridPos: JsonNode): Rect? {
        val x = gridPos.get("x")?.takeIf { it.isNumber }?.asInt() ?: return null
        val y = gridPos.get("y")?.takeIf { it.isNumber }?.asInt() ?: return null
        val w = gridPos.get("w")?.takeIf { it.isNumber }?.asInt() ?: return null
        val h = gridPos.get("h")?.takeIf { it.isNumber }?.asInt() ?: return null
        return Rect(x, y, w, h)
    }

    /** A panel's grid rectangle; two rectangles overlap when each covers part of the other. */
    private data class Rect(val x: Int, val y: Int, val w: Int, val h: Int) {
        /** Whether this rectangle and [other] share any grid cell. */
        fun overlaps(other: Rect): Boolean =
            x < other.x + other.w && other.x < x + w && y < other.y + other.h && other.y < y + h
    }
}
