@file:Suppress("UnstableApiUsage")


plugins {
    id("java-library-conventions")
}

project.description = "Showcase Query Client"

dependencies {
    implementation(platform(project(":platform")))

    api(project(":showcase-query-api"))
    api(libs.reactor.core)

    implementation(project(":showcase-query-proto"))

    implementation(libs.spring.boot.starter.webflux)

    implementation(libs.resilience4j.springBoot3)
    implementation(libs.resilience4j.reactor)

    implementation(project(":showcase-resilience4j-extension"))
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
                implementation(project())
                implementation(project(":showcase-test"))
                implementation(testFixtures(project(":showcase-projection-model")))
                implementation(testFixtures(project(":showcase-query-api")))
                implementation(testFixtures(project(":showcase-query-proto")))
            }
        }

        val test by getting(JvmTestSuite::class)

        val componentTest by register<JvmTestSuite>("componentTest") {
            dependencies {
                implementation(project(":showcase-resilience4j-extension"))

                implementation(libs.axon.springBoot.starter) {
                    exclude(
                        group = libs.axon.serverConnector.get().group,
                        module = libs.axon.serverConnector.get().name
                    )
                }
                implementation(libs.spring.data.opensearch.starter) {
                    exclude(
                        group = libs.opensearch.client.restHighLevel.get().group,
                        module = libs.opensearch.client.restHighLevel.get().name
                    )
                }
                implementation(libs.opensearch.client.java)
                implementation(libs.elasticsearch.client.java)
                implementation(libs.reactor.test)
                implementation(libs.resilience4j.springBoot3)
                implementation(libs.spring.boot.starter.aop)
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.starter.webflux)
                implementation(libs.wiremock.springBoot)
            }

            targets {
                all {
                    testTask.configure {
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
                implementation(libs.spring.data.opensearch.starter) {
                    exclude(
                        group = libs.opensearch.client.restHighLevel.get().group,
                        module = libs.opensearch.client.restHighLevel.get().name
                    )
                }
                implementation(libs.opensearch.client.java)
                implementation(libs.elasticsearch.client.java)
                implementation(libs.reactor.test)
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.testcontainers)
                implementation(libs.spring.data.opensearch.testcontainers) {
                    exclude(
                        group = libs.opensearch.client.restHighLevel.get().group,
                        module = libs.opensearch.client.restHighLevel.get().name
                    )
                }
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.testcontainers.opensearch)
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(componentTest)

                        dependsOn(":showcase-query-service:bootBuildImage")

                        systemProperty("disable-axoniq-console-message", "true")
                    }
                }
            }
        }
    }
}
