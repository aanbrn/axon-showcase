import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("Bundled Grafana dashboard JSON rules")
class DashboardJsonRulesTests {

    @TempDir lateinit var dir: File

    private fun dashboard(name: String, content: String): File =
        File(dir, name).apply {
            parentFile.mkdirs()
            writeText(content)
        }

    @Test
    @DisplayName("A valid dashboard has no violation")
    fun validDashboardHasNoViolation() {
        val file = dashboard("good.json", """{"title": "Axon Showcase", "panels": []}""")

        assertThat(DashboardJsonRules.violations(listOf(file))).isEmpty()
    }

    @Test
    @DisplayName("A truncated dashboard is reported naming the file")
    fun truncatedDashboardIsReported() {
        val file = dashboard("bad.json", """{"title": "Axon Showcase", "panels": []""")

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("bad.json")
        }
    }

    @Test
    @DisplayName("Every malformed dashboard is reported, not only the first")
    fun everyMalformedDashboardIsReported() {
        val first = dashboard("first.json", "{ not json")
        val second = dashboard("second.json", "[1, 2,")

        assertThat(DashboardJsonRules.violations(listOf(first, second)))
            .hasSize(2)
            .anySatisfy { assertThat(it).contains("first.json") }
            .anySatisfy { assertThat(it).contains("second.json") }
    }

    @Test
    @DisplayName("An empty dashboard file is a violation (an empty file parses to a missing node, not an error)")
    fun emptyDashboardIsAViolation() {
        val file = dashboard("empty.json", "")

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("empty.json")
        }
    }

    @Test
    @DisplayName("Trailing content after the JSON value is a violation")
    fun trailingContentIsAViolation() {
        val file = dashboard("trailing.json", """{"title": "Axon Showcase"}x""")

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("trailing.json")
        }
    }

    @Test
    @DisplayName("No dashboard files is itself a violation")
    fun noDashboardsIsAViolation() {
        assertThat(DashboardJsonRules.violations(emptyList())).hasSize(1)
    }
}
