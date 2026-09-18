/** The rules `HelmUpdatesTask` applies when a chart lookup is restricted to the coordinate's pinned major version. */
internal object HelmUpdateRules {

    /** The first of [versions] whose leading integer equals the pinned version's, or null when none does. */
    fun sameMajor(versions: List<String>, pinnedVersion: String): String? {
        val pinnedMajor = Versions.leadingInteger(pinnedVersion)
        return versions.firstOrNull { Versions.leadingInteger(it) == pinnedMajor }
    }
}
