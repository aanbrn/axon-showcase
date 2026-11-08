import io.github.build.extensions.oss.gradle.plugins.helm.command.tasks.HelmAddRepository
import io.github.build.extensions.oss.gradle.plugins.helm.command.tasks.HelmUpdateDependencies
import io.github.build.extensions.oss.gradle.plugins.helm.command.tasks.HelmUpdateRepositories
import io.github.build.extensions.oss.gradle.plugins.helm.dsl.HelmChart
import java.time.Duration

plugins {
    id("helm-conventions")
}

helm {
    charts.named<HelmChart>("main") {
        chartName = rootProject.name

        lint {
            strict = true
            withSubcharts = true

            configurations {
                register("full") {
                    valueFiles.from("src/test/helm/helm-lint-full.yaml")
                }
                register("minimal") {
                    valueFiles.from("src/test/helm/helm-lint-minimal.yaml")
                }
            }
        }
    }
}

// The chart's only external dependency is Bitnami's `common` chart, so its packaging path registers and refreshes only
// that repository's index; `extraArgs` scopes the executed `helm repo update`, which `repositoryNames` alone does not
// do. The root project's release-install path keeps every declared repository, and the chart publication runs this same
// packaging.
tasks.named<HelmUpdateRepositories>("helmUpdateRepositories") {
    attempts.set(3)
    retryDelay.set(Duration.ofSeconds(2))
    repositoryCacheTtl.set(Duration.ZERO)
    repositoryNames.set(listOf("bitnami"))
    extraArgs.addAll("bitnami")
}

tasks.withType<HelmAddRepository>().configureEach {
    attempts.set(3)
    retryDelay.set(Duration.ofSeconds(2))
}

tasks.named<HelmUpdateDependencies>("helmUpdateMainChartDependencies") {
    attempts.set(3)
    retryDelay.set(Duration.ofSeconds(2))
}

tasks.named("helmAddGrafanaRepository") { enabled = false }
tasks.named("helmAddPrometheusCommunityRepository") { enabled = false }
tasks.named("helmAddIngressNginxRepository") { enabled = false }
