/**
 * The rules `dependencyUpdates` applies to decide whether a candidate version is rejected.
 *
 * The suppression files under `config/dependency-updates/` are line lists rather than Java properties: an entry may be
 * a `group:module` coordinate, whose colon `Properties` would treat as a key/value separator (collapsing an exact entry
 * to a group prefix), so the lines are parsed here on their first `=` instead.
 */
internal object DependencyUpdateRules {

    /**
     * The suppression entries in [lines]: blank lines and `#` comments are skipped, and an optional `=value` is
     * dropped.
     */
    fun disabledEntries(lines: List<String>): Set<String> = lines.mapNotNull { entryLine(it)?.first }.toSet()

    /** The held coordinates in [lines] mapped to their version line; entries without a value are ignored. */
    fun holdBackEntries(lines: List<String>): Map<String, String> =
        lines.mapNotNull { entryLine(it) }.filter { it.second.isNotEmpty() }.toMap()

    /** The `entry to value` a content line declares, with the entry read as the text before the first `=`. */
    private fun entryLine(line: String): Pair<String, String>? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null
        }
        val separator = trimmed.indexOf('=')
        val entry = (if (separator < 0) trimmed else trimmed.substring(0, separator)).trim()
        val value = if (separator < 0) "" else trimmed.substring(separator + 1).trim()
        return if (entry.isEmpty()) null else entry to value
    }

    /** Whether [version] is not a stable release (a snapshot, milestone, RC, or a non-numeric shape). */
    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        return (stableKeyword || regex.matches(version)).not()
    }

    /** Whether [version] is calendar-versioned (its leading segment is a 4-digit year). */
    fun isCalendarVersioned(version: String): Boolean = version.takeWhile { it.isDigit() }.length == 4

    /** Whether [candidateVersion] is a major bump from [currentVersion]; a calendar coordinate compares its train. */
    fun isMajorBump(currentVersion: String, candidateVersion: String): Boolean =
        if (isCalendarVersioned(currentVersion)) {
            versionTrain(currentVersion) != versionTrain(candidateVersion)
        } else {
            (leadingInteger(candidateVersion) ?: 0) > (leadingInteger(currentVersion) ?: 0)
        }

    /** The first two segments of [version] (the release train). */
    fun versionTrain(version: String): String = version.split('.').take(2).joinToString(".")

    /** The leading integer of [version], or null when it declares none. */
    fun leadingInteger(version: String): Int? = version.takeWhile { it.isDigit() }.toIntOrNull()

    /** Whether [entry] (an exact `group:module` or a group prefix) covers the [group]:[name] coordinate. */
    fun matchesDisabled(entry: String, group: String, name: String): Boolean =
        if (entry.contains(":")) {
            "$group:$name" == entry
        } else {
            group == entry || group.startsWith("$entry.")
        }

    /**
     * Whether [candidateVersion] is held back by [heldLine]: it shares the held line's leading integer and sits on a
     * newer `major.minor` train. A patch within the held line, and any major jump, are not held back.
     */
    fun isHeldBack(candidateVersion: String, heldLine: String): Boolean {
        val heldMajor = leadingInteger(heldLine) ?: return false
        return leadingInteger(candidateVersion) == heldMajor &&
            Versions.isNewer(versionTrain(candidateVersion), versionTrain(heldLine))
    }

    /**
     * Whether `dependencyUpdates` rejects the candidate: a non-stable version without a stable current release, a
     * non-catalog-owned coordinate, a major-blocked entry, or a held-back coordinate.
     */
    fun shouldReject(
        group: String,
        module: String,
        candidateVersion: String,
        currentVersion: String,
        catalogOwned: Set<String>,
        majorDisabled: Set<String>,
        holdBack: Map<String, String>,
    ): Boolean {
        val notOwned = "$group:$module" !in catalogOwned && candidateVersion != currentVersion
        val blockedMajor =
            majorDisabled.any { matchesDisabled(it, group, module) } && isMajorBump(currentVersion, candidateVersion)
        val heldBack = holdBack["$group:$module"]?.let { isHeldBack(candidateVersion, it) } ?: false
        return (isNonStable(candidateVersion) && !isNonStable(currentVersion)) || notOwned || blockedMajor || heldBack
    }
}
