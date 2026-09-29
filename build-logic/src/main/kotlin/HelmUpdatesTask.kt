import java.io.Serializable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/** One chart's update check: its chart reference, catalog pin, and Helm repository. */
data class HelmChartUpdateCheck(
    val name: String,
    val chartRef: String,
    val pinnedVersion: String,
    val repo: String,
) : Serializable

/** Reports newer Helm CLI and chart versions for the configured charts. */
abstract class HelmUpdatesTask : AbstractHelmRepositoriesTask() {

    /** The Helm CLI version the project pins. */
    @get:Input abstract val helmCliVersion: Property<String>

    /** The chart checks to report on. */
    @get:Input abstract val chartChecks: ListProperty<HelmChartUpdateCheck>

    /** The chart repositories the report reads. */
    @get:Input abstract val repoUrls: MapProperty<String, String>

    /** The check names whose major-line updates are suppressed. */
    @get:Input abstract val majorDisabled: SetProperty<String>

    /** The report file the task writes. */
    @get:OutputFile abstract val reportFile: RegularFileProperty

    /** Builds the report of newer Helm CLI and chart versions. */
    @TaskAction
    fun check() {
        addHelmRepositories(repoUrls.get(), tolerateFailures = true)

        val report = mutableListOf<String>()

        helmCliLatest()?.let { latest ->
            if (Versions.isNewer(latest, helmCliVersion.get())) {
                report += "helm: ${helmCliVersion.get()} -> $latest"
            }
        }

        chartChecks.get().forEach { check ->
            val latest =
                if (check.name in majorDisabled.get()) {
                    latestSameMajorVersion(check)
                } else {
                    latestChartVersion(check)
                }
            latest?.let { l ->
                if (Versions.isNewer(l, check.pinnedVersion)) {
                    report += "${check.name}: ${check.pinnedVersion} -> $l"
                }
            }
        }

        val text = if (report.isEmpty()) "No Helm updates available.\n" else report.joinToString("\n") + "\n"
        reportFile.get().asFile.writeText(text)
        logger.lifecycle("\nHelm updates:\n$text")
    }

    /** The latest chart version [check]'s repository publishes, or null when the lookup fails. */
    private fun latestChartVersion(check: HelmChartUpdateCheck): String? =
        try {
            val output =
                execHelmCaptureOutput("search", "repo") {
                    args(check.chartRef)
                }
            output.lines().drop(1).firstOrNull()?.trim()?.split(Regex("\\s+"))?.getOrNull(1)
        } catch (_: Exception) {
            null
        }

    /** The latest same-major chart version for [check], or null when the lookup fails. */
    private fun latestSameMajorVersion(check: HelmChartUpdateCheck): String? =
        try {
            val output =
                execHelmCaptureOutput("search", "repo") {
                    args(check.chartRef, "--versions")
                }
            val versions = output.lines().drop(1).mapNotNull { line -> line.trim().split(Regex("\\s+")).getOrNull(1) }
            HelmUpdateRules.sameMajor(versions, check.pinnedVersion)
        } catch (_: Exception) {
            null
        }

    /** The latest Helm CLI release tag, or null when the lookup fails. */
    private fun helmCliLatest(): String? =
        try {
            val builder =
                HttpRequest.newBuilder(URI.create("https://api.github.com/repos/helm/helm/releases/latest"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/vnd.github+json")
            ToolingJson.githubToken()?.let { builder.header("Authorization", "Bearer $it") }
            val request = builder.GET().build()
            val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
            ToolingJson.RELEASE_TAG.find(response.body())?.groupValues?.get(1)?.removePrefix("v")
        } catch (_: Exception) {
            null
        }
}
