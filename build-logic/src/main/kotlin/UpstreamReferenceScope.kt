/**
 * Selects the durable artifacts whose `owner/repo#NNN` references the upstream-reference report scans.
 *
 * <p>The corpus is `AGENTS.md`, `README.md`, `docs/adr/`, `docs/ideas.md`, and every project-authored skill under
 * `.opencode/skills/`. Generated skills (`openspec-*`, `opsx-*`) and vendored skills (`axon4to5-*`, `ast-grep`) are
 * excluded by provenance: the repository does not author them, so their references are not the repository's to track.
 */
internal object UpstreamReferenceScope {

    /** The root-level durable files the report scans. */
    private val ROOT_FILES = setOf("AGENTS.md", "README.md", "docs/ideas.md")

    /** The skill-directory prefixes the repository does not author. */
    private val EXCLUDED_SKILL_PREFIXES = listOf("ast-grep", "axon4to5-", "opsx-", "openspec-")

    /** Returns whether [relativePath] (relative to the repository root) is in the report's corpus. */
    fun includes(relativePath: String): Boolean {
        val path = relativePath.replace('\\', '/')
        if (path in ROOT_FILES) {
            return true
        }
        if (path.startsWith("docs/adr/") && path.endsWith(".md")) {
            return true
        }
        if (!path.startsWith(".opencode/skills/") || !path.endsWith("/SKILL.md")) {
            return false
        }
        val skill = path.removePrefix(".opencode/skills/").substringBefore('/')
        return EXCLUDED_SKILL_PREFIXES.none { skill.startsWith(it) }
    }
}
