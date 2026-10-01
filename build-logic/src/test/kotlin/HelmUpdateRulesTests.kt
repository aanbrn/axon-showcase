import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Helm chart update rules")
class HelmUpdateRulesTests {

    @Test
    @DisplayName("The same-major filter keeps candidates in the pinned major only")
    fun sameMajorFilter() {
        val versions = listOf("92.0.0", "91.4.1", "91.4.0", "90.9.9")

        assertThat(HelmUpdateRules.sameMajor(versions, "91.4.0")).isEqualTo("91.4.1")
        assertThat(HelmUpdateRules.sameMajor(versions, "90.0.0")).isEqualTo("90.9.9")
        assertThat(HelmUpdateRules.sameMajor(versions, "89.0.0")).isNull()
    }

    @Test
    @DisplayName("A suppression list skips comments and blank lines and keeps each chart name whole")
    fun suppressionListSkipsCommentsAndBlanks() {
        val lines =
            listOf(
                "# Helm chart names whose MAJOR version updates are suppressed",
                "",
                "bitnami-postgresql",
                "  bitnami-kafka  ",
                "bitnami-opensearch",
            )

        assertThat(HelmUpdateRules.disabledEntries(lines))
            .containsExactlyInAnyOrder("bitnami-postgresql", "bitnami-kafka", "bitnami-opensearch")
    }

    @Test
    @DisplayName("A comment containing an equals sign is skipped whole, not split into an entry")
    fun commentWithEqualsSignIsSkipped() {
        val lines =
            listOf(
                "# see release=latest for how the tag is chosen",
                "bitnami-kafka",
            )

        assertThat(HelmUpdateRules.disabledEntries(lines)).containsExactly("bitnami-kafka")
    }

    @Test
    @DisplayName("An entry carrying a value is read up to the equals sign")
    fun entryValueIsDiscarded() {
        val lines = listOf("bitnami-postgresql=17.6")

        assertThat(HelmUpdateRules.disabledEntries(lines)).containsExactly("bitnami-postgresql")
    }

    @Test
    @DisplayName("An empty or comment-only list yields no entries")
    fun emptyListYieldsNoEntries() {
        assertThat(HelmUpdateRules.disabledEntries(emptyList())).isEmpty()
        assertThat(HelmUpdateRules.disabledEntries(listOf("# just a comment", "   "))).isEmpty()
    }
}
