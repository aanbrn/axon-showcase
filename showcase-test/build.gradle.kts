plugins {
    id("java-library-conventions")
}

project.description = "Showcase Test Utilities"

dependencies {
    implementation(platform(project(":platform")))

    api(libs.assertj.core)
    api(libs.commons.lang3)
    api(libs.junit.jupiter.api)

    implementation(libs.axon.extensions.kafka)
}
