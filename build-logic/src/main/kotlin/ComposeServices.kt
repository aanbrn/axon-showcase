/**
 * The compose service each Gradle module addresses, for the per-service `compose*` tasks `docker-conventions`
 * registers.
 *
 * `docker-compose.yml` names its services without the `showcase-` module prefix — the module `showcase-api-gateway` is
 * the compose service `api-gateway` — so a task that passed the module name addressed a service that exists in no
 * compose file and failed with `no such service`. The mapping is explicit because Compose cannot express it: a service
 * is named, not derived, and a module only maps to one when it actually ships a service image.
 */
private val composeServicesByModule =
    mapOf(
        "showcase-api-gateway" to "api-gateway",
        "showcase-command-service" to "command-service",
        "showcase-query-service" to "query-service",
        "showcase-projection-service" to "projection-service",
        "showcase-web-ui" to "web-ui",
    )

/**
 * The compose service [moduleName] addresses, or null when the module ships none — a library module, `load-tests`, or
 * the `helm` module — for which no per-service compose task should be registered.
 */
fun composeServiceFor(moduleName: String): String? = composeServicesByModule[moduleName]
