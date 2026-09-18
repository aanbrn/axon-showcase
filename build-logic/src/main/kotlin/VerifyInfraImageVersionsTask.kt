import java.io.Serializable
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

data class InfraImageVersionCheck(
    val component: String,
    val chartRef: String,
    val chartVersion: String,
    val imageTag: String,
    val valuesDirs: List<String>,
) : Serializable

@CacheableTask
abstract class VerifyInfraImageVersionsTask : AbstractHelmRepositoriesTask() {

    @get:Input abstract val checks: ListProperty<InfraImageVersionCheck>

    @get:Input abstract val repos: MapProperty<String, String>

    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) abstract val valuesFiles: ConfigurableFileCollection

    @get:OutputFile abstract val resultFile: RegularFileProperty

    @TaskAction
    fun verify() {
        addHelmRepositories(repos.get())

        checks.get().forEach { check ->
            val values =
                execHelmCaptureOutput("show", "values") {
                    args(check.chartRef)
                    option("--version", check.chartVersion)
                }
            val chartImageTag =
                InfraImageVersionRules.topLevelImageTag(values.lines())
                    ?: throw GradleException("Chart '${check.chartRef}' values contain no top-level 'image.tag'.")
            InfraImageVersionRules.floatingReferenceReason(check)?.let { throw GradleException(it) }
            InfraImageVersionRules.mismatchReason(check, chartImageTag)?.let { throw GradleException(it) }
            println(
                "${check.component}: image tag '${check.imageTag}' and chart " +
                    "'${check.chartRef}@${check.chartVersion}' preconfigured image tag '$chartImageTag' are consistent"
            )
        }

        verifyValuesFiles()

        resultFile.get().asFile.writeText("ok")
    }

    private fun verifyValuesFiles() {
        valuesFiles.forEach { file ->
            val pinnedTag = InfraImageVersionRules.topLevelImageTag(file.readLines())
            if (pinnedTag != null) {
                throw GradleException(
                    "values file '${file.path}' overrides 'image.tag' to '$pinnedTag'; " +
                        "the Helm deployment must use the chart's preconfigured image tag."
                )
            }
        }
    }
}
