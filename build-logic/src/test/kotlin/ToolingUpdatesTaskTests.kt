import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Tooling provider patterns")
class ToolingUpdatesTaskTests {

    @Test
    @DisplayName("The release tag pattern tolerates the whitespace the APIs vary in")
    fun releaseTagPatternToleratesWhitespace() {
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
