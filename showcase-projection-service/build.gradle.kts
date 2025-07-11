@file:Suppress("UnstableApiUsage")

import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("spring-boot-conventions")
}

project.description = "Showcase Projection Service"

dependencies {
    implementation(project(":showcase-command-api"))
    implementation(project(":showcase-projection-model"))

    implementation(libs.axon.springBootStarter) {
        exclude(group = libs.axon.serverConnector.get().group, module = libs.axon.serverConnector.get().name)
    }
    implementation(libs.axon.extensions.kafka.springBootStarter)
    implementation(libs.axon.extensions.reactor.springBootStarter)

    implementation(libs.spring.boot.starter.data.elasticsearch)
    implementation(libs.spring.boot.starter.webflux)

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
                implementation(testFixtures(project(":showcase-projection-model")))
            }
        }

        val test by getting(JvmTestSuite::class)

        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(libs.axon.extensions.kafka)
                implementation(libs.axon.extensions.reactor)
                implementation(libs.axon.test)
                implementation(libs.reactor.test)
                implementation(libs.spring.boot.starter.data.elasticsearch)
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spring.boot.starter.webflux)
                implementation(libs.spring.boot.testcontainers)
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.testcontainers.kafka)
                implementation(libs.testcontainers.elasticsearch)
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)

                        systemProperty("disable-axoniq-console-message", "true")
                    }
                }
            }
        }
    }
}

tasks.named<BootBuildImage>("bootBuildImage") {
    imageName = "aanbrn/axon-showcase-projection-service:${project.version}"

    environment.putAll(
        mapOf(
            "BPE_DEFAULT_SERVER_PORT" to "8080",
            "BPE_DEFAULT_DB_HOSTS" to "axon-showcase-db-events",
            "BPE_DEFAULT_KAFKA_BOOTSTRAP_SERVERS" to "axon-showcase-kafka:9092",
            "BPE_DEFAULT_ES_URIS" to "http://axon-showcase-es-views:9200",
        )
    )
}
