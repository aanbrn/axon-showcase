import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails `check` when a guidance document's manual `helm install … --version` command disagrees with the catalog pin for
 * its chart.
 *
 * The block quotes the catalog pins directly, so no chart repository is resolved (unlike
 * [VerifyInfraImageVersionsTask]); [InstallCommandRules] holds the parsing and the mismatch rule.
 */
@CacheableTask
abstract class VerifyInstallCommandsTask : DefaultTask() {

    /** The guidance document whose manual install commands are checked. */
    @get:InputFile @get:PathSensitive(PathSensitivity.RELATIVE) abstract val sourceFile: RegularFileProperty

    /** The project directory the document's label is computed against; the label does not affect the result. */
    @get:Internal abstract val repoRoot: DirectoryProperty

    /** The catalog's `chartRef → pinnedVersion` pairs. */
    @get:Input abstract val chartPins: MapProperty<String, String>

    /** The verification's result file, so the task is cacheable. */
    @get:OutputFile abstract val resultFile: RegularFileProperty

    /** Verifies each install command against its chart's catalog pin, failing on a mismatch. */
    @TaskAction
    fun verify() {
        val pins = chartPins.get()
        val file = sourceFile.get().asFile
        val source = repoRoot.get().asFile.toPath().relativize(file.toPath()).toString()
        val commands = InstallCommandRules.installCommands(file.readLines())
        InstallCommandRules.noCommandsReason(source, commands.size)?.let { throw GradleException(it) }
        commands.forEach { (chartRef, version) ->
            InstallCommandRules.mismatchReason(source, chartRef, version, pins[chartRef])?.let {
                throw GradleException(it)
            }
            println("$source install command for '$chartRef' at '$version' matches the catalog pin")
        }

        resultFile.get().asFile.writeText("ok")
    }
}
