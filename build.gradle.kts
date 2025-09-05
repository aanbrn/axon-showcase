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

            version = libs.versions.kube.prometheus.stack

            namespace = "monitoring"
            createNamespace = true

            tags.add("monitoring")

            mustUninstallAfter(
                "axon-showcase-db-events",
                "axon-showcase-os-views",
                "axon-showcase"
            )
        }

        create("axon-showcase-db-events") {
            from("bitnami/postgresql")

            version = libs.versions.bitnami.postgresql

            tags.addAll(listOf("database", "db-events"))

            mustInstallAfter("kps")

            mustUninstallAfter("axon-showcase")
        }

        create("axon-showcase-db-sagas") {
            from("bitnami/postgresql")

            version = libs.versions.bitnami.postgresql

            tags.addAll(listOf("database", "db-sagas"))

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
                ":showcase-saga-service:bootBuildImage",
                ":showcase-projection-service:bootBuildImage",
                ":showcase-query-service:bootBuildImage",
                ":showcase-api-gateway:bootBuildImage"
            )

            mustInstallAfter(
                "axon-showcase-db-events",
                "axon-showcase-db-sagas",
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
