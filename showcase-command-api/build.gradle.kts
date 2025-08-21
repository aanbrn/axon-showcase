@file:Suppress("UnstableApiUsage")

plugins {
    id("java-library-conventions")
}

project.description = "Showcase Command API"

dependencies {
    api(project(":showcase-identifier-extension"))
    api(libs.axon.modelling)
    api(libs.hibernate.validator)

    implementation(libs.commons.lang3)
    implementation(libs.expressly)

    testFixturesImplementation(project(":showcase-test"))
    testFixturesImplementation(libs.commons.lang3)
}
