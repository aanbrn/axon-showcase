@file:Suppress("UnstableApiUsage")

import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("spring-boot-conventions")
}

project.description = "Showcase Saga Service"

dependencies {
    implementation(project(":showcase-command-client"))

    implementation(libs.axon.springBootStarter) {
        exclude(group = libs.axon.serverConnector.get().group, module = libs.axon.serverConnector.get().name)
    }
    implementation(libs.axon.extensions.jgroups.springBootStarter)
    implementation(libs.axon.extensions.kafka.springBootStarter)

    implementation(libs.jgroups.kunernetes)

    implementation(libs.spring.boot.starter.cache)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.starter.webflux)

    implementation(libs.dbScheduler.springBootStarter)

    implementation(libs.postgresql)
    implementation(libs.flyway.postgresql)

    implementation(libs.jackson.module.blackbird)

    implementation(libs.caffeine.jcache)

    implementation(libs.axon.micrometer)
    implementation(libs.axon.tracing.opentelemetry)

    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.micrometer.tracing.bridge.otel)
    implementation(libs.opentelemetry.exporter.otlp)

    runtimeOnly(libs.netty.resolver.dnsNativeMacos)
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
                implementation(project())
                implementation(project(":showcase-test"))
                implementation(testFixtures(project(":showcase-command-api")))
                implementation(testFixtures(project(":showcase-command-client")))
            }
        }

        val test by getting(JvmTestSuite::class)

        val componentTest by register<JvmTestSuite>("componentTest") {
            dependencies {
                implementation(libs.axon.test)
                implementation(libs.hamcrest)
                implementation(libs.mockito.junit.jupiter)
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
                implementation(libs.axon.extensions.kafka)
                implementation(libs.axon.extensions.reactor)
                implementation(libs.dbScheduler)
                implementation(libs.reactor.test)
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.testcontainers)
                implementation(libs.spring.data.jdbc)
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.testcontainers.kafka)
                implementation(libs.testcontainers.postgresql)
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(componentTest)

                        systemProperty("disable-axoniq-console-message", "true")
                    }
                }
            }
        }
    }
}

tasks.named<BootBuildImage>("bootBuildImage") {
    imageName = "aanbrn/axon-showcase-saga-service:${project.version}"

    environment.putAll(
        mapOf(
            "BPE_DEFAULT_SERVER_PORT" to "8080",
            "BPE_DEFAULT_DB_HOSTS" to "axon-showcase-db-sagas",
            "BPE_DEFAULT_JGROUPS_BIND_ADDR" to "SITE_LOCAL",
            "BPE_DEFAULT_JGROUPS_BIND_PORT" to "7800",
            "BPE_DEFAULT_JGROUPS_TCP_PING_HOSTS" to "axon-showcase-api-gateway[7800]," +
                    "axon-showcase-command-service[7800],axon-showcase-saga-service[7800]",
            "BPE_DEFAULT_KAFKA_BOOTSTRAP_SERVERS" to "axon-showcase-kafka:9092"
        )
    )
}
