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

    private fun panels(vararg panels: String): File =
        dashboard("panels.json", """{"title": "Axon Showcase", "panels": [${panels.joinToString(",")}]}""")

    @Test
    @DisplayName("A repeated panel id is a violation, a panel nested in a collapsed row included")
    fun repeatedPanelIdIsAViolation() {
        val file =
            panels(
                """{"id": 84, "type": "stat", "title": "Projector State",
                   "gridPos": {"x": 0, "y": 0, "w": 6, "h": 7}, "targets": [{"expr": "up"}]}""",
                """{"id": 26, "type": "row", "title": "Projection Service",
                   "gridPos": {"x": 0, "y": 7, "w": 24, "h": 1},
                   "panels": [{"id": 84, "type": "timeseries", "title": "Event Age",
                               "gridPos": {"x": 0, "y": 8, "w": 24, "h": 7}, "targets": [{"expr": "up"}]}]}""",
            )

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("panels.json").contains("repeats panel id 84")
        }
    }

    @Test
    @DisplayName("Two panels on the same grid positions are a violation naming both")
    fun overlappingPanelsAreAViolation() {
        val file =
            panels(
                """{"id": 1, "type": "timeseries", "title": "Throughput",
                   "gridPos": {"x": 0, "y": 0, "w": 12, "h": 7}, "targets": [{"expr": "up"}]}""",
                """{"id": 2, "type": "timeseries", "title": "Latency",
                   "gridPos": {"x": 0, "y": 0, "w": 12, "h": 7}, "targets": [{"expr": "up"}]}""",
            )

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("'Throughput'").contains("'Latency'").contains("overlapping")
        }
    }

    @Test
    @DisplayName("A row panel that meets the panel below it is a violation")
    fun aRowOverlappingAPanelIsAViolation() {
        val file =
            panels(
                """{"id": 1, "type": "row", "title": "Projection Service",
                   "gridPos": {"x": 0, "y": 0, "w": 24, "h": 1}}""",
                """{"id": 2, "type": "timeseries", "title": "Throughput",
                   "gridPos": {"x": 0, "y": 0, "w": 12, "h": 7}, "targets": [{"expr": "up"}]}""",
            )

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("'Projection Service'").contains("'Throughput'")
        }
    }

    @Test
    @DisplayName("A panel without a grid position is a violation")
    fun aPanelWithoutGridPositionIsAViolation() {
        val file = panels("""{"id": 1, "type": "timeseries", "title": "Throughput", "targets": [{"expr": "up"}]}""")

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("'Throughput'").contains("no grid position")
        }
    }

    @Test
    @DisplayName("A target whose expression is blank is a violation")
    fun aBlankExpressionIsAViolation() {
        val file =
            panels(
                """{"id": 1, "type": "timeseries", "title": "Throughput",
                   "gridPos": {"x": 0, "y": 0, "w": 12, "h": 7}, "targets": [{"expr": "  "}]}""",
            )

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("'Throughput'").contains("no query expression")
        }
    }

    @Test
    @DisplayName("A target that omits the expression is a violation")
    fun aMissingExpressionIsAViolation() {
        val file =
            panels(
                """{"id": 1, "type": "timeseries", "title": "Throughput",
                   "gridPos": {"x": 0, "y": 0, "w": 12, "h": 7}, "targets": [{"legendFormat": "rate"}]}""",
            )

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("'Throughput'").contains("no query expression")
        }
    }

    @Test
    @DisplayName("A panel whose second target carries no expression is a violation")
    fun aMixedTargetPanelIsAViolation() {
        val file =
            panels(
                """{"id": 1, "type": "timeseries", "title": "Throughput",
                   "gridPos": {"x": 0, "y": 0, "w": 12, "h": 7},
                   "targets": [{"expr": "up"}, {"expr": ""}]}""",
            )

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("'Throughput'").contains("no query expression")
        }
    }

    @Test
    @DisplayName("A dashboard whose panels tile the grid, with a text panel and a row, has no violation")
    fun aTilingDashboardHasNoViolation() {
        val file =
            panels(
                """{"id": 1, "type": "row", "title": "Projection Service",
                   "gridPos": {"x": 0, "y": 0, "w": 24, "h": 1}}""",
                """{"id": 2, "type": "text", "title": "How to read this",
                   "gridPos": {"x": 0, "y": 1, "w": 12, "h": 7}}""",
                """{"id": 3, "type": "timeseries", "title": "Throughput",
                   "gridPos": {"x": 12, "y": 1, "w": 12, "h": 7}, "targets": [{"expr": "up"}]}""",
                """{"id": 4, "type": "timeseries", "title": "Latency",
                   "gridPos": {"x": 0, "y": 8, "w": 24, "h": 7}, "targets": [{"expr": "up"}, {"expr": "up"}]}""",
            )

        assertThat(DashboardJsonRules.violations(listOf(file))).isEmpty()
    }

    @Test
    @DisplayName("A row panel that declares a blank target expression is a violation")
    fun aRowWithABlankTargetIsAViolation() {
        val file =
            panels(
                """{"id": 1, "type": "row", "title": "Projection Service",
                   "gridPos": {"x": 0, "y": 0, "w": 24, "h": 1}, "targets": [{"expr": ""}]}""",
            )

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("'Projection Service'").contains("no query expression")
        }
    }

    @Test
    @DisplayName("A grid position whose fields are not numbers is a violation")
    fun aNonNumericGridPositionIsAViolation() {
        val file =
            panels(
                """{"id": 1, "type": "timeseries", "title": "Throughput",
                   "gridPos": {"x": "0", "y": 0, "w": 12, "h": 7}, "targets": [{"expr": "up"}]}""",
            )

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(1).allSatisfy {
            assertThat(it).contains("'Throughput'").contains("no grid position")
        }
    }

    @Test
    @DisplayName("An untitled panel is named by its id")
    fun anUntitledPanelIsNamedByItsId() {
        val file = panels("""{"id": 7, "type": "timeseries", "targets": [{"expr": ""}]}""")

        assertThat(DashboardJsonRules.violations(listOf(file))).hasSize(2).anySatisfy {
            assertThat(it).contains("untitled panel 7").contains("no query expression")
        }
    }
}
