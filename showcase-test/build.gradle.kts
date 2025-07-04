plugins {
    id("java-library-conventions")
}

project.description = "Showcase Test Utilities"

dependencies {
    implementation(platform(project(":platform")))

    implementation(libs.axon.extensions.kafka)
    implementation(libs.assertj.core)
    implementation(libs.commons.lang3)
    implementation(libs.junit.jupiter.api)
    implementation(libs.reactor.core)
}
