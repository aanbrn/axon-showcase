import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Documented-pin rules")
class DocumentedPinRulesTests {

    private val pins =
        mapOf(
            "paketo-nginx" to "1.3.0",
            "paketo-procfile" to "5.15.3",
            "paketo-builder-jammy-base" to "0.4.659",
        )

    @Test
    @DisplayName("A stated pin at its catalog version is parsed and clean")
    fun matchingPin() {
        val docs = mapOf("AGENTS.md" to "(`paketo-buildpacks/nginx@1.3.0`, `builder-jammy-base:0.4.659`)")

        assertThat(DocumentedPinRules.statedPins(docs).map { it.catalogKey to it.version })
            .containsExactly("paketo-nginx" to "1.3.0", "paketo-builder-jammy-base" to "0.4.659")
        assertThat(DocumentedPinRules.findings(docs, pins)).isEmpty()
    }

    @Test
    @DisplayName("A stated pin at another version names the site and both versions")
    fun mismatchedPin() {
        val docs = mapOf("AGENTS.md" to "`paketo-buildpacks/nginx@1.2.0`")

        assertThat(DocumentedPinRules.findings(docs, pins))
            .containsExactly(
                "AGENTS.md: states 'paketo-buildpacks/nginx@1.2.0', but the catalog pins 'paketo-nginx' at '1.3.0'.",
            )
    }

    @Test
    @DisplayName("A pin at a sentence end is captured without the trailing period")
    fun sentenceEndPin() {
        val docs = mapOf("README.md" to "the buildpack is pinned at paketo-buildpacks/nginx@1.3.0. See the catalog.")

        assertThat(DocumentedPinRules.findings(docs, pins)).isEmpty()
    }

    @Test
    @DisplayName("The catalog-key form a historical mention uses is not matched")
    fun catalogKeyFormNotMatched() {
        val docs = mapOf("AGENTS.md" to "held `paketo-nginx` back at `1.2.0`")

        assertThat(DocumentedPinRules.findings(docs, pins)).isEmpty()
    }

    @Test
    @DisplayName("A name and version separated by prose is not matched")
    fun proseSplitNotMatched() {
        val docs = mapOf("AGENTS.md" to "a `paketo-buildpacks/procfile` split from its `5.15.0`")

        assertThat(DocumentedPinRules.findings(docs, pins)).isEmpty()
    }

    @Test
    @DisplayName("A placeholder is not a numeric version and is not matched")
    fun placeholderNotMatched() {
        val docs =
            mapOf(
                "AGENTS.md" to "`builder-jammy-base:<version>`",
                "README.md" to "`paketo-buildpacks/newthing@<version>`",
            )

        assertThat(DocumentedPinRules.findings(docs, pins)).isEmpty()
    }

    @Test
    @DisplayName("A version that does not start with a digit is not matched")
    fun wordVersionNotMatched() {
        val docs = mapOf("AGENTS.md" to "`paketo-buildpacks/nginx@latest`")

        assertThat(DocumentedPinRules.findings(docs, pins)).isEmpty()
    }

    @Test
    @DisplayName("A hyphenated buildpack name and a pre-release version parse")
    fun hyphenatedNameAndPreRelease() {
        val docs = mapOf("AGENTS.md" to "`paketo-buildpacks/ca-certificates@3.0.0-rc1`")

        assertThat(DocumentedPinRules.statedPins(docs).map { it.catalogKey to it.version })
            .containsExactly("paketo-ca-certificates" to "3.0.0-rc1")
    }

    @Test
    @DisplayName("A digit-bearing buildpack name parses")
    fun digitNameParses() {
        val docs = mapOf("AGENTS.md" to "`paketo-buildpacks/ii2@1.0.0`")

        assertThat(DocumentedPinRules.statedPins(docs).map { it.catalogKey }).containsExactly("paketo-ii2")
    }

    @Test
    @DisplayName("A buildpack form with no name is not matched")
    fun emptyNameNotMatched() {
        val docs = mapOf("AGENTS.md" to "`paketo-buildpacks/@1.0.0`")

        assertThat(DocumentedPinRules.findings(docs, pins)).isEmpty()
    }

    @Test
    @DisplayName("A stated pin with no catalog key fails, naming the key")
    fun unownedPin() {
        val docs = mapOf("AGENTS.md" to "`paketo-buildpacks/newthing@1.0.0`")

        assertThat(DocumentedPinRules.findings(docs, pins))
            .containsExactly(
                "AGENTS.md: states 'paketo-buildpacks/newthing@1.0.0', whose key 'paketo-newthing' the catalog does " +
                    "not pin.",
            )
    }

    @Test
    @DisplayName("A doc stating no pin is clean")
    fun noPins() {
        assertThat(DocumentedPinRules.findings(mapOf("README.md" to "no pins here"), pins)).isEmpty()
    }
}
