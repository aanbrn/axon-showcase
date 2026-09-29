import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("AGENTS.md install command rules")
class InstallCommandRulesTests {

    @Test
    @DisplayName("The chart and version are read from a helm install command")
    fun chartAndVersionAreRead() {
        val lines =
            listOf(
                "helm install kps prometheus-community/kube-prometheus-stack --version 91.8.1 \\",
                "  --namespace monitoring --create-namespace --wait",
            )

        assertThat(InstallCommandRules.installCommands(lines))
            .containsExactly("prometheus-community/kube-prometheus-stack" to "91.8.1")
    }

    @Test
    @DisplayName("A command without a version (the local chart) is skipped")
    fun versionlessCommandIsSkipped() {
        val lines = listOf("helm install axon-showcase ./helm/chart --namespace axon-showcase --wait")

        assertThat(InstallCommandRules.installCommands(lines)).isEmpty()
    }

    @Test
    @DisplayName("A non-install line is ignored")
    fun nonInstallLineIsIgnored() {
        val lines = listOf("# Deploy to local cluster (must be ordered)", "kubectl get pods")

        assertThat(InstallCommandRules.installCommands(lines)).isEmpty()
    }

    @Test
    @DisplayName("A documented version that matches its catalog pin has no mismatch")
    fun matchingVersionHasNoMismatch() {
        assertThat(InstallCommandRules.mismatchReason("bitnami/kafka", "31.5.0", "31.5.0")).isNull()
    }

    @Test
    @DisplayName("A documented version that differs from its catalog pin is reported")
    fun mismatchedVersionIsReported() {
        assertThat(InstallCommandRules.mismatchReason("bitnami/kafka", "31.4.0", "31.5.0"))
            .contains("bitnami/kafka")
            .contains("31.4.0")
            .contains("31.5.0")
    }

    @Test
    @DisplayName("A chart the catalog does not pin is reported")
    fun chartWithoutAPinIsReported() {
        assertThat(InstallCommandRules.mismatchReason("example/unknown", "1.0.0", null))
            .contains("example/unknown")
            .contains("no version")
    }
}
