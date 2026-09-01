import io.github.build.extensions.oss.gradle.plugins.helm.command.tasks.AbstractHelmCommandTask
import java.io.Serializable
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
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
abstract class VerifyInfraImageVersionsTask : AbstractHelmCommandTask() {

    @get:Input
    abstract val checks: ListProperty<InfraImageVersionCheck>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val valuesFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val resultFile: RegularFileProperty

    @TaskAction
    fun verify() {
        setupBitnamiRepository()

        checks.get().forEach { check ->
            val values = execHelmCaptureOutput("show", "values") {
                args(check.chartRef)
                option("--version", check.chartVersion)
            }
            val chartImageTag =
                topLevelImageTag(values.lines())
                    ?: throw GradleException(
                        "Chart '${check.chartRef}' values contain no top-level 'image.tag'."
                    )
            val imageAppVersion = check.imageTag.takeWhile { it.isDigit() || it == '.' }
            val chartAppVersion = chartImageTag.takeWhile { it.isDigit() || it == '.' }
            val imageSegments = imageAppVersion.split('.').size
            // The official tag must declare at least the minor version: a bare major (e.g. '17') is a floating
            // reference that Docker Hub re-points to the latest 17.x, so it cannot be a single source of truth.
            if (imageSegments < 2) {
                throw GradleException(
                    "${check.component} image tag '${check.imageTag}' is a floating reference; " +
                        "the official tag must declare at least the minor version (e.g. '17.6', not '17')."
                )
            }
            // The official tag is never mutated: the chart app version is truncated to the official tag's segment
            // count and compared exactly, so a two-segment official tag (17.6) matches a chart app version 17.6.0 at
            // minor granularity, while a full-patch official tag (3.9.0) requires an exact chart app version match.
            if (truncateToSegments(chartAppVersion, imageSegments) != imageAppVersion) {
                throw GradleException(
                    "Infra image version mismatch: ${check.component} image tag '${check.imageTag}' is inconsistent with " +
                        "chart '${check.chartRef}@${check.chartVersion}' preconfigured image tag '$chartImageTag' " +
                        "(app version '$chartAppVersion')."
                )
            }
            println(
                "${check.component}: image tag '${check.imageTag}' and chart '${check.chartRef}@${check.chartVersion}' " +
                    "preconfigured image tag '$chartImageTag' are consistent"
            )
        }

        verifyValuesFiles()

        resultFile.get().asFile.writeText("ok")
    }

    private fun setupBitnamiRepository() {
        // execHelm submits asynchronously to the worker queue and returns immediately; use the output-capturing
        // variant so each setup step blocks until it completes, keeping repo add -> repo update -> show values in
        // order even on a fresh machine where the repo does not yet exist.
        execHelmCaptureOutput("repo", "add") {
            args("bitnami", "https://charts.bitnami.com/bitnami")
        }
        execHelmCaptureOutput("repo", "update") {
            args("bitnami")
        }
    }

    private fun verifyValuesFiles() {
        valuesFiles.forEach { file ->
            val pinnedTag = topLevelImageTag(file.readLines())
            if (pinnedTag != null) {
                throw GradleException(
                    "values file '${file.path}' overrides 'image.tag' to '$pinnedTag'; " +
                        "the Helm deployment must use the chart's preconfigured image tag."
                )
            }
        }
    }

    private fun topLevelImageTag(lines: List<String>): String? {
        val imageIndex = lines.indexOfFirst { it == "image:" }
        if (imageIndex < 0) {
            return null
        }
        return lines.drop(imageIndex + 1)
            .takeWhile { it.startsWith("  ") }
            .firstOrNull { it.trimStart().startsWith("tag:") }
            ?.substringAfter("tag:")
            ?.trim()
    }

    private fun truncateToSegments(version: String, segments: Int): String =
        version.split('.').take(segments).joinToString(".")
}