import org.gradle.api.Project
import org.gradle.api.provider.SetProperty

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

/**
 * The set of compose task paths a consuming task scheduled at configuration time.
 *
 * The web UI's e2e wiring marks `:composeBuildAndUp` and `:composeDown` here so the `docker-conventions` `onlyIf` guard
 * lets them run when reached as dependencies (not only when requested by name). The state is a `SetProperty` held in
 * the root project's extra properties rather than an `extra` boolean per task, because a task action can hold a
 * reference to it while an execution-time read of `rootProject.extra` captures the project, which the configuration
 * cache rejects.
 */
@Suppress("UNCHECKED_CAST")
fun composeScheduleState(project: Project): SetProperty<String> {
    val composeScheduled = "composeScheduled"
    val extra = project.extensions.extraProperties
    return if (extra.has(composeScheduled)) {
        extra.get(composeScheduled) as SetProperty<String>
    } else {
        project.objects.setProperty(String::class.java).convention(emptySet()).also {
            extra.set(composeScheduled, it)
        }
    }
}
