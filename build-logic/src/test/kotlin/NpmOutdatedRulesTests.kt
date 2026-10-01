import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Web UI npm outdated report rules")
class NpmOutdatedRulesTests {

    private val header = "Package      Current  Wanted  Latest  Location                  Depended by"

    @Test
    @DisplayName("A suppressed package's major-only update is dropped from the report")
    fun suppressedMajorIsDropped() {
        val lines =
            listOf(
                header,
                "typescript   6.0.3   6.0.3   7.0.2  node_modules/typescript  showcase-web-ui",
                "vite         8.3.1   8.3.2   8.3.2  node_modules/vite        showcase-web-ui",
            )

        val rendered = NpmOutdatedRules.filterReport(lines, setOf("typescript"))

        assertThat(rendered).contains("vite")
        assertThat(rendered).doesNotContain("typescript")
    }

    @Test
    @DisplayName("A suppressed package's same-major update is kept")
    fun suppressedSameMajorIsKept() {
        val lines =
            listOf(
                header,
                "typescript   6.0.3   6.0.4   6.0.4  node_modules/typescript  showcase-web-ui",
            )

        assertThat(NpmOutdatedRules.filterReport(lines, setOf("typescript"))).contains("typescript")
    }

    @Test
    @DisplayName("An unlisted package's major update is kept")
    fun unlistedMajorIsKept() {
        val lines =
            listOf(
                header,
                "vite   8.3.1   8.3.1   9.0.0  node_modules/vite  showcase-web-ui",
            )

        assertThat(NpmOutdatedRules.filterReport(lines, setOf("typescript"))).contains("vite")
    }

    @Test
    @DisplayName("A row whose version columns cannot be read is kept")
    fun unreadableRowIsKept() {
        val lines =
            listOf(
                header,
                "typescript   MISSING   6.0.3   LINKED  node_modules/typescript  showcase-web-ui",
            )

        assertThat(NpmOutdatedRules.filterReport(lines, setOf("typescript"))).contains("typescript")
    }

    @Test
    @DisplayName("A header-only report renders just the header")
    fun headerOnlyRendersHeader() {
        assertThat(NpmOutdatedRules.filterReport(listOf(header), setOf("typescript"))).isEqualTo(header)
    }

    @Test
    @DisplayName("An empty input renders empty")
    fun emptyInputRendersEmpty() {
        assertThat(NpmOutdatedRules.filterReport(emptyList(), setOf("typescript"))).isEmpty()
    }

    @Test
    @DisplayName("A suppression entry matches its package exactly, not a name it prefixes")
    fun suppressionMatchesExactly() {
        val lines =
            listOf(
                header,
                "typescript-eslint   8.71.0   8.71.1   8.71.1  node_modules/typescript-eslint  showcase-web-ui",
            )

        assertThat(NpmOutdatedRules.filterReport(lines, setOf("typescript"))).contains("typescript-eslint")
    }

    @Test
    @DisplayName("The suppression list skips comments and blanks and keeps each package whole")
    fun suppressionListParses() {
        val lines =
            listOf(
                "# packages whose major updates are suppressed",
                "",
                "typescript",
                "  eslint  ",
            )

        assertThat(NpmOutdatedRules.suppressedEntries(lines)).containsExactlyInAnyOrder("typescript", "eslint")
    }
}
