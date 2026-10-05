import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Web UI npm audit suppression rules")
class NpmAuditRulesTests {

    private val today = LocalDate.of(2026, 10, 5)

    private val chain =
        """
        {
          "vulnerabilities": {
            "eslint-plugin-boundaries": { "severity": "high", "via": ["@boundaries/elements", "micromatch"] },
            "@boundaries/elements": { "severity": "high", "via": ["micromatch"] },
            "micromatch": { "severity": "high", "via": ["braces"] },
            "braces": { "severity": "high", "via": [
              { "url": "https://github.com/advisories/GHSA-vfj7-8cjw-p6xm", "title": "braces DoS" }
            ] }
          }
        }
        """
            .trimIndent()

    private fun suppression(id: String, expires: String) =
        """{"suppressions": [{"id": "$id", "reason": "dev-only, no fix", "expires": "$expires"}]}"""

    @Test
    @DisplayName("A listed unexpired advisory's whole chain is suppressed and the gate is clean")
    fun listedAdvisoriesChainIsSuppressed() {
        val result = NpmAuditRules.evaluate(chain, suppression("GHSA-vfj7-8cjw-p6xm", "2026-11-01"), today)

        assertThat(result.unsuppressed).isEmpty()
        assertThat(result.violations).isEmpty()
        assertThat(result.applied).hasSize(1)
        assertThat(result.applied.single().id).isEqualTo("GHSA-vfj7-8cjw-p6xm")
        assertThat(result.isClean()).isTrue()
    }

    @Test
    @DisplayName("Without a suppression the high-severity chain is unsuppressed")
    fun unsuppressedChainIsReported() {
        val result = NpmAuditRules.evaluate(chain, """{"suppressions": []}""", today)

        assertThat(result.unsuppressed).contains("braces (high)", "micromatch (high)")
        assertThat(result.violations).isEmpty()
        assertThat(result.isClean()).isFalse()
    }

    @Test
    @DisplayName("An expired suppression is treated as absent and the finding fails")
    fun expiredSuppressionIsTreatedAsAbsent() {
        val result = NpmAuditRules.evaluate(chain, suppression("GHSA-vfj7-8cjw-p6xm", "2026-10-04"), today)

        assertThat(result.unsuppressed).isNotEmpty()
        assertThat(result.applied).isEmpty()
    }

    @Test
    @DisplayName("A listed id matching no finding is a violation naming the entry")
    fun staleSuppressionIdIsAViolation() {
        val result = NpmAuditRules.evaluate(chain, suppression("GHSA-0000-0000-0000", "2026-11-01"), today)

        assertThat(result.violations).anySatisfy {
            assertThat(it).contains("GHSA-0000-0000-0000").contains("matches no finding")
        }
        assertThat(result.isClean()).isFalse()
    }

    @Test
    @DisplayName("A suppression entry missing a field is a violation naming its position")
    fun malformedSuppressionEntryIsAViolation() {
        val result =
            NpmAuditRules.evaluate(chain, """{"suppressions": [{"id": "GHSA-x", "expires": "2026-11-01"}]}""", today)

        assertThat(result.violations).anySatisfy { assertThat(it).contains("entry #1") }
        assertThat(result.isClean()).isFalse()
    }

    @Test
    @DisplayName("An unparseable audit report is a violation, not a silent pass")
    fun unparseableReportIsAViolation() {
        val result = NpmAuditRules.evaluate("{ not json", """{"suppressions": []}""", today)

        assertThat(result.violations).isNotEmpty()
        assertThat(result.isClean()).isFalse()
    }

    @Test
    @DisplayName("An empty report with no suppressions is clean")
    fun emptyFindingsAreClean() {
        val result = NpmAuditRules.evaluate("""{"vulnerabilities": {}}""", """{"suppressions": []}""", today)

        assertThat(result.isClean()).isTrue()
    }
}
