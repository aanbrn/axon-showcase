@file:Suppress("UnstableApiUsage")


plugins {
    id("java-library-conventions")
}

project.description = "Showcase Command Client"

dependencies {
    implementation(platform(project(":platform")))

    api(project(":showcase-command-api"))
    api(libs.reactor.core)

    implementation(libs.axon.extensions.reactor.springBootStarter)

    implementation(libs.resilience4j.springBoot3)
    implementation(libs.resilience4j.reactor)

    implementation(project(":showcase-resilience4j-extension"))

    implementation(libs.commons.lang3)
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
                implementation(project())
                implementation(project(":showcase-test"))
                implementation(testFixtures(project(":showcase-command-api")))
            }
        }

        val test by getting(JvmTestSuite::class)

        val componentTest = register<JvmTestSuite>("componentTest") {
            dependencies {
                implementation(libs.axon.springBoot.starter) {
                    exclude(
                        group = libs.axon.serverConnector.get().group,
                        module = libs.axon.serverConnector.get().name
                    )
                }
                implementation(libs.axon.extensions.reactor.springBootStarter)
                implementation(libs.commons.lang3)
                implementation(libs.reactor.test)
                implementation(libs.reactor.blockhound)
                implementation(libs.reactor.tools)
                implementation(libs.resilience4j.springBoot3)
                implementation(libs.spring.boot.starter.aop)
                implementation(libs.spring.boot.starter.test)
            }

            targets {
                all {
                    testTask.configure {
                        jvmArgs = listOf(
                            "-XX:+AllowRedefinitionToAddDeleteMethods",
                            "-XX:+EnableDynamicAgentLoading"
                        )

                        shouldRunAfter(test)
                    }
                }
            }
        }

        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(libs.axon.springBoot.starter) {
                    exclude(
                        group = libs.axon.serverConnector.get().group,
                        module = libs.axon.serverConnector.get().name
                    )
                }
                implementation(libs.axon.extensions.jgroups.springBootStarter)
                implementation(libs.axon.extensions.reactor.springBootStarter)
                implementation(libs.reactor.test)
                implementation(libs.reactor.blockhound)
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.starter.webflux)
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.testcontainers.postgresql)
                implementation(libs.testcontainers.kafka)
            }

            targets {
                all {
                    testTask.configure {
                        jvmArgs = listOf(
                            "-XX:+AllowRedefinitionToAddDeleteMethods",
                            "-XX:+EnableDynamicAgentLoading"
                        )

                        shouldRunAfter(componentTest)
                        mustRunAfter(":showcase-command-service:integrationTest")

                        dependsOn(":showcase-command-service:bootBuildImage")

                        systemProperty("disable-axoniq-console-message", "true")
                    }
                }
            }
        }
    }
}
