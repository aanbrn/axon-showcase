@file:Suppress("UnstableApiUsage")


plugins {
    id("java-library-conventions")
}

project.description = "Showcase Command API"

dependencies {
    implementation(platform(project(":platform")))

    api(project(":showcase-identifier-extension"))
    api(libs.axon.modelling)
    api(libs.hibernate.validator)
    api(libs.jackson.annotations)

    implementation(libs.commons.lang3)
    implementation(libs.expressly)

    testFixturesImplementation(project(":showcase-test"))
    testFixturesImplementation(libs.commons.lang3)
    testFixturesImplementation(libs.hamcrest)
}
