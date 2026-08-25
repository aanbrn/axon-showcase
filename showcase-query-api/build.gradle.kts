plugins {
    id("java-library-conventions")
}

project.description = "Showcase Query API"

dependencies {
    implementation(platform(project(":platform")))

    api(project(":showcase-command-api"))

    api(libs.axon.messaging)
    api(libs.spring.data.commons)
    api(libs.jackson2.databind)
    api(libs.swagger.annotations.jakarta)

    implementation(libs.commons.lang3)

    testFixturesImplementation(testFixtures(project(":showcase-command-api")))

    testFixturesImplementation(project(":showcase-test"))
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
                implementation(testFixtures(project(":showcase-command-api")))
            }
        }
    }
}
