import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Infra image version rules")
class InfraImageVersionRulesTests {

    @Test
    @DisplayName("The top-level image tag is read from the image block")
    fun topLevelImageTagIsRead() {
        val lines =
            listOf(
                "replicaCount: 1",
                "image:",
                "  repository: bitnami/postgresql",
                "  tag: 17.6.0",
                "resources:",
            )

        assertThat(InfraImageVersionRules.topLevelImageTag(lines)).isEqualTo("17.6.0")
    }

    @Test
    @DisplayName("Values without a top-level image block declare no tag")
    fun noImageBlockMeansNoTag() {
        assertThat(InfraImageVersionRules.topLevelImageTag(listOf("replicaCount: 1", "resources:"))).isNull()
    }

    @Test
    @DisplayName("An image key that is not at the top level is ignored")
    fun nestedImageKeyIsIgnored() {
        val lines = listOf("resources:", "  image:", "    tag: 9.9.9")

        assertThat(InfraImageVersionRules.topLevelImageTag(lines)).isNull()
    }

    @Test
    @DisplayName("A tag outside the image block is not the image tag")
    fun tagOutsideTheBlockIsIgnored() {
        val underAnotherKey = listOf("image:", "  repository: x", "database:", "  tag: 1.2.3")
        val unindented = listOf("image:", "  repository: x", "tag: 1.2.3")

        assertThat(InfraImageVersionRules.topLevelImageTag(underAnotherKey)).isNull()
        assertThat(InfraImageVersionRules.topLevelImageTag(unindented)).isNull()
    }

    @Test
    @DisplayName("The chart app version is truncated to the official tag's segment count")
    fun truncationFollowsTheTagGranularity() {
        assertThat(InfraImageVersionRules.truncateToSegments("17.6.0", 2)).isEqualTo("17.6")
        assertThat(InfraImageVersionRules.truncateToSegments("3.9.0", 3)).isEqualTo("3.9.0")
        assertThat(InfraImageVersionRules.truncateToSegments("17.6.0", 1)).isEqualTo("17")
    }

    @Test
    @DisplayName("A bare-major official tag is rejected as a floating reference")
    fun bareMajorIsFloating() {
        assertThat(InfraImageVersionRules.floatingReferenceReason(check(imageTag = "17")))
            .contains("floating reference")
            .contains("'17'")
        assertThat(InfraImageVersionRules.floatingReferenceReason(check(imageTag = "17.6"))).isNull()
    }

    @Test
    @DisplayName("A two-segment tag matches its chart at minor granularity, a full one exactly")
    fun mismatchFollowsTheTagGranularity() {
        assertThat(InfraImageVersionRules.mismatchReason(check(imageTag = "17.6"), "17.6.0")).isNull()
        assertThat(InfraImageVersionRules.mismatchReason(check(imageTag = "3.9.0"), "3.9.0")).isNull()
        assertThat(InfraImageVersionRules.mismatchReason(check(imageTag = "18.1"), "17.6.0")).isNotNull()
        assertThat(InfraImageVersionRules.mismatchReason(check(imageTag = "3.9.0"), "3.9.1"))
            .contains("3.9.0")
            .contains("3.9.1")
    }

    private fun check(imageTag: String) =
        InfraImageVersionCheck("postgres", "bitnami/postgresql", "16.7.27", imageTag, emptyList())
}
