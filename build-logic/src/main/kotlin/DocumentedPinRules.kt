/**
 * The rules the documented-pin `check` gate applies to the live docs.
 *
 * The docs state the catalog's Paketo pins in prose, in two adjacent forms — `` `paketo-buildpacks/<name>@<version>` ``
 * and `` `builder-jammy-base:<version>` `` — and this object maps each to its catalog key and compares the version. The
 * logic is pure so it is testable without Gradle: [VerifyDocumentedPinsTask] reads the docs, calls these rules, and
 * turns a message into a failure.
 */
internal object DocumentedPinRules {

    /** The document the `infra-image-versions` requirement names as the site that states the Paketo pins. */
    const val PACK_DOC = ".opencode/skills/deployment-operations/SKILL.md"

    /**
     * The repo-relative documents the gate scans for a stated pin: the root guidance, the README, and the
     * deployment-operations pack the pins moved into. Declared here so the set is testable and a document the gate
     * should read cannot silently drop out of the build script.
     */
    val LIVE_DOCS =
        listOf(
            "AGENTS.md",
            "README.md",
            PACK_DOC,
        )

    /**
     * The docs' text keyed by project-relative path. A path key, not a file name: every subject pack is a `SKILL.md`,
     * so a name key would silently drop one pack's pins once a second pack states them.
     */
    fun relativeLabels(root: java.nio.file.Path, files: Iterable<java.io.File>): Map<String, String> = files.associate {
        root.relativize(it.toPath()).toString() to it.readText()
    }

    /**
     * The catalog keys [doc] does not state. The requirement names [doc] as the site that states the pins, so a
     * document that states none — or misses one key — is a failure rather than an empty finding list.
     */
    fun unstated(doc: String, stated: List<StatedPin>, pins: Map<String, String>): List<String> =
        pins.keys.filterNot { key -> stated.any { it.doc == doc && it.catalogKey == key } }

    /** The declared [liveDocs] the [scanned] set does not cover; empty when the gate reads every declared doc. */
    fun absent(liveDocs: List<String>, scanned: Set<String>): List<String> = liveDocs.filterNot { it in scanned }

    /** A pin a live doc states: the doc it appears in, the pin as written, its catalog key, and its version. */
    data class StatedPin(val doc: String, val written: String, val catalogKey: String, val version: String)

    private const val VERSION = """\d(?:[\w.-]*[a-z0-9])?"""

    private val BUILDPACK = Regex("""paketo-buildpacks/([a-z0-9-]+)@($VERSION)""")

    private val BUILDER = Regex("""builder-jammy-base:($VERSION)""")

    /** The pins [docs] (path → text) state, each mapped to its catalog key. */
    fun statedPins(docs: Map<String, String>): List<StatedPin> = docs.flatMap { (path, text) ->
        val buildpacks =
            BUILDPACK.findAll(text).map {
                StatedPin(path, it.value, "paketo-${it.groupValues[1]}", it.groupValues[2])
            }
        val builder =
            BUILDER.findAll(text).map {
                StatedPin(path, it.value, "paketo-builder-jammy-base", it.groupValues[1])
            }
        buildpacks + builder
    }

    /**
     * The reasons [stated] pins disagree with [pins] (catalog key → version). A stated pin whose key the catalog does
     * not hold is a failure too — the catalog is the single source, so a doc naming a key it does not pin is the
     * inconsistency the gate exists to catch — and a version that differs is a mismatch. Empty when every stated pin
     * agrees.
     */
    fun findings(stated: List<StatedPin>, pins: Map<String, String>): List<String> = stated.mapNotNull { pin ->
        val pinned =
            pins[pin.catalogKey]
                ?: return@mapNotNull "${pin.doc}: states '${pin.written}', whose key '${pin.catalogKey}' the catalog " +
                    "does not pin."
        if (pin.version != pinned) {
            "${pin.doc}: states '${pin.written}', but the catalog pins '${pin.catalogKey}' at '$pinned'."
        } else {
            null
        }
    }

    /** The reasons the pins [docs] state disagree with [pins], parsing [docs] first. */
    fun findings(docs: Map<String, String>, pins: Map<String, String>): List<String> = findings(statedPins(docs), pins)
}
