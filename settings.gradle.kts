rootProject.name = "axon-showcase"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            setUrl("https://download.red-gate.com/maven/release")
        }
    }
}

include(
    "platform",
    "showcase-resilience4j-extension",
    "showcase-test",
    "showcase-command-api",
    "showcase-command-service",
    "showcase-command-client",
    "showcase-projection-model",
    "showcase-projection-service",
    "showcase-saga-service",
    "showcase-query-api",
    "showcase-query-proto",
    "showcase-query-service",
    "showcase-query-client",
    "showcase-api-gateway",
    "helm:chart",
    "load-tests"
)
