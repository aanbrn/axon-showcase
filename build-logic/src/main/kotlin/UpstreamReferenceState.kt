/**
 * Renders the state the report prints for one upstream reference from the triple the GitHub API returns.
 *
 * <p>The state is `open`, `closed (<state_reason>) <YYYY-MM-DD>`, `closed <YYYY-MM-DD>` when the API reported no
 * reason, or `unresolved` when the lookup produced no usable answer. The reason is the API's own token (`completed`,
 * `not_planned`, `duplicate`) rendered verbatim, so a duplicate or a declined closure reads differently from a
 * completed one.
 */
internal object UpstreamReferenceState {

    /**
     * The separator that joins the three fields; the report row itself is tab-separated, so the state cannot carry one.
     */
    private const val SEPARATOR = "|"

    /** The `gh api --jq` program producing the `state|state_reason|closed_at` triple [parse] reads. */
    val QUERY = ".state + \"$SEPARATOR\" + (.state_reason // \"\") + \"$SEPARATOR\" + (.closed_at // \"\")"

    /**
     * Renders [raw] — the `state|state_reason|closed_at` triple, empty where the API reported no value — as the state
     * column. A blank answer or one whose state token is not `open`/`closed` is `unresolved`, so an API change surfaces
     * as an unresolved reference rather than as text the report cannot be read against.
     */
    fun parse(raw: String): String {
        val parts = raw.trim().split(SEPARATOR, limit = 3)
        return when (parts[0]) {
            "open" -> "open"
            "closed" -> closed(parts)
            else -> "unresolved"
        }
    }

    /** Renders a closed reference with the API's reason when it reported one. */
    private fun closed(parts: List<String>): String {
        val reason = parts.getOrNull(1).orEmpty().trim()
        val closedAt = parts.getOrNull(2).orEmpty().trim().take(10)
        return when {
            closedAt.isEmpty() -> "closed"
            reason.isEmpty() -> "closed $closedAt"
            else -> "closed ($reason) $closedAt"
        }
    }
}
