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
}
