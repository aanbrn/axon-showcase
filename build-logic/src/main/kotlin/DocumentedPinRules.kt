/**
 * The rules the documented-pin `check` gate applies to the live docs.
 *
 * The docs state the catalog's Paketo pins in prose, in two adjacent forms — `` `paketo-buildpacks/<name>@<version>` ``
 * and `` `builder-jammy-base:<version>` `` — and this object maps each to its catalog key and compares the version. The
 * logic is pure so it is testable without Gradle: [VerifyDocumentedPinsTask] reads the docs, calls these rules, and
 * turns a message into a failure.
 */
internal object DocumentedPinRules {

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
