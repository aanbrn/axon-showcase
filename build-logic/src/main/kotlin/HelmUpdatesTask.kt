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

data class HelmChartUpdateCheck(
    val name: String,
    val chartRef: String,
    val pinnedVersion: String,
    val repo: String,
) : Serializable

abstract class HelmUpdatesTask : AbstractHelmRepositoriesTask() {

    @get:Input
    abstract val helmCliVersion: Property<String>

    @get:Input
    abstract val chartChecks: ListProperty<HelmChartUpdateCheck>

    @get:Input
    abstract val repoUrls: MapProperty<String, String>

    @get:Input
    abstract val majorDisabled: SetProperty<String>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun check() {
        addHelmRepositories(repoUrls.get(), tolerateFailures = true)

        val report = mutableListOf<String>()

        helmCliLatest()?.let { latest ->
            if (isNewer(latest, helmCliVersion.get())) {
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
                if (isNewer(l, check.pinnedVersion)) {
                    report += "${check.name}: ${check.pinnedVersion} -> $l"
                }
            }
        }

        val text = if (report.isEmpty()) "No Helm updates available.\n" else report.joinToString("\n") + "\n"
        reportFile.get().asFile.writeText(text)
        logger.lifecycle("\nHelm updates:\n$text")
    }

    private fun latestChartVersion(check: HelmChartUpdateCheck): String? =
        try {
            val output =
                execHelmCaptureOutput("search", "repo") {
                    args(check.chartRef)
                }
            output
                .lines()
                .drop(1)
                .firstOrNull()
                ?.trim()
                ?.split(Regex("\\s+"))
                ?.getOrNull(1)
        } catch (_: Exception) {
            null
        }

    private fun latestSameMajorVersion(check: HelmChartUpdateCheck): String? =
        try {
            val output =
                execHelmCaptureOutput("search", "repo") {
                    args(check.chartRef, "--versions")
                }
            val pinnedMajor = leadingInteger(check.pinnedVersion)
            output
                .lines()
                .drop(1)
                .mapNotNull { line ->
                    line.trim().split(Regex("\\s+")).getOrNull(1)
                }
                .firstOrNull { candidate -> leadingInteger(candidate) == pinnedMajor }
        } catch (_: Exception) {
            null
        }

    private fun helmCliLatest(): String? =
        try {
            val request =
                HttpRequest.newBuilder(URI.create("https://api.github.com/repos/helm/helm/releases/latest"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/vnd.github+json")
                    .GET()
                    .build()
            val response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
            Regex("\"tag_name\":\"([^\"]+)\"")
                .find(response.body())
                ?.groupValues
                ?.get(1)
                ?.removePrefix("v")
        } catch (_: Exception) {
            null
        }

    private fun isNewer(candidate: String, current: String): Boolean {
        val candidateParts = numericParts(candidate)
        val currentParts = numericParts(current)
        val max = maxOf(candidateParts.size, currentParts.size)
        for (i in 0 until max) {
            val c = candidateParts.getOrElse(i) { 0 }
            val k = currentParts.getOrElse(i) { 0 }
            if (c != k) {
                return c > k
            }
        }
        return false
    }

    private fun numericParts(version: String): List<Int> =
        version
            .takeWhile { it.isDigit() || it == '.' }
            .split('.')
            .filter { it.isNotEmpty() }
            .map { it.toIntOrNull() ?: 0 }

    private fun leadingInteger(version: String): Int? = numericParts(version).firstOrNull()
}