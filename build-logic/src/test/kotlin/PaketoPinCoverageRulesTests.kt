import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Paketo-pin coverage rules")
class PaketoPinCoverageRulesTests {

    @Test
    @DisplayName("A fully covered catalog reports nothing")
    fun fullyCovered() {
        val catalog = PaketoPinCoverageRules.catalogKeys(listOf("paketo.nginx", "paketo.procfile", "other.alias"))
        val checks = setOf("paketo-nginx", "paketo-procfile")

        assertThat(catalog).containsExactlyInAnyOrder("paketo-nginx", "paketo-procfile")
        assertThat(PaketoPinCoverageRules.uncovered(catalog, checks)).isEmpty()
        assertThat(PaketoPinCoverageRules.unknown(catalog, checks)).isEmpty()
    }

    @Test
    @DisplayName("A catalog pin without a check is named as uncovered")
    fun uncoveredPin() {
        val catalog = PaketoPinCoverageRules.catalogKeys(listOf("paketo.nginx", "paketo.health.checker"))

        assertThat(PaketoPinCoverageRules.uncovered(catalog, setOf("paketo-nginx")))
            .containsExactly("paketo-health-checker")
    }

    @Test
    @DisplayName("A check naming a pin the catalog does not hold is named as unknown")
    fun unknownEntry() {
        val catalog = PaketoPinCoverageRules.catalogKeys(listOf("paketo.nginx"))

        assertThat(PaketoPinCoverageRules.unknown(catalog, setOf("paketo-nginx", "paketo-retired")))
            .containsExactly("paketo-retired")
    }

    @Test
    @DisplayName("Both directions are reported together, each sorted by key")
    fun bothDirectionsSorted() {
        val catalog =
            PaketoPinCoverageRules.catalogKeys(
                listOf("paketo.nginx", "paketo.procfile", "paketo.health.checker", "paketo.builder.jammy.base"),
            )
        val checks = setOf("paketo-retired", "paketo-apocryphal", "paketo-nginx")

        assertThat(PaketoPinCoverageRules.uncovered(catalog, checks))
            .containsExactly("paketo-builder-jammy-base", "paketo-health-checker", "paketo-procfile")
        assertThat(PaketoPinCoverageRules.unknown(catalog, checks))
            .containsExactly("paketo-apocryphal", "paketo-retired")
    }

    @Test
    @DisplayName("A dotted alias is normalized to the key form")
    fun dottedAlias() {
        assertThat(PaketoPinCoverageRules.catalogKeys(listOf("paketo.builder.noble.java.tiny")))
            .containsExactly("paketo-builder-noble-java-tiny")
    }

    @Test
    @DisplayName("A non-Paketo alias is not a catalog pin")
    fun nonPaketoAlias() {
        assertThat(PaketoPinCoverageRules.catalogKeys(listOf("spring.boot", "helm", "bitnami.common"))).isEmpty()
    }

    @Test
    @DisplayName("An empty catalog reports every check as unknown")
    fun emptyCatalog() {
        val catalog = PaketoPinCoverageRules.catalogKeys(emptyList())

        assertThat(catalog).isEmpty()
        assertThat(PaketoPinCoverageRules.unknown(catalog, setOf("paketo-nginx"))).containsExactly("paketo-nginx")
    }
}
