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

val mainChart = helm.charts.getByName("main")

// The chart's only external dependency is Bitnami's `common` chart, so its packaging path registers and refreshes only
// that repository's index; the root project's release-install path keeps every repository, and the chart publication
// runs this same packaging. The fetch retries each network step a bounded number of times, which the plugin's own
// tasks cannot do.
val fetchChartDependencies = tasks.register<FetchChartDependenciesTask>("fetchMainChartDependencies") {
    description = "Registers the chart's repository and fetches its external dependencies, retrying each step"
    repos.set(mapOf("bitnami" to helm.repositories.getByName("bitnami").url.get().toString()))
    chartDir.set(mainChart.outputDir)
    dependencyDescriptorFile.set(mainChart.outputDir.map { it.file("Chart.yaml") })
    lockFile.set(mainChart.outputDir.map { it.file("Chart.lock") })
    subchartsDir.set(mainChart.outputDir.map { it.dir("charts") })
    dependsOn("helmCollectMainChartSources")
}

tasks.named("helmAddBitnamiRepository") { enabled = false }
tasks.named("helmAddGrafanaRepository") { enabled = false }
tasks.named("helmAddPrometheusCommunityRepository") { enabled = false }
tasks.named("helmAddIngressNginxRepository") { enabled = false }
tasks.named("helmAddRepositories") { enabled = false }
tasks.named("helmUpdateRepositories") { enabled = false }

// The plugin's dependency task stays as a disabled bridge so the package task, both lints, the chart artifact and the
// release install keep their single dependency on it; a skipped task's dependencies still run.
tasks.named("helmUpdateMainChartDependencies") {
    enabled = false
    dependsOn(fetchChartDependencies)
}
