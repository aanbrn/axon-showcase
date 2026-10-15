import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails `check` when the catalog's Prettier version is not the one the web module resolves.
 *
 * Both declarations are committed files, so the check reads text and resolves nothing external;
 * [PrettierAlignmentRules] holds the parsing and the comparison, so the task turns their message into a failure.
 */
@CacheableTask
abstract class VerifyPrettierAlignmentTask : DefaultTask() {

    /** The version catalog holding the root formatting's Prettier version. */
    @get:InputFile @get:PathSensitive(PathSensitivity.RELATIVE) abstract val catalog: RegularFileProperty

    /** The web module's lockfile, whose Prettier resolution is the version it formats with. */
    @get:InputFile @get:PathSensitive(PathSensitivity.RELATIVE) abstract val lockfile: RegularFileProperty

    /** The verification's result file, so the task is cacheable. */
    @get:OutputFile abstract val resultFile: RegularFileProperty

    /** Verifies the two versions agree, failing with the reasons when they do not. */
    @TaskAction
    fun verify() {
        val catalogText = catalog.get().asFile.readText()
        val findings = PrettierAlignmentRules.findings(catalogText, lockfile.get().asFile.readText())
        if (findings.isNotEmpty()) {
            throw GradleException(findings.joinToString("\n"))
        }
        println(
            "the root formatting and the web module both use Prettier " +
                "${PrettierAlignmentRules.catalogVersion(catalogText)}",
        )

        resultFile.get().asFile.writeText("ok")
    }
}
