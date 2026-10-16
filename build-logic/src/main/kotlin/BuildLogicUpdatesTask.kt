import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * The Gradle Plugin Portal lookup and the build-script parse, named so a test pins each against real input rather than
 * a hand-written string.
 *
 * The portal is the one surface that carries all of the build-logic pins: it serves its own published plugins and
 * proxies Maven Central, where a coordinate such as `org.jetbrains.kotlin:kotlin-gradle-plugin` also lives. A
 * coordinate's `maven-metadata.xml` lists every published version, which is what the stability filter needs — its
 * `<release>` element names a pre-release for some coordinates (`2.5.0-Beta1` for Kotlin, `3.2.1-beta1` for the Helm
 * plugin), so that element is deliberately not read.
 */
internal object BuildLogicPins {

    /** A metadata entry's version; the document's `<latest>`/`<release>` elements are not `[version]` entries. */
    val VERSION = Regex("<version>([^<]+)</version>")

    /** The portal's metadata URL for a coordinate, whose dotted group becomes path segments. */
    fun metadataUri(group: String, module: String): URI =
        URI.create("https://plugins.gradle.org/m2/${group.replace('.', '/')}/$module/maven-metadata.xml")

    /**
     * The catalog aliases [buildScript] consumes as Gradle plugins, read from its `libs.<accessor>` references: Gradle
     * spells an alias's `-` as `.` in the accessor, so the accessor's segments rejoin as the alias, and the
     * `-plugin`-suffixed names are the pins this check covers.
     */
    fun consumedAliases(buildScript: String): Set<String> =
        Regex("""libs\.([A-Za-z0-9.]+)""")
            .findAll(buildScript)
            .map { it.groupValues[1].replace('.', '-') }
            .filter { it.endsWith("-plugin") }
            .toSet()

    /**
     * The mismatch between the aliases [declared] and the plugin aliases [buildScript] consumes, or null when the
     * declaration is faithful: a pin the build consumes but the list omits is the gap this check exists to close, a
     * declared pin the build no longer consumes is a stale entry, and a repeated entry cannot be a set member.
     */
    fun declarationMismatch(declared: List<String>, buildScript: String): String? {
        val consumed = consumedAliases(buildScript)
        val unlisted = consumed - declared.toSet()
        val notConsumed = declared.toSet() - consumed
        val repeated = declared.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        if (unlisted.isEmpty() && notConsumed.isEmpty() && repeated.isEmpty()) {
            return null
        }
        return "The declared plugin pins do not match the ones build-logic consumes: " +
            listOfNotNull(
                    repeated.takeIf { it.isNotEmpty() }?.let { "declared twice $it" },
                    unlisted.takeIf { it.isNotEmpty() }?.let { "unlisted $it" },
                    notConsumed.takeIf { it.isNotEmpty() }?.let { "declared but not consumed $it" },
                )
                .joinToString("; ")
    }

    /**
     * The highest version in [versions] that [DependencyUpdateRules.shouldReject] accepts, or null when it accepts
     * none. Filtering before selecting is what keeps a rejected newest candidate from hiding the newest acceptable one:
     * a pre-release, a deferred major, or a held-back train must not suppress the update behind it.
     */
    fun latestAccepted(
        versions: List<String>,
        group: String,
        module: String,
        current: String,
        catalogVersions: Map<String, String>,
        majorDisabled: Set<String>,
        holdBack: Map<String, String>,
    ): String? =
        Versions.highest(
            versions.filterNot {
                DependencyUpdateRules.shouldReject(group, module, it, current, catalogVersions, majorDisabled, holdBack)
            },
        )

    /**
     * The report row for [alias] pinned at [current], or null when it has no actionable update. A lookup that yielded
     * no version at all — [versions] is null, or its body carried no entry — is reported as unread rather than read as
     * current, so a repository that stopped answering stays visible instead of looking up to date.
     */
    fun rowFor(
        alias: String,
        current: String,
        versions: List<String>?,
        group: String,
        module: String,
        catalogVersions: Map<String, String>,
        majorDisabled: Set<String>,
        holdBack: Map<String, String>,
    ): String? {
        if (versions.isNullOrEmpty()) {
            return "$alias: $current -> unread (the portal returned no version)"
        }
        val latest =
            latestAccepted(versions, group, module, current, catalogVersions, majorDisabled, holdBack) ?: return null
        return if (Versions.isNewer(latest, current)) "$alias: $current -> $latest" else null
    }
}

