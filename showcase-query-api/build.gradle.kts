@file:Suppress("UnstableApiUsage")

plugins {
    id("java-library-conventions")
}

project.description = "Showcase Query API"

dependencies {
    implementation(platform(project(":platform")))

    api(project(":showcase-command-api"))

    api(libs.axon.messaging)
    api(libs.spring.data.commons)
    api(libs.jackson.databind)
    api(libs.swagger.annotations.jakarta)

    implementation(libs.commons.lang3)

    testFixturesApi(testFixtures(project(":showcase-command-api")))

    testFixturesImplementation(project(":showcase-test"))
}
