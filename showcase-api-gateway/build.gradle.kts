@file:Suppress("UnstableApiUsage")

import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("spring-boot-conventions")
}

project.description = "Showcase API Gateway"

dependencies {
    implementation(platform(project(":platform")))

    implementation(project(":showcase-command-client"))
    implementation(project(":showcase-query-client"))

    implementation(libs.axon.springBoot.starter) {
        exclude(group = libs.axon.serverConnector.get().group, module = libs.axon.serverConnector.get().name)
    }
    implementation(libs.axon.extensions.jgroups.springBootStarter)
    implementation(libs.axon.extensions.reactor.springBootStarter)

    implementation(libs.jgroups.kunernetes)

    implementation(libs.spring.boot.starter.aop)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.springdoc.openapi.starter.webflux.ui)

    implementation(libs.spring.data.commons)

    implementation(libs.hibernate.validator)

    implementation(libs.jackson.module.blackbird)

    implementation(libs.caffeine)

    implementation(libs.commons.lang3)
    implementation(libs.streamex)

    implementation(libs.resilience4j.springBoot3)

    implementation(libs.axon.micrometer)
    implementation(libs.axon.tracing.opentelemetry)

    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.micrometer.registry.otlp)
    implementation(libs.micrometer.tracing.bridge.otel)
    implementation(libs.opentelemetry.exporter.otlp)
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

        val test by getting(JvmTestSuite::class)

        val componentTest by register<JvmTestSuite>("componentTest") {
            dependencies {
                implementation(project(":showcase-command-client"))
                implementation(project(":showcase-query-client"))

                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.starter.webflux)
                implementation(libs.caffeine)
                implementation(libs.streamex)
                implementation(libs.netty.resolver.dnsNativeMacos)
                implementation(libs.reactor.blockhound)
                implementation(libs.resilience4j.all)
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
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.starter.webflux)
                implementation(libs.spring.boot.testcontainers)

                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.testcontainers.postgresql)
                implementation(libs.testcontainers.kafka)
                implementation(libs.testcontainers.opensearch)

                implementation(libs.netty.resolver.dnsNativeMacos)
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(componentTest)
                        mustRunAfter(":showcase-command-client:integrationTest")
                        mustRunAfter(":showcase-query-client:integrationTest")

                        dependsOn(
                            "bootBuildImage",
                            ":showcase-command-service:bootBuildImage",
                            ":showcase-projection-service:bootBuildImage",
                            ":showcase-query-service:bootBuildImage"
                        )

                        systemProperty("disable-axoniq-console-message", "true")
                    }
                }
            }
        }
    }
}

tasks.named<BootBuildImage>("bootBuildImage") {
    imageName = "aanbrn/axon-showcase-api-gateway:${project.version}"

    environment.putAll(
        mapOf(
            "BPE_DEFAULT_SERVER_PORT" to "8080",
            "BPE_DEFAULT_SHOWCASE_QUERY_SERVICE_URL" to "http://axon-showcase-query-service:8080",
            "BPE_DEFAULT_JGROUPS_BIND_ADDR" to "SITE_LOCAL",
            "BPE_DEFAULT_JGROUPS_BIND_PORT" to "7800",
            "BPE_DEFAULT_JGROUPS_TCP_PING_HOSTS" to
                    "axon-showcase-api-gateway[7800],axon-showcase-command-service[7800]"
        )
    )
}
