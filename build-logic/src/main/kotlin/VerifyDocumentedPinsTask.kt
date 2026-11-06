import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails `check` when the catalog's Paketo pins and the build's Paketo check list disagree, or when a live doc states a
 * catalog-owned Paketo pin at a version other than the catalog's.
 *
 * The coverage is a precondition for the comparison's meaning: the pins the task verifies are the check list's, so a
 * catalog pin outside that list would never be compared. [PaketoPinCoverageRules] holds that comparison and
 * [DocumentedPinRules] the docs'; the catalog pins are passed in, so nothing external is resolved.
 */
@CacheableTask
abstract class VerifyDocumentedPinsTask : DefaultTask() {

    /** The live docs whose stated Paketo pins are checked. */
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) abstract val docs: ConfigurableFileCollection

    /** The project directory the docs' labels are computed against; the labels do not affect the result. */
    @get:Internal abstract val repoRoot: DirectoryProperty

    /** The catalog's `key → pinnedVersion` pairs for the Paketo pins. */
    @get:Input abstract val paketoPins: MapProperty<String, String>

    /** The catalog's version aliases, from which the Paketo keys are selected and normalized. */
    @get:Input abstract val catalogAliases: SetProperty<String>

    /** The verification's result file, so the task is cacheable. */
    @get:OutputFile abstract val resultFile: RegularFileProperty

    /** Verifies the catalog's coverage, then each stated pin against its catalog pin. */
    @TaskAction
    fun verify() {
        val pins = paketoPins.get()
        val catalog = PaketoPinCoverageRules.catalogKeys(catalogAliases.get())
        val uncovered = PaketoPinCoverageRules.uncovered(catalog, pins.keys)
        val unknown = PaketoPinCoverageRules.unknown(catalog, pins.keys)
        if (uncovered.isNotEmpty()) {
            throw GradleException(
                "The catalog pins Paketo coordinates the build does not check: ${uncovered.joinToString(", ")}",
            )
        }
        if (unknown.isNotEmpty()) {
            throw GradleException(
                "The build checks Paketo coordinates the catalog does not pin: ${unknown.joinToString(", ")}",
            )
        }
        val root = repoRoot.get().asFile.toPath()
        val docs = DocumentedPinRules.relativeLabels(root, this.docs.files)
        val absent = DocumentedPinRules.absent(DocumentedPinRules.LIVE_DOCS, docs.keys)
        if (absent.isNotEmpty()) {
            throw GradleException("The documented-pin gate does not scan: ${absent.joinToString(", ")}")
        }
        val stated = DocumentedPinRules.statedPins(docs)
        val unstated = DocumentedPinRules.unstated(DocumentedPinRules.PACK_DOC, stated, pins)
        if (unstated.isNotEmpty()) {
            throw GradleException("${DocumentedPinRules.PACK_DOC} states no pin for: ${unstated.joinToString(", ")}")
        }
        val findings = DocumentedPinRules.findings(stated, pins)
        if (findings.isNotEmpty()) {
            throw GradleException(findings.joinToString("\n"))
        }
        stated.forEach { pin ->
            println("${pin.doc} states '${pin.written}', matching the catalog pin '${pin.catalogKey}'")
        }

        resultFile.get().asFile.writeText("ok")
    }
}
