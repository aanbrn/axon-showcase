import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Module dependency rules")
class ModuleDependencyRulesTests {

    @Test
    @DisplayName("Depending on a service application is forbidden")
    fun serviceApplicationTarget() {
        val violations = ModuleDependencyRules.violations(listOf(ModuleEdge("load-tests", "showcase-query-service")))

        assertThat(violations).hasSize(1)
        assertThat(violations.first()).contains("load-tests", "showcase-query-service")
    }

    @Test
    @DisplayName("An extension module may depend only on platform and showcase-test")
    fun extensionStaysALeaf() {
        val allowed =
            listOf(
                ModuleEdge("showcase-identifier-extension", "platform"),
                ModuleEdge("showcase-identifier-extension", "showcase-test"),
            )
        val forbidden = listOf(ModuleEdge("showcase-identifier-extension", "showcase-command-api"))

        assertThat(ModuleDependencyRules.violations(allowed)).isEmpty()
        assertThat(ModuleDependencyRules.violations(forbidden)).hasSize(1)
    }

    @Test
    @DisplayName("A contract module may not depend on a client or a service application")
    fun contractStaysAContract() {
        val onAClient = listOf(ModuleEdge("showcase-query-api", "showcase-query-client"))
        val onAService = listOf(ModuleEdge("showcase-query-api", "showcase-query-service"))

        assertThat(ModuleDependencyRules.violations(onAClient)).hasSize(1)
        assertThat(ModuleDependencyRules.violations(onAService)).hasSize(1)
        assertThat(ModuleDependencyRules.violations(listOf(ModuleEdge("showcase-query-api", "showcase-command-api"))))
            .isEmpty()
    }

    @Test
    @DisplayName("A self-edge is not a dependency between modules")
    fun selfEdgeIsIgnored() {
        val selfEdges =
            listOf(
                ModuleEdge("showcase-command-service", "showcase-command-service"),
                ModuleEdge("showcase-query-service", "showcase-query-service"),
            )

        assertThat(ModuleDependencyRules.violations(selfEdges)).isEmpty()
    }

    @Test
    @DisplayName("An unclassified module is permitted as a target")
    fun unclassifiedTargetIsPermitted() {
        assertThat(ModuleDependencyRules.violations(listOf(ModuleEdge("load-tests", "showcase-command-api")))).isEmpty()
    }

    @Test
    @DisplayName("A duplicate edge is reported once")
    fun duplicatesCollapse() {
        val edge = ModuleEdge("showcase-command-client", "showcase-command-service")

        assertThat(ModuleDependencyRules.violations(listOf(edge, edge))).hasSize(1)
    }
}
