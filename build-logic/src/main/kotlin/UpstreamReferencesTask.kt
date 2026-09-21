import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

/**
 * A task that reports the state of the upstream references the repository's durable artifacts cite, by extracting each
 * `owner/repo#NNN` from the corpus and resolving it through the GitHub API.
 *
 * <p>The corpus spans `AGENTS.md`, `README.md`, `docs/adr/`, and `docs/ideas.md` — the files this task is configured
 * with. A reference is cited for several reasons (a dependency or tool gap we await, a documentation pointer, or a
 * counter-example), so a closed issue is a *trigger to check*, not a finding: the report lists every reference with its
 * state and where it is cited, and the workflow mentions the owner when any has closed, leaving the decision to them.
 *
 * <p>A reference that does not resolve (a renamed repository, a deleted issue) is reported as unresolved rather than
 * failing the task, because the corpus may legitimately cite a reference that no longer resolves.
 */
abstract class UpstreamReferencesTask : DefaultTask() {

    /** The files the references are extracted from. */
    @get:InputFiles abstract val sourceFiles: ConfigurableFileCollection

    /** The report file listing each reference with its state and citations. */
    @get:OutputFile abstract val reportFile: RegularFileProperty

    /** Executes `gh` for the API lookups; captured at configuration time. */
    @get:Inject abstract val execOperations: ExecOperations

    /** Extracts the references, resolves each, and writes the report. */
    @TaskAction
    fun report() {
        val root = project.rootDir.toPath()
        val refs = mutableMapOf<String, MutableList<String>>()
        sourceFiles.files
            .sortedBy { it.path }
            .forEach { file ->
                val label = runCatching { root.relativize(file.toPath()).toString() }.getOrDefault(file.name)
                file.readLines().forEachIndexed { index, line ->
                    UpstreamReferencePattern.REFERENCE.findAll(line).forEach { match ->
                        refs.getOrPut(match.value) { mutableListOf() }.add("$label:${index + 1}")
                    }
                }
            }

        val rows =
            refs.toSortedMap().map { (id, citations) ->
                val (repo, number) = id.split("#", limit = 2)
                ReferenceState(id, resolveState(repo, number), citations)
            }

        reportFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(
                if (rows.isEmpty()) {
                    "No upstream references found.\n"
                } else {
                    rows.joinToString("\n") { it.render() } + "\n"
                }
            )
        }
    }

    /** Resolves one reference's state (`open`, `closed YYYY-MM-DD`, or `unresolved`). */
    private fun resolveState(repo: String, number: String): String {
        val gh = ghCli()
        if (gh.isEmpty()) {
            return "unresolved"
        }
        val output = java.io.ByteArrayOutputStream()
        runCatching {
            execOperations.exec {
                commandLine(gh, "api", "repos/$repo/issues/$number", "--jq", ".state + \" \" + (.closed_at // \"\")")
                standardOutput = output
                errorOutput = java.io.ByteArrayOutputStream()
                isIgnoreExitValue = true
            }
        }
        val text = output.toString().trim()
        if (text.isBlank()) {
            return "unresolved"
        }
        val parts = text.split(" ", limit = 2)
        return when (parts[0]) {
            "open" -> "open"
            "closed" -> "closed ${parts.getOrElse(1) { "" }.take(10)}".trim()
            else -> "unresolved"
        }
    }

    /**
     * Resolves the absolute path of the `gh` executable from the current PATH, so the lookup does not depend on the
     * daemon JVM's cached PATH for native process spawning. Returns an empty string if `gh` cannot be found.
     */
    private fun ghCli(): String {
        val path = System.getenv("PATH") ?: return ""
        val executableNames =
            if (System.getProperty("os.name").startsWith("Windows")) listOf("gh.exe") else listOf("gh")
        return path.split(java.io.File.pathSeparator).firstNotNullOfOrNull { dir ->
            executableNames
                .firstOrNull { name ->
                    val executable = java.io.File(dir, name)
                    executable.isFile && executable.canExecute()
                }
                ?.let { java.io.File(dir, it).absolutePath }
        } ?: ""
    }

    /** One resolved reference as the report renders it. */
    private data class ReferenceState(val id: String, val state: String, val citations: List<String>) {
        fun render(): String = "$id\t$state\t${citations.joinToString(", ")}"
    }
}

/** The patterns the upstream-reference extraction uses, exposed for its unit tests. */
internal object UpstreamReferencePattern {

    /**
     * Matches `owner/repo#NNN`. It requires a `/` before the `#` and a digit after it, so the corpus's other id-shaped
     * tokens (`ADR-0002`, `CVE-2021-44228`, `UTF-8`, `ISO-8601`, `KAFKA-18281`) and a bare `owner/repo` reference do
     * not match.
     */
    val REFERENCE = Regex("[A-Za-z0-9._-]+/[A-Za-z0-9._-]+#[0-9]+")
}
