import java.io.Serializable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/** Where a pinned tool's latest version is published. */
enum class ToolingVersionSource {
    /** The npm registry's `latest` dist-tag, for [ToolingUpdateCheck.sourceRef] (a package name). */
    NPM_LATEST,

    /** The latest GitHub release tag, for [ToolingUpdateCheck.sourceRef] (an `owner/repo`). */
    GITHUB_RELEASE,
}

/** A tool version pinned in a workflow file, with the source its latest version comes from. */
data class ToolingUpdateCheck(
    val name: String,
    val workflowFile: String,
    val pinPattern: String,
    val source: ToolingVersionSource,
    val sourceRef: String,
) : Serializable

/**
 * A task that reports newer versions of the tool versions pinned in workflow inputs, by reading each pin from the
 * workflow that declares it and querying the registry or release feed that provides it.
 *
 * <p>Each pin is read from its workflow file rather than declared here, because a workflow `with:` input or `run:`
 * argument cannot read the version catalog: the workflow file is the only place CI actually consumes the pin.
 *
 * <p>A source lookup that fails, and a pin that does not match its pattern exactly once, are treated differently: the
 * lookup is reported as no update (as the other update checks do), while an unreadable pin fails the task, because a
 * pattern that silently stopped matching would make the check look permanently current.
 */
abstract class ToolingUpdatesTask : DefaultTask() {

    /** The pinned tools to check. */
    @get:Input abstract val checks: ListProperty<ToolingUpdateCheck>

    /** The workflow files the pins are read from. */
    @get:InputFiles abstract val pinFiles: ConfigurableFileCollection

    /** The report file listing each pinned tool with an available newer version. */
    @get:OutputFile abstract val reportFile: RegularFileProperty

    /** Reads each pin, queries its source, and writes the report. */
    @TaskAction
    fun check() {
        val report = mutableListOf<String>()
        checks.get().forEach { check ->
            val pinned = pinnedVersion(check)
            latestVersion(check)?.let { latest ->
                if (Versions.isNewer(latest, pinned)) {
                    report += "${check.name}: $pinned -> $latest"
                }
            }
        }
        val text = if (report.isEmpty()) "No tooling updates available.\n" else report.joinToString("\n") + "\n"
        reportFile.get().asFile.writeText(text)
        logger.lifecycle("\nTooling updates:\n$text")
    }

    /** The version pinned in the check's workflow file, failing when its pattern does not match exactly once. */
    private fun pinnedVersion(check: ToolingUpdateCheck): String {
        val file =
            pinFiles.files.firstOrNull { it.name == check.workflowFile }
                ?: error("${check.name}: ${check.workflowFile} is not among the declared pin files")
        val matches = Regex(check.pinPattern).findAll(file.readText()).map { it.groupValues[1] }.toList()
        check(matches.size == 1) {
            "${check.name}: expected exactly one match of '${check.pinPattern}' in " +
                "${check.workflowFile}, found ${matches.size}"
        }
        return matches.single()
    }

    /** Returns the latest version published for the check's source, or null when the lookup fails. */
    private fun latestVersion(check: ToolingUpdateCheck): String? =
        try {
            val uri =
                when (check.source) {
                    ToolingVersionSource.NPM_LATEST ->
                        URI.create("https://registry.npmjs.org/${check.sourceRef.replace("/", "%2F")}")
                    ToolingVersionSource.GITHUB_RELEASE ->
                        URI.create("https://api.github.com/repos/${check.sourceRef}/releases/latest")
                }
            val builder =
                HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10)).header("Accept", "application/json")
            if (check.source == ToolingVersionSource.GITHUB_RELEASE) {
                ToolingJson.githubToken()?.let { builder.header("Authorization", "Bearer $it") }
            }
            val request = builder.GET().build()
            val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
            val pattern =
                when (check.source) {
                    ToolingVersionSource.NPM_LATEST -> ToolingJson.NPM_LATEST
                    ToolingVersionSource.GITHUB_RELEASE -> ToolingJson.RELEASE_TAG
                }
            pattern.find(response.body())?.groupValues?.get(1)?.removePrefix("v")
        } catch (_: Exception) {
            null
        }
}

/** The provider JSON fields the update checks read, tolerant of the whitespace the providers vary in. */
internal object ToolingJson {
    /** The npm registry's `latest` dist-tag (npm returns compact JSON). */
    val NPM_LATEST: Regex = Regex("\"latest\":\\s*\"([^\"]+)\"")

    /**
     * The token a GitHub lookup may authenticate with, when CI provides one: an anonymous lookup is rate limited, and a
     * throttled response is indistinguishable from a current version.
     */
    fun githubToken(): String? = System.getenv("GH_TOKEN") ?: System.getenv("GITHUB_TOKEN")

    /** A GitHub release's `tag_name` (the API returns pretty-printed JSON). */
    val RELEASE_TAG: Regex = Regex("\"tag_name\":\\s*\"([^\"]+)\"")
}
