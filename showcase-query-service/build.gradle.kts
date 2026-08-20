@file:Suppress("UnstableApiUsage")

import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("spring-boot-conventions")
    id("code-coverage-conventions")
}

project.description = "Showcase Query Service"

dependencies {
    implementation(platform(project(":platform")))

    implementation(project(":showcase-projection-model"))
    implementation(project(":showcase-query-api"))
    implementation(project(":showcase-query-proto"))

    implementation(libs.axon.springBoot.starter) {
        exclude(group = libs.axon.serverConnector.get().group, module = libs.axon.serverConnector.get().name)
    }

    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.validation)

    implementation(libs.spring.data.opensearch.starter) {
        exclude(
            group = libs.opensearch.client.restHighLevel.get().group,
            module = libs.opensearch.client.restHighLevel.get().name
        )
    }
    implementation(libs.opensearch.client.java)
    implementation(libs.elasticsearch.client.java)

    implementation(libs.jackson2.module.blackbird)

    implementation(libs.commons.lang3)
    implementation(libs.mapstruct)
    implementation(libs.streamex)

    implementation(libs.axon.micrometer)
    implementation(libs.axon.tracing.opentelemetry)
    implementation(libs.reactor.core.micrometer)

    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.micrometer.registry.otlp)
    implementation(libs.micrometer.tracing.bridge.otel)
    implementation(libs.opentelemetry.exporter.otlp)

    annotationProcessor(project(":showcase-mapstruct-extension"))
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
                implementation(project())
                implementation(project(":showcase-test"))
                implementation(testFixtures(project(":showcase-command-api")))
                implementation(testFixtures(project(":showcase-query-api")))
            }
        }

        val test = suites.getByName<JvmTestSuite>("test") {
            dependencies {
                implementation(libs.mockito.junit.jupiter)
            }
        }

        val componentTest = suites.register<JvmTestSuite>("componentTest") {
            dependencies {
                implementation(libs.axon.test)
                implementation(libs.hamcrest)
                implementation(libs.mockito.junit.jupiter)
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.starter.webflux)
                implementation(libs.spring.boot.starter.actuator)
                implementation(libs.spring.data.opensearch.starter) {
                    exclude(
                        group = libs.opensearch.client.restHighLevel.get().group,
                        module = libs.opensearch.client.restHighLevel.get().name
                    )
                }
                implementation(libs.opensearch.client.java)
                implementation(libs.spring.tx)
                implementation(project(":showcase-projection-model"))
                implementation(project(":showcase-query-proto"))
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
                implementation(project(":showcase-projection-model"))
                implementation(project(":showcase-query-proto"))

                implementation(libs.axon.test)
                implementation(libs.reactor.test)
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.starter.webflux)
                implementation(libs.spring.boot.testcontainers)
                implementation(libs.spring.data.opensearch.testcontainers) {
                    exclude(
                        group = libs.opensearch.client.restHighLevel.get().group,
                        module = libs.opensearch.client.restHighLevel.get().name
                    )
                }
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.testcontainers.opensearch)
                implementation(libs.netty.resolver.dnsNativeMacos)
                implementation(libs.reactor.blockhound)
                implementation(libs.reactor.tools)
            }

            targets {
                all {
                    testTask.configure {
                        jvmArgs = listOf(
                            "-XX:+AllowRedefinitionToAddDeleteMethods",
                            "-XX:+EnableDynamicAgentLoading"
                        )

                        shouldRunAfter(componentTest)
                    }
                }
            }
        }
    }
}

tasks.named<BootBuildImage>("bootBuildImage") {
    imageName = "aanbrn/axon-showcase-query-service:${project.version}"

    environment.putAll(
        mapOf(
            "BPE_DEFAULT_SERVER_PORT" to "8080",
            "BPE_DEFAULT_DB_HOSTS" to "axon-showcase-db-events",
            "BPE_DEFAULT_OS_URIS" to "http://axon-showcase-os-views:9200"
        )
    )
}
