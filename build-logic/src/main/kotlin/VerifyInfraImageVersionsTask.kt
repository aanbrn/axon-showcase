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

/** One infra component's check: its official image tag against its pinned chart's preconfigured tag. */
data class InfraImageVersionCheck(
    val component: String,
    val chartRef: String,
    val chartVersion: String,
    val imageTag: String,
    val valuesDirs: List<String>,
) : Serializable

/**
 * Fails `check` when a pinned Bitnami chart's preconfigured `image.tag` disagrees with its official `*-image-tag`, or
 * when an infra values file pins `image.tag`.
 *
 * The chart is resolved over the network ([AbstractHelmRepositoriesTask]), so the task is cacheable on the pinned
 * coordinates and values files; [InfraImageVersionRules] holds the comparison.
 */
@CacheableTask
abstract class VerifyInfraImageVersionsTask : AbstractHelmRepositoriesTask() {

    /** The infra checks derived from the configured Helm releases. */
    @get:Input abstract val checks: ListProperty<InfraImageVersionCheck>

    /** The chart repositories the Helm resolution reads. */
    @get:Input abstract val repos: MapProperty<String, String>

    /** The infra values files, checked for a disallowed `image.tag` override. */
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) abstract val valuesFiles: ConfigurableFileCollection

    /** The verification's result file, so the task is cacheable. */
    @get:OutputFile abstract val resultFile: RegularFileProperty

    /** Verifies each chart's preconfigured tag and each values file, failing on a drift or an override. */
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

    /** Fails when an infra values file pins `image.tag`, which the deployment must not override. */
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
