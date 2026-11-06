import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails `check` when a project-authored command's lifecycle declaration is missing, names a value its vocabulary does
 * not carry, points at an owner that does not resolve, omits the preflight a unit-starting command owes, or leaves a
 * generated entry unmapped.
 *
 * The claim is narrow by design: it proves declaration completeness, permitted values, reference resolution, the
 * preflight anchors, and mapping coverage — never that a command's prose behaves as declared. [CommandLifecycleRules]
 * holds every predicate; the derived coverage comes from the tracked file set, so an undeclared new command fails
 * rather than passing as an empty extraction.
 */
abstract class VerifyCommandLifecycleTask : DefaultTask() {

    /** Every command definition file the projection judges. */
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) abstract val commands: ConfigurableFileCollection

    /** The lifecycle section's source, which publishes the model and the generated mapping. */
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) abstract val agents: ConfigurableFileCollection

    /** The repository root the declarations' owner paths resolve against. */
    @get:Internal abstract val repoRoot: DirectoryProperty

    /** Verifies the declaration of every project-authored command and the generated entries' mapping. */
    @TaskAction
    fun verify() {
        val root = repoRoot.get().asFile
        val agentsFile = agents.files.single()
        val names = commands.files.associateBy { it.name }
        val generated = CommandLifecycleRules.generated(names.keys)
        val projectAuthored = names.filterKeys { it !in generated }
        val findings =
            CommandLifecycleRules.findings(
                projectAuthored = projectAuthored.mapValues { (_, file) -> CommandLifecycleRules.read(file) },
                generated = generated,
                agentsFile = CommandLifecycleRules.read(agentsFile),
                exists = { owner -> root.resolve(owner).exists() },
            )
        if (findings.isNotEmpty()) {
            throw GradleException(findings.joinToString("\n"))
        }
        println("Verified ${projectAuthored.size} command declarations and ${generated.size} generated mappings")
    }
}
