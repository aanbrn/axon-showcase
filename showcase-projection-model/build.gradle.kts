@file:Suppress("UnstableApiUsage")

plugins {
    id("java-library-conventions")
}

project.description = "Showcase Projection Model"

dependencies {
    implementation(platform(project(":platform")))

    api(libs.spring.data.opensearch) {
        exclude(
            group = libs.opensearch.client.restHighLevel.get().group,
            module = libs.opensearch.client.restHighLevel.get().name
        )
    }
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
                implementation(project())
            }
        }

        val test = suites.getByName<JvmTestSuite>("test")

        register<JvmTestSuite>("componentTest") {
            dependencies {
                implementation(libs.jackson2.databind)
                implementation(libs.jackson2.jsr310)
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
