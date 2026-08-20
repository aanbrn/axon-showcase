@file:Suppress("UnstableApiUsage")

plugins {
    id("java-library-conventions")
    id("protobuf-conventions")
    id("code-coverage-conventions")
}

extra["coverage.generatedClassExcludes"] = listOf(
    "**/QueryProto*.class",
    "**/QueryRequest*.class"
)

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
                implementation(project(":showcase-identifier-extension"))
                implementation(project(":showcase-test"))
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
