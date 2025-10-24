@file:Suppress("UnstableApiUsage")

import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("spring-boot-conventions")
}

project.description = "Showcase Command Service"

dependencies {
    implementation(platform(project(":platform")))

    implementation(project(":showcase-command-api"))

    implementation(libs.axon.springBoot.starter) {
        exclude(group = libs.axon.serverConnector.get().group, module = libs.axon.serverConnector.get().name)
    }
    implementation(libs.axon.extensions.jgroups.springBootStarter)
    implementation(libs.axon.extensions.kafka.springBootStarter)

    implementation(libs.jgroups.kunernetes)

    implementation(libs.spring.boot.starter.cache)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.validation)

    implementation(libs.postgresql)
    implementation(libs.flyway.postgresql)

    implementation(libs.dbScheduler.springBootStarter)

    implementation(libs.jackson.module.blackbird)

    implementation(libs.caffeine.jcache)

    implementation(libs.commons.lang3)
    implementation(libs.guava)
    implementation(libs.streamex)

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
                implementation(testFixtures(project(":showcase-command-api")))
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
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.testcontainers)
                implementation(libs.spring.data.jdbc)
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.testcontainers.postgresql)
                implementation(libs.testcontainers.kafka)
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
    imageName = "aanbrn/axon-showcase-command-service:${project.version}"

    environment.putAll(
        mapOf(
            "BPE_DEFAULT_SERVER_PORT" to "8080",
            "BPE_DEFAULT_DB_HOSTS" to "axon-showcase-db-events",
            "BPE_DEFAULT_JGROUPS_BIND_ADDR" to "SITE_LOCAL",
            "BPE_DEFAULT_JGROUPS_BIND_PORT" to "7800",
            "BPE_DEFAULT_JGROUPS_TCP_PING_HOSTS" to
                    "axon-showcase-api-gateway[7800],axon-showcase-command-service[7800]",
            "BPE_DEFAULT_KAFKA_BOOTSTRAP_SERVERS" to "axon-showcase-kafka:9092"
        )
    )
}
