import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails `check` when an `AGENTS.md` manual `helm install … --version` command disagrees with the catalog pin for its
 * chart.
 *
 * The block quotes the catalog pins directly, so no chart repository is resolved (unlike
 * [VerifyInfraImageVersionsTask]); [InstallCommandRules] holds the parsing and the mismatch rule.
 */
@CacheableTask
abstract class VerifyInstallCommandsTask : DefaultTask() {

    /** The `AGENTS.md` file whose manual install commands are checked. */
    @get:InputFile @get:PathSensitive(PathSensitivity.RELATIVE) abstract val agentsFile: RegularFileProperty

    /** The catalog's `chartRef → pinnedVersion` pairs. */
    @get:Input abstract val chartPins: MapProperty<String, String>

    /** The verification's result file, so the task is cacheable. */
    @get:OutputFile abstract val resultFile: RegularFileProperty

    /** Verifies each install command against its chart's catalog pin, failing on a mismatch. */
    @TaskAction
    fun verify() {
        val pins = chartPins.get()
        InstallCommandRules.installCommands(agentsFile.get().asFile.readLines()).forEach { (chartRef, version) ->
            InstallCommandRules.mismatchReason(chartRef, version, pins[chartRef])?.let { throw GradleException(it) }
            println("AGENTS.md install command for '$chartRef' at '$version' matches the catalog pin")
        }

        resultFile.get().asFile.writeText("ok")
    }
}
