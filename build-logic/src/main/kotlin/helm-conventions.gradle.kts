import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.citi.helm")
}

val libs = the<LibrariesForLibs>()

helm {
    downloadClient {
        enabled = true
        version = libs.versions.helm.asProvider().get()
    }

    repositories {
        bitnami()

        register("grafana") {
            url("https://grafana.github.io/helm-charts")
        }

        register("prometheus-community") {
            url("https://prometheus-community.github.io/helm-charts")
        }
    }

    filtering {
        values.putAll(
            mapOf(
                "bitnamiCommonVersion" to libs.versions.bitnami.common.get()
            )
        )
    }
}
