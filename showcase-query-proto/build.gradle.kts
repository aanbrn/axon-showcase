@file:Suppress("UnstableApiUsage")

plugins {
    id("java-library-conventions")
    id("protobuf-conventions")
}

project.description = "Showcase Query Protocol"

dependencies {
    implementation(platform(project(":platform")))

    api(libs.axon.messaging)
    api(libs.protobuf.java)

    implementation(libs.commons.lang3)
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
                implementation(project())
                implementation(testFixtures(project(":showcase-test")))
            }
        }

        val test by getting(JvmTestSuite::class)

        register<JvmTestSuite>("componentTest") {
            dependencies {
                implementation(libs.jackson.databind)
                implementation(libs.jackson.jsr310)
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}
