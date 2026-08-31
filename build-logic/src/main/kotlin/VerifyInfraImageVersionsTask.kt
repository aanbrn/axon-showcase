import io.github.build.extensions.oss.gradle.plugins.helm.command.tasks.AbstractHelmCommandTask
import java.io.Serializable
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

data class InfraImageVersionCheck(
    val component: String,
    val chartRef: String,
    val chartVersion: String,
    val imageTag: String,
    val valuesDirs: List<String>,
) : Serializable

abstract class VerifyInfraImageVersionsTask : AbstractHelmCommandTask() {

    @get:Input
    abstract val checks: ListProperty<InfraImageVersionCheck>

    @TaskAction
    fun verify() {
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
            // The official image tag and the chart's preconfigured app version may differ only by trailing ".0"
            // zero-padding (e.g. postgres official 17.6 vs chart app version 17.6.0, or 17 vs 17.0.0); strip it from
            // both sides so such equivalent versions compare equal while a genuine patch difference still fails.
            if (stripTrailingZeroSegments(imageAppVersion) != stripTrailingZeroSegments(chartAppVersion)) {
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

            verifyValuesFiles(check)
        }
    }

    private fun verifyValuesFiles(check: InfraImageVersionCheck) {
        check.valuesDirs.forEach { valuesDir ->
            project.file(valuesDir).listFiles()
                .orEmpty()
                .filter { it.isFile && it.name.matches(Regex("values.*\\.ya?ml")) }
                .forEach { file ->
                    val pinnedTag = topLevelImageTag(file.readLines())
                    if (pinnedTag != null) {
                        throw GradleException(
                            "${check.component} values file '${file.path}' overrides 'image.tag' to '$pinnedTag'; " +
                                "the Helm deployment must use the chart's preconfigured image tag."
                        )
                    }
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

    private fun stripTrailingZeroSegments(version: String): String {
        var normalized = version
        while (normalized.endsWith(".0")) {
            normalized = normalized.removeSuffix(".0")
        }
        return normalized
    }
}