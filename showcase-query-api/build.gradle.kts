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
    api(libs.swagger.annotations.jakarta)

    testFixturesApi(testFixtures(project(":showcase-command-api")))

    testFixturesImplementation(project(":showcase-test"))
}
