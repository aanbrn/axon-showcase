import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper

/**
 * The rules the Prettier-alignment `check` gate applies to the two declarations that format this repository's text: the
 * root Spotless formats' Prettier version, which the version catalog holds, and the version the web module's lockfile
 * resolves. Their divergence left a markdown file silently ungated once — Spotless skips a file it cannot converge on,
 * with a zero exit — so the agreement is verified rather than assumed. The logic is pure so it is testable without
 * Gradle: [VerifyPrettierAlignmentTask] reads the two files, calls these rules, and turns a message into a failure.
 */
internal object PrettierAlignmentRules {

    /** The catalog key that holds the root formatting's Prettier version. */
    private const val KEY = "prettier"

    /**
     * The catalog's entry for [KEY], anchored at its line's start so a key that merely *ends* in the pin's name
     * (`my-prettier`) does not match — the trailing `\s*=` already excludes a key the name prefixes
     * (`prettier-plugin`).
     */
    private val CATALOG_ENTRY = Regex("""(?m)^$KEY\s*=\s*"([^"]+)"""")

    /**
     * Strict by default: `readTree` accepts a trailing or second document and returns a missing node for empty input,
     * so a malformed lockfile would otherwise read as one that resolves nothing.
     */
    private val mapper = JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build()

    /** The version [catalog] records for the root formatting's Prettier, or null when it records none. */
    fun catalogVersion(catalog: String): String? = CATALOG_ENTRY.find(catalog)?.groupValues?.get(1)

    /** The version the web module's [lockfile] resolves for Prettier, or null when it resolves none. */
    fun resolvedVersion(lockfile: String): String? {
        val version = mapper.readTree(lockfile).path("packages").path("node_modules/prettier").path("version")
        return if (version.isTextual) version.asText() else null
    }

    /**
     * The reasons the two versions do not agree — a missing entry on either side, a lockfile that is not readable JSON,
     * or two different versions — and empty when they agree.
     */
    fun findings(catalog: String, lockfile: String): List<String> {
        val pinned =
            catalogVersion(catalog) ?: return listOf("the catalog records no '$KEY' version for the root formatting.")
        val resolved =
            try {
                resolvedVersion(lockfile)
            } catch (e: Exception) {
                return listOf("the web module's lockfile is not readable as JSON: ${e.message}")
            }
        return when {
            resolved == null -> listOf("the web module's lockfile resolves no Prettier version.")
            pinned != resolved ->
                listOf(
                    "the catalog pins '$KEY' at '$pinned', but the web module resolves '$resolved' — " +
                        "two formatter versions can disagree on whether a file converges at all.",
                )
            else -> emptyList()
        }
    }
}
