import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import java.io.File

/**
 * The rules the bundled Grafana dashboard gate applies to the chart's dashboards directory.
 *
 * The logic is pure so it is testable without Gradle: [VerifyDashboardJsonTask] resolves the files, calls these rules,
 * and turns each failure into a Gradle exception. The dashboard files are emitted by the chart as opaque strings, so a
 * malformed one is invisible to `helm lint` and must be parsed here.
 */
internal object DashboardJsonRules {

    /**
     * Strict by default: `readTree` alone accepts a trailing or second document (`{"a":1}x`) and returns a missing node
     * for empty input, so the mapper is configured to reject trailing content explicitly.
     */
    private val mapper = JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build()

    /**
     * A failure message for each of [files] that is not valid JSON, naming the file. An empty [files] set is itself a
     * failure, named once, so a renamed dashboards directory cannot disable the gate silently.
     */
    fun violations(files: List<File>): List<String> {
        if (files.isEmpty()) {
            return listOf("no bundled Grafana dashboard was found")
        }
        return files.mapNotNull(::violation)
    }

    /** The reason [file] is not a valid dashboard, or null when it is. */
    private fun violation(file: File): String? {
        if (file.readText().isBlank()) {
            return "dashboard '$file' is empty"
        }
        return runCatching { mapper.readTree(file) }
            .exceptionOrNull()
            ?.let { "dashboard '$file' is not valid JSON: $it" }
    }
}
