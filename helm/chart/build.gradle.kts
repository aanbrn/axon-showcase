import com.citi.gradle.plugins.helm.dsl.HelmChart

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
