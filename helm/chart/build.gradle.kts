import com.citi.gradle.plugins.helm.dsl.HelmChart

plugins {
    id("helm-conventions")
}

helm {
    charts.named<HelmChart>("main") {
        chartName = rootProject.name
    }
}
