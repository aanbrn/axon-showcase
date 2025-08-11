import com.citi.gradle.plugins.helm.dsl.HelmChart

plugins {
    `kotlin-dsl`
    id("helm-conventions")
}

dependencies {
    implementation(platform(project(":platform")))
}

helm {
    charts.named<HelmChart>("main") {
        chartName = rootProject.name
    }
}
