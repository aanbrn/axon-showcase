import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Parses the chart's bundled Grafana dashboard files and fails when one is not valid JSON, naming it.
 *
 * The chart renders each dashboard into a ConfigMap as an opaque string, so `helm lint` and `helm template` never parse
 * it; this gate parses every file before failing, so a second malformed file is not hidden by the first. The rule set
 * is [DashboardJsonRules], which is pure and unit-tested; this task only adapts it to Gradle's inputs and outputs.
 */
@CacheableTask
abstract class VerifyDashboardJsonTask : DefaultTask() {

    /** The bundled Grafana dashboard files, resolved by the root build. */
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) abstract val dashboards: ConfigurableFileCollection

    /** The report naming each malformed dashboard, or stating that all are valid. */
    @get:OutputFile abstract val resultFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val files = dashboards.files.sortedBy { it.path }
        val violations = DashboardJsonRules.violations(files)

        val report = resultFile.get().asFile
        report.parentFile.mkdirs()
        report.writeText(
            buildString {
                appendLine("Checked ${files.size} bundled Grafana dashboard file(s).")
                if (violations.isEmpty()) {
                    appendLine("All are valid JSON.")
                } else {
                    violations.forEach { appendLine(it) }
                }
            }
        )

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Bundled Grafana dashboard check failed:\n" + violations.joinToString("\n") { "  - $it" }
            )
        }
    }
}
