import io.github.build.extensions.oss.gradle.plugins.helm.command.tasks.HelmUpdateRepositories
import io.github.build.extensions.oss.gradle.plugins.helm.dsl.HelmChart

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

tasks.named("helmUpdateMainChartDependencies") {
    setOnlyIf { true }
}

// The chart's only external dependency is Bitnami's `common` chart, so its packaging path registers and refreshes only
// that repository's index; the root project's release-install path keeps every repository, and the chart publication
// runs this same packaging.
tasks.named("helmAddGrafanaRepository") { enabled = false }
tasks.named("helmAddPrometheusCommunityRepository") { enabled = false }
tasks.named("helmAddIngressNginxRepository") { enabled = false }
tasks.named<HelmUpdateRepositories>("helmUpdateRepositories") {
    repositoryNames.set(listOf("bitnami"))
    extraArgs.addAll("bitnami")
}
