plugins {
    id("dependency-versions-conventions")
    id("docker-conventions")
    id("helm-releases-conventions")
}

allprojects {
    group = "com.github.aanbrn"
    version = "0.1.0-SNAPSHOT"
}

helm {
    releases {
        all {
            valuesDir("helm/values/$name")

            wait = true
            waitForJobs = true

            test {
                enabled = false
            }
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
                "axon-showcase"
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
                ":showcase-api-gateway:bootBuildImage"
            )

            mustInstallAfter(
                "kps",
                "tempo",
                "axon-showcase-db-events",
                "axon-showcase-kafka",
                "axon-showcase-os-views"
            )
        }
    }

    releaseTargets {
        create("local") {
            selectTags = "*"
        }
    }
}
