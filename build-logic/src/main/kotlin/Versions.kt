/**
 * The version ordering the update checks and the infra-image gate share.
 *
 * Segments are read numerically and the shorter side is zero-padded, so two spellings of one release (`5.15` and
 * `5.15.0`) compare equal where a release ordering is concerned. [compare] adds a longer-spelling tiebreak, which
 * [highest] uses to report a provider's canonical tag rather than its alias.
 */
internal object Versions {

    /** Whether [candidate] is a newer release than [current]; an equivalent spelling is not newer. */
    fun isNewer(candidate: String, current: String): Boolean {
        val candidateParts = numericParts(candidate)
        val currentParts = numericParts(current)
        for (i in 0 until maxOf(candidateParts.size, currentParts.size)) {
            val candidatePart = candidateParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }
            if (candidatePart != currentPart) {
                return candidatePart > currentPart
            }
        }
        return false
    }

    /**
     * Orders two versions by numeric segment, breaking a tie on the longer spelling.
     *
     * The tiebreak exists for selection, not for release ordering: a repository that publishes `5.15` and `5.15.0` for
     * the same image should be reported by its full form, and [isNewer] deliberately does not apply it.
     */
    fun compare(a: String, b: String): Int {
        val aParts = numericParts(a)
        val bParts = numericParts(b)
        for (i in 0 until maxOf(aParts.size, bParts.size)) {
            val diff = aParts.getOrElse(i) { 0 } - bParts.getOrElse(i) { 0 }
            if (diff != 0) {
                return diff
            }
        }
        return aParts.size - bParts.size
    }

    /** The highest version of [versions] by [compare], or null when the list is empty. */
    fun highest(versions: List<String>): String? = versions.maxWithOrNull { a, b -> compare(a, b) }

    /** The numeric segments of a version, ignoring a leading `v`; a non-numeric prefix declares none. */
    fun numericParts(version: String): List<Int> =
        version
            .removePrefix("v")
            .takeWhile { it.isDigit() || it == '.' }
            .split('.')
            .filter { it.isNotEmpty() }
            .map { it.toIntOrNull() ?: 0 }

    /** The first numeric segment of a version, or null when it declares none. */
    fun leadingInteger(version: String): Int? = numericParts(version).firstOrNull()
}
