import java.io.File

/**
 * The rules `HelmUpdatesTask` applies when a chart lookup is restricted to the coordinate's pinned major version, and
 * to decide which charts the report suppresses.
 *
 * `config/helm-updates/major-disabled.txt` is a line list rather than Java properties — one chart name per line, `#`
 * comments and blank lines skipped — so it is parsed here rather than by `java.util.Properties`, which reads a key at
 * its first separator and would silently reinterpret an entry that carried one. The file path is supplied by the helm
 * convention plugin, which owns it (a plain object cannot resolve the project directory).
 */
internal object HelmUpdateRules {

    /** The suppression entries in [lines]: blank lines and `#` comments are skipped, an optional `=value` dropped. */
    fun disabledEntries(lines: List<String>): Set<String> = lines.mapNotNull { entryLine(it) }.toSet()

    /** [disabledEntries] read from [file]; an absent file yields no entries. */
    fun disabledEntriesOf(file: File): Set<String> =
        if (file.exists()) disabledEntries(file.readLines()) else emptySet()

    /** The entry a content line declares, read as the text before the first `=` (an optional value is discarded). */
    private fun entryLine(line: String): String? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null
        }
        val separator = trimmed.indexOf('=')
        val entry = (if (separator < 0) trimmed else trimmed.substring(0, separator)).trim()
        return entry.ifEmpty { null }
    }

    /** The first of [versions] whose leading integer equals the pinned version's, or null when none does. */
    fun sameMajor(versions: List<String>, pinnedVersion: String): String? {
        val pinnedMajor = Versions.leadingInteger(pinnedVersion)
        return versions.firstOrNull { Versions.leadingInteger(it) == pinnedMajor }
    }
}
