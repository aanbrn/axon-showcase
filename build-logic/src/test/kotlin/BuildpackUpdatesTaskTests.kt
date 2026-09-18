import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Buildpack tag parsing and selection")
class BuildpackUpdatesTaskTests {

    /**
     * A verbatim slice of a live `paketobuildpacks/procfile` tags response around its `5.15.0` tag, so the pattern is
     * pinned against the provider's own serialization rather than a hand-written string.
     */
    private val realVersionedTag =
        "\"last_updater_username\":\"paketobuildpacksadmin\",\"name\":\"5.15.0\",\"repository\":"

    /** The same response's floating-tag entry, which declares no version and must never be reported. */
    private val realFloatingTag = "\"name\":\"latest\""

    @Test
    @DisplayName("The tag pattern reads a versioned name from a real tags-API response")
    fun tagPatternReadsRealBody() {
        assertThat(BuildpackJson.TAG_NAME.findAll(realVersionedTag).map { it.groupValues[1] }.toList())
            .containsExactly("5.15.0")
    }

    @Test
    @DisplayName("The pattern ignores a floating tag and tolerates a space after the colon")
    fun patternIgnoresFloatingTag() {
        assertThat(BuildpackJson.TAG_NAME.find(realFloatingTag)).isNull()
        assertThat(BuildpackJson.TAG_NAME.find("{\"name\": \"5.15.1\"}")?.groupValues?.get(1)).isEqualTo("5.15.1")
    }
}
