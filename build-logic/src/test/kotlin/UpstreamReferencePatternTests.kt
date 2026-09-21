import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Upstream reference patterns")
class UpstreamReferencePatternTests {

    private fun extract(text: String): List<String> =
        UpstreamReferencePattern.REFERENCE.findAll(text).map { it.value }.toList()

    @Test
    @DisplayName("A well-formed owner/repo#NNN reference is extracted")
    fun wellFormedReferenceIsExtracted() {
        assertThat(extract("see anomalyco/opencode#50247 for the trigger")).containsExactly("anomalyco/opencode#50247")
    }

    @Test
    @DisplayName("A repository name carrying dashes is extracted whole")
    fun dottedAndDashedRepositoryIsExtracted() {
        assertThat(extract("ben-manes/gradle-versions-plugin#755 and build-extensions-oss/gradle-helm-plugin#145"))
            .containsExactly("ben-manes/gradle-versions-plugin#755", "build-extensions-oss/gradle-helm-plugin#145")
    }

    @Test
    @DisplayName("A bare owner/repo without an issue number is not extracted")
    fun bareRepositoryIsNotExtracted() {
        assertThat(extract("the ben-manes/gradle-versions-plugin task and Fission-AI/OpenSpec project")).isEmpty()
    }

    @Test
    @DisplayName("An owner/repo with a non-numeric issue part is not extracted")
    fun nonNumericIssuePartIsNotExtracted() {
        assertThat(extract("anomalyco/opencode#abc")).isEmpty()
    }

    @Test
    @DisplayName("Id-shaped tokens that are not upstream references are not extracted")
    fun nonReferenceIdsAreNotExtracted() {
        val line = "ADR-0002, CVE-2021-44228, UTF-8, ISO-8601, and KAFKA-18281 are not references"

        assertThat(extract(line)).isEmpty()
    }

    @Test
    @DisplayName("Every reference on a line is extracted")
    fun multipleReferencesOnOneLineAreExtracted() {
        assertThat(extract("both Fission-AI/OpenSpec#1891 and Fission-AI/OpenSpec#1892 apply"))
            .containsExactly("Fission-AI/OpenSpec#1891", "Fission-AI/OpenSpec#1892")
    }
}
