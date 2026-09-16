import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Verifies the modules' declared project dependencies against the sanctioned module graph, failing when a module takes
 * an edge the structure forbids and naming both ends of it. The rule set itself is [ModuleDependencyRules], which is
 * pure and unit-tested; this task only adapts it to Gradle's inputs and outputs.
 */
@CacheableTask
abstract class VerifyModuleDependenciesTask : DefaultTask() {

    /** Every declared module-to-module edge, gathered by the root build. */
    @get:Input abstract val edges: ListProperty<ModuleEdge>

    /** The report naming each forbidden edge, or stating that none was found. */
    @get:OutputFile abstract val resultFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val distinct = edges.get().distinct()
        val violations = ModuleDependencyRules.violations(distinct)

        val report = resultFile.get().asFile
        report.parentFile.mkdirs()
        report.writeText(
            buildString {
                appendLine("Checked ${distinct.size} module dependency edge(s).")
                if (violations.isEmpty()) {
                    appendLine("No forbidden edge.")
                } else {
                    violations.forEach { appendLine("Forbidden: $it") }
                }
                appendLine()
                appendLine("Inspected edges:")
                distinct.sortedBy { it.toString() }.forEach { appendLine("  $it") }
            }
        )

        if (violations.isNotEmpty()) {
            throw GradleException(
                "The module dependency graph has ${violations.size} forbidden edge(s):\n" +
                    violations.joinToString("\n") { "  - $it" }
            )
        }
    }
}
