import java.io.Serializable

/** A declared dependency from one module to another. */
data class ModuleEdge(val from: String, val to: String) : Serializable

/** The structural role a module plays, which decides which edges it may take and receive. */
enum class ModuleKind {
    SERVICE_APPLICATION,
    EXTENSION,
    CONTRACT,
    CLIENT,
    OTHER,
}

/**
 * The sanctioned module graph, expressed as the edges that must never appear.
 *
 * An allow-list would have to enumerate every legitimate edge and would fail on the tree the day it landed; naming the
 * forbidden ones is both stable and derivable from the repository's documents. The logic is pure so it is testable
 * without a Gradle runtime.
 */
object ModuleDependencyRules {

    private val serviceApplications =
        setOf(
            "showcase-command-service",
            "showcase-query-service",
            "showcase-projection-service",
            "showcase-api-gateway",
        )

    private val extensions =
        setOf(
            "showcase-identifier-extension",
            "showcase-mapstruct-extension",
            "showcase-resilience4j-extension",
        )

    private val contracts =
        setOf(
            "showcase-command-api",
            "showcase-query-api",
            "showcase-projection-model",
            "showcase-query-proto",
        )

    private val clients = setOf("showcase-command-client", "showcase-query-client")

    private val extensionAllowedTargets = setOf("platform", "showcase-test")

    /** Classifies a module by name; anything unrecognised is [ModuleKind.OTHER], which no rule forbids. */
    fun kindOf(module: String): ModuleKind =
        when (module) {
            in serviceApplications -> ModuleKind.SERVICE_APPLICATION
            in extensions -> ModuleKind.EXTENSION
            in contracts -> ModuleKind.CONTRACT
            in clients -> ModuleKind.CLIENT
            else -> ModuleKind.OTHER
        }

    /** Returns one message per forbidden edge, naming both ends; an empty list means the graph is sanctioned. */
    fun violations(edges: List<ModuleEdge>): List<String> =
        edges.distinct().filter { it.from != it.to }.mapNotNull(::violation)

    private fun violation(edge: ModuleEdge): String? {
        val from = kindOf(edge.from)
        val to = kindOf(edge.to)
        return when {
            to == ModuleKind.SERVICE_APPLICATION ->
                "${edge.from} depends on the service application ${edge.to} — " +
                    "services talk via a -client, Kafka, or HTTP"
            from == ModuleKind.EXTENSION && edge.to !in extensionAllowedTargets ->
                "${edge.from} is an -extension module and must stay a leaf, but depends on ${edge.to}"
            from == ModuleKind.CONTRACT && to == ModuleKind.CLIENT ->
                "${edge.from} is a contract module and must not depend on ${edge.to}"
            else -> null
        }
    }
}
