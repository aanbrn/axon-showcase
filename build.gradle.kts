import io.github.build.extensions.oss.gradle.plugins.helm.release.dsl.HelmRelease
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.ExtensionAware

plugins {
    id("dependency-security-conventions")
    id("dependency-versions-conventions")
    id("docker-conventions")
    id("helm-releases-conventions")
    alias(libs.plugins.spotless)
    jacoco
}

spotless {
    kotlinGradle {
        target("*.gradle.kts", "build-logic/*.gradle.kts", "build-logic/src/**/*.gradle.kts")
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
    }
}

allprojects {
    group = "com.github.aanbrn"
    version = "0.1.0-SNAPSHOT"

    configurations.configureEach {
        resolutionStrategy {
            dependencySubstitution {
                substitute(module("org.lz4:lz4-java"))
                    .using(module(libs.lz4.java.get().toString()))
                    .because("Force relocation of LZ4 implementation")
            }

            eachDependency {
                if (requested.group == "org.lz4" && requested.name == "lz4-java") {
                    useTarget(libs.lz4.java.get().toString())
                    because("Force relocation of LZ4 implementation")
                }
            }
        }
    }
}

tasks.register<JacocoReport>("jacocoRootReport") {
    description = "Aggregates JaCoCo coverage across all modules that apply code-coverage-conventions"

    val covered = subprojects.filter { it.plugins.hasPlugin("code-coverage-conventions") }

    dependsOn(covered.map { "${it.path}:jacocoTestReport" })

    executionData.setFrom(
        covered.map { it.layout.buildDirectory.dir("jacoco").get().asFileTree.matching { include("*.exec") } }
    )
    sourceDirectories.setFrom(
        covered.map { it.extensions.getByType<SourceSetContainer>().named("main").get().allSource.srcDirs }
    )
    classDirectories.setFrom(
        covered.map {
            it.extensions.getByType<SourceSetContainer>().named("main").get().output.classesDirs.asFileTree.matching {
                exclude(
                    "**/*Proto.class",
                    "**/*OrBuilder.class",
                    "**/*OuterClass.class",
                    "**/*Grpc.class",
                )
            }
        }
    )

    reports {
        html.required = true
        xml.required = true
    }
}

val infraChartSpecs =
    mapOf(
        "bitnami/postgresql" to ("postgres" to libs.versions.postgres.image.tag),
        "bitnami/kafka" to ("kafka" to libs.versions.kafka.image.tag),
        "bitnami/opensearch" to ("opensearch" to libs.versions.opensearch.image.tag),
    )

tasks.register("verifyInfraImageVersions", VerifyInfraImageVersionsTask::class.java) {
    group = "verification"
    description = "Verifies each infra image tag matches the image tag preconfigured in its pinned Bitnami Helm chart"

    checks.set(
        providers.provider {
            val helmExtension = project.extensions.getByName("helm") as ExtensionAware
            @Suppress("UNCHECKED_CAST")
            val releases = helmExtension.extensions.getByName("releases") as NamedDomainObjectContainer<HelmRelease>
            releases.mapNotNull { release ->
                val chartRef = release.chart.map { it.chartLocation }.getOrNull()
                val spec = chartRef?.let { infraChartSpecs[it] }
                if (chartRef == null || spec == null) {
                    null
                } else {
                    InfraImageVersionCheck(
                        component = spec.first,
                        chartRef = chartRef,
                        chartVersion = release.version.get(),
                        imageTag = spec.second.get(),
                        valuesDirs =
                            release.valuesDirs.get().map { dir ->
                                rootProject.layout.projectDirectory.asFile.toPath().relativize(dir.toPath()).toString()
                            },
                    )
                }
            }
        }
    )

    dependsOn("helmUpdateRepositories")
}

tasks.named("check") {
    dependsOn("verifyInfraImageVersions")
}

helm {
    releases {
        all {
            valuesDir("helm/values/$name")

            wait = true
            waitForJobs = true

            test { enabled = false }
        }

        create("kps") {
            from("prometheus-community/kube-prometheus-stack")

            version = libs.versions.prometheus.community.stack

            namespace = "monitoring"
            createNamespace = true

            tags.add("monitoring")

            mustUninstallAfter(
                "tempo",
                "axon-showcase-db-events",
                "axon-showcase-kafka",
                "axon-showcase-os-views",
                "axon-showcase",
            )
        }

        create("tempo") {
            from("grafana/tempo")

            version = libs.versions.grafana.tempo

            namespace = "monitoring"
            createNamespace = true

            tags.add("monitoring")

            mustInstallAfter("kps")

            mustUninstallAfter("axon-showcase")
        }

        create("axon-showcase-db-events") {
            from("bitnami/postgresql")

            version = libs.versions.bitnami.postgresql

            tags.addAll(listOf("database", "db-events"))

            mustInstallAfter("kps")

            mustUninstallAfter("axon-showcase")
        }

        create("axon-showcase-os-views") {
            from("bitnami/opensearch")

            version = libs.versions.bitnami.opensearch

            tags.addAll(listOf("database", "os-views"))

            mustInstallAfter("kps")

            mustUninstallAfter("axon-showcase")
        }

        create("axon-showcase-kafka") {
            from("bitnami/kafka")

            version = libs.versions.bitnami.kafka

            tags.add("kafka")

            mustInstallAfter("kps")

            mustUninstallAfter("axon-showcase")
        }

        create("axon-showcase") {
            from(chart(":helm:chart", "main"))

            tags.add("application")

            installDependsOn(
                ":showcase-command-service:bootBuildImage",
                ":showcase-projection-service:bootBuildImage",
                ":showcase-query-service:bootBuildImage",
                ":showcase-api-gateway:bootBuildImage",
            )

            mustInstallAfter(
                "kps",
                "tempo",
                "axon-showcase-db-events",
                "axon-showcase-kafka",
                "axon-showcase-os-views",
            )
        }
    }

    releaseTargets { create("local") { selectTags = "*" } }
}