/**
 * A task that reports newer stable releases for the Gradle plugin versions the included `build-logic` build pins.
 *
 * <p>Most of those pins appear in no report: `dependencyUpdates` runs in the main build, and a pin that resolves only
 * on the included build's buildscript classpath is invisible to the main build's resolution.
 *
 * <p>The pins are declared as the catalog aliases `build-logic` consumes, and each alias's coordinate and pinned
 * version are read from the catalog, so the check cannot disagree with either file; the declared list is asserted
 * against the aliases the build script actually consumes, so a pin added there fails the check rather than going
 * uncovered. Newer releases are filtered with the report's own [DependencyUpdateRules.shouldReject] and the same
 * suppression files, and a lookup that yields no version is reported rather than read as current.
 */
abstract class BuildLogicUpdatesTask : DefaultTask() {

    /** The catalog aliases of the plugin pins to check. */
    @get:Input abstract val aliases: ListProperty<String>

    /** The version catalog that owns each alias's coordinate and pinned version. */
    @get:InputFile abstract val catalogFile: RegularFileProperty

    /** The included build's script, whose consumed `-plugin` aliases the declared list is asserted against. */
    @get:InputFile abstract val buildLogicBuildFile: RegularFileProperty

    /** The major-blocked suppression list, as the report reads it. */
    @get:InputFile abstract val majorDisabledFile: RegularFileProperty

    /** The held-back suppression list, as the report reads it. */
    @get:InputFile abstract val holdBackFile: RegularFileProperty

    /** The report file listing each pin with an available newer stable release. */
    @get:OutputFile abstract val reportFile: RegularFileProperty

    /** Resolves each declared pin and writes the report. */
    @TaskAction
    fun check() {
        val declared = aliases.get()
        assertDeclaredMatchesConsumed(declared)

        val catalog = DependencyUpdateRules.catalogLibraries(catalogFile.get().asFile.readLines())
        val catalogVersions = catalog.values.associate { "${it.group}:${it.module}" to it.version }
        val majorDisabled = DependencyUpdateRules.disabledEntries(majorDisabledFile.get().asFile.readLines())
        val holdBack = DependencyUpdateRules.holdBackEntries(holdBackFile.get().asFile.readLines())

        val report =
            declared.sorted().mapNotNull { alias ->
                val library =
                    catalog[alias] ?: error("$alias is declared but the version catalog declares no such library")
                BuildLogicPins.rowFor(
                    alias,
                    library.version,
                    metadataVersions(library.group, library.module),
                    library.group,
                    library.module,
                    catalogVersions,
                    majorDisabled,
                    holdBack,
                )
            }

        val text = if (report.isEmpty()) "No build-logic pin updates available.\n" else report.joinToString("\n") + "\n"
        reportFile.get().asFile.writeText(text)
        logger.lifecycle("\nBuild-logic pin updates:\n$text")
    }

    /** Fails unless the declared list is exactly the set of plugin aliases the included build consumes. */
    private fun assertDeclaredMatchesConsumed(declared: List<String>) {
        BuildLogicPins.declarationMismatch(declared, buildLogicBuildFile.get().asFile.readText())?.let { error(it) }
    }

    /** The versions the portal publishes for the coordinate, or null when the lookup yields no readable body. */
    private fun metadataVersions(group: String, module: String): List<String>? =
        try {
            val request =
                HttpRequest.newBuilder(BuildLogicPins.metadataUri(group, module))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/xml")
                    .GET()
                    .build()
            val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            BuildLogicPins.VERSION.findAll(response.body()).map { it.groupValues[1] }.toList()
        } catch (_: Exception) {
            null
        }
}
