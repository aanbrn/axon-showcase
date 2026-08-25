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
