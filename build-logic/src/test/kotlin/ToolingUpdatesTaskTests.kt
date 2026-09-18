import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Tooling version ordering")
class ToolingUpdatesTaskTests {

    @Test
    @DisplayName("A longer version whose shared segments are equal is the newer one")
    fun longerVersionIsNewer() {
        assertThat(ToolingVersions.isNewer("1.2.0", "1.2")).isTrue()
        assertThat(ToolingVersions.isNewer("1.2", "1.2.0")).isFalse()
    }

    @Test
    @DisplayName("An equal version is not newer, including a zero-padded one")
    fun equalVersionIsNotNewer() {
        assertThat(ToolingVersions.isNewer("1.2.0", "1.2.0")).isFalse()
        assertThat(ToolingVersions.isNewer("1.2", "1.2.0")).isFalse()
    }

    @Test
    @DisplayName("A higher numeric segment is newer, in any position")
    fun higherSegmentIsNewer() {
        assertThat(ToolingVersions.isNewer("1.3", "1.2.9")).isTrue()
        assertThat(ToolingVersions.isNewer("2.0", "1.99")).isTrue()
        assertThat(ToolingVersions.isNewer("1.2.9", "1.3")).isFalse()
    }

    @Test
    @DisplayName("A leading v is ignored on either side")
    fun leadingVIgnored() {
        assertThat(ToolingVersions.isNewer("v1.1307.3", "v1.1307.2")).isTrue()
        assertThat(ToolingVersions.isNewer("1.1307.3", "v1.1307.2")).isTrue()
        assertThat(ToolingVersions.isNewer("v1.1307.2", "1.1307.3")).isFalse()
        assertThat(ToolingVersions.isNewer("v1.2.0", "1.2.0")).isFalse()
    }

    @Test
    @DisplayName("The provider JSON patterns tolerate the whitespace the APIs vary in")
    fun jsonPatternsTolerateWhitespace() {
        val pretty = "{\n  \"tag_name\": \"v1.2.3\",\n  \"name\": \"helm\"\n}"
        val compact = "{\"tag_name\":\"v1.2.3\",\"name\":\"helm\"}"

        assertThat(ToolingJson.RELEASE_TAG.find(pretty)?.groupValues?.get(1)).isEqualTo("v1.2.3")
        assertThat(ToolingJson.RELEASE_TAG.find(compact)?.groupValues?.get(1)).isEqualTo("v1.2.3")
    }

    @Test
    @DisplayName("The npm dist-tag pattern reads latest rather than a neighbouring tag")
    fun npmPatternReadsLatest() {
        val body = "{\"dist-tags\":{\"next\":\"0.3.0\",\"latest\":\"1.13.1\"}}"

        assertThat(ToolingJson.NPM_LATEST.find(body)?.groupValues?.get(1)).isEqualTo("1.13.1")
    }
}
