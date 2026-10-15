import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Prettier-alignment rules")
class PrettierAlignmentRulesTests {

    private val catalog =
        """
        [versions]
        java = "21"
        prettier = "3.9.10"
        node = "24.20.0"
        """
            .trimIndent()

    private val lockfile = """{"packages":{"node_modules/prettier":{"version":"3.9.10"}}}"""

    @Test
    @DisplayName("Two versions that agree write no finding")
    fun aligned_writesNoFinding() {
        assertThat(PrettierAlignmentRules.findings(catalog, lockfile)).isEmpty()
    }

    @Test
    @DisplayName("A catalog pin the web module does not resolve fails, naming both versions")
    fun drift_failsNamingBothVersions() {
        val findings = PrettierAlignmentRules.findings(catalog, lockfile.replace("3.9.10", "3.9.6"))

        assertThat(findings).singleElement().asString().contains("3.9.10").contains("3.9.6")
    }

    @Test
    @DisplayName("A catalog that records no Prettier version fails")
    fun noCatalogEntry_fails() {
        val findings = PrettierAlignmentRules.findings(catalog.replace("""prettier = "3.9.10"""", ""), lockfile)

        assertThat(findings).singleElement().asString().contains("records no 'prettier' version")
    }

    @Test
    @DisplayName("A lookalike key is not read as the pin")
    fun lookalikeKey_isNotThePin() {
        val lookalike = catalog.replace("""prettier = "3.9.10"""", """prettier-plugin = "9.9.9"""")

        assertThat(PrettierAlignmentRules.catalogVersion(lookalike)).isNull()
        assertThat(PrettierAlignmentRules.findings(lookalike, lockfile))
            .singleElement()
            .asString()
            .contains("records no 'prettier' version")
    }

    @Test
    @DisplayName("A key that merely ends in the pin's name is not read as the pin")
    fun suffixedKey_isNotThePin() {
        val suffixed = catalog.replace("""prettier = "3.9.10"""", """my-prettier = "9.9.9"""")

        assertThat(PrettierAlignmentRules.catalogVersion(suffixed)).isNull()
    }

    @Test
    @DisplayName("A lockfile that resolves no Prettier version fails")
    fun noResolvedVersion_fails() {
        val findings =
            PrettierAlignmentRules.findings(catalog, """{"packages":{"node_modules/other":{"version":"1.0.0"}}}""")

        assertThat(findings).singleElement().asString().contains("resolves no Prettier version")
    }

    @Test
    @DisplayName("A lockfile that is not readable JSON fails rather than reading as no version")
    fun unreadableLockfile_fails() {
        val findings = PrettierAlignmentRules.findings(catalog, """{"packages":{}}x""")

        assertThat(findings).singleElement().asString().contains("not readable as JSON")
    }
}
