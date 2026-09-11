import java.io.Serializable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/** A pinned Paketo coordinate to check against its Docker Hub repository. */
data class BuildpackUpdateCheck(
    val name: String,
    val repository: String,
    val pinnedVersion: String,
) : Serializable

/**
 * A task that reports newer versions of the Paketo builder and buildpacks pinned in the version catalog, by querying
 * the Docker Hub tags API for each configured repository.
 *
 * <p>The Docker Hub repository is carried separately from [BuildpackUpdateCheck.name], because a buildpack's Cloud
 * Native Buildpacks id (`paketo-buildpacks/nginx`) is not its Docker Hub repository (`paketobuildpacks/nginx`).
 */
abstract class BuildpackUpdatesTask : DefaultTask() {

    /** The pinned Paketo coordinates to check. */
    @get:Input abstract val checks: ListProperty<BuildpackUpdateCheck>

    /** The report file listing each coordinate with an available newer version. */
    @get:OutputFile abstract val reportFile: RegularFileProperty

    /** Queries each repository and writes the report. */
    @TaskAction
    fun check() {
        val report = mutableListOf<String>()
        checks.get().forEach { check ->
            latestVersion(check.repository)?.let { latest ->
                if (isNewer(latest, check.pinnedVersion)) {
                    report += "${check.name}: ${check.pinnedVersion} -> $latest"
                }
            }
        }
        val text = if (report.isEmpty()) "No buildpack updates available.\n" else report.joinToString("\n") + "\n"
        reportFile.get().asFile.writeText(text)
        logger.lifecycle("\nBuildpack updates:\n$text")
    }

    /** Returns the highest numeric tag of the Docker Hub repository, or null if the lookup fails. */
    private fun latestVersion(repository: String): String? =
        try {
            val uri =
                URI.create(
                    "https://hub.docker.com/v2/repositories/$repository/tags?page_size=100&ordering=last_updated"
                )
            val request =
                HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build()
            val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
            Regex("\"name\":\"([0-9][^\"]*)\"")
                .findAll(response.body())
                .map { it.groupValues[1] }
                .maxWithOrNull { a, b -> compareVersions(a, b) }
        } catch (_: Exception) {
            null
        }

    /** Whether [candidate] is a newer version than [current], comparing numeric segments. */
    private fun isNewer(candidate: String, current: String): Boolean = compareVersions(candidate, current) > 0

    /** Compares two version strings by numeric segment, padding the shorter with zeros. */
    private fun compareVersions(a: String, b: String): Int {
        val aParts = numericParts(a)
        val bParts = numericParts(b)
        val max = maxOf(aParts.size, bParts.size)
        for (i in 0 until max) {
            val diff = aParts.getOrElse(i) { 0 } - bParts.getOrElse(i) { 0 }
            if (diff != 0) {
                return diff
            }
        }
        return aParts.size - bParts.size
    }

    /** The numeric segments of a version string (a non-numeric prefix yields an empty list). */
    private fun numericParts(version: String): List<Int> =
        version
            .takeWhile { it.isDigit() || it == '.' }
            .split('.')
            .filter { it.isNotEmpty() }
            .map { it.toIntOrNull() ?: 0 }
}
