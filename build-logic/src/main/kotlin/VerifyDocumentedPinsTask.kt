import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Fails `check` when a live doc states a catalog-owned Paketo pin at a version other than the catalog's.
 *
 * The docs state the pins in prose, so [DocumentedPinRules] holds the parsing and the mismatch rule; the catalog pins
 * are passed in, so nothing external is resolved.
 */
@CacheableTask
abstract class VerifyDocumentedPinsTask : DefaultTask() {

    /** The live docs whose stated Paketo pins are checked. */
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) abstract val docs: ConfigurableFileCollection

    /** The project directory the docs' labels are computed against; the labels do not affect the result. */
    @get:Internal abstract val repoRoot: DirectoryProperty

    /** The catalog's `key → pinnedVersion` pairs for the Paketo pins. */
    @get:Input abstract val paketoPins: MapProperty<String, String>

    /** The verification's result file, so the task is cacheable. */
    @get:OutputFile abstract val resultFile: RegularFileProperty

    /** Verifies each stated pin against its catalog pin, failing on a mismatch or an unowned key. */
    @TaskAction
    fun verify() {
        val root = repoRoot.get().asFile.toPath()
        val docs = DocumentedPinRules.relativeLabels(root, this.docs.files)
        val absent = DocumentedPinRules.absent(DocumentedPinRules.LIVE_DOCS, docs.keys)
        if (absent.isNotEmpty()) {
            throw GradleException("The documented-pin gate does not scan: ${absent.joinToString(", ")}")
        }
        val stated = DocumentedPinRules.statedPins(docs)
        val pins = paketoPins.get()
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
