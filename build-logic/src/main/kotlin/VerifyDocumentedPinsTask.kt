import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
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

    /** The catalog's `key → pinnedVersion` pairs for the Paketo pins. */
    @get:Input abstract val paketoPins: MapProperty<String, String>

    /** The verification's result file, so the task is cacheable. */
    @get:OutputFile abstract val resultFile: RegularFileProperty

    /** Verifies each stated pin against its catalog pin, failing on a mismatch or an unowned key. */
    @TaskAction
    fun verify() {
        val docs = this.docs.files.associate { it.name to it.readText() }
        val stated = DocumentedPinRules.statedPins(docs)
        val findings = DocumentedPinRules.findings(stated, paketoPins.get())
        if (findings.isNotEmpty()) {
            throw GradleException(findings.joinToString("\n"))
        }
        stated.forEach { pin ->
            println("${pin.doc} states '${pin.written}', matching the catalog pin '${pin.catalogKey}'")
        }

        resultFile.get().asFile.writeText("ok")
    }
}
