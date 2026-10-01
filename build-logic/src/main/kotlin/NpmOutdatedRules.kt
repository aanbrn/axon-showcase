/**
 * Filters the web UI's `npm outdated` report against the suppression list in
 * `config/web-ui-updates/major-disabled.txt`.
 *
 * `npm outdated` has no suppression flag and no hook equivalent to the JVM report's `rejectVersionIf`, so a package
 * whose major is deferred is filtered out of the rendered report instead: a listed package's row is kept only when the
 * version the manifest admits (`Wanted`) shares the newest published version's (`Latest`) leading integer. A row whose
 * columns cannot be read is kept, since a filter must not hide a row it does not understand.
 */
internal object NpmOutdatedRules {

    private const val HEADER_PREFIX = "Package"

    /**
     * The rendered report for [lines]: the table with a suppressed package's major-only rows removed. A blank input
     * yields a blank render (so the task's "no updates" branch still reads an empty report).
     */
    fun filterReport(lines: List<String>, suppressed: Set<String>): String {
        val header = lines.firstOrNull() ?: return ""
        val columns = columnIndexes(header)
        val body = lines.drop(1).mapNotNull { line -> keepOrDrop(line, columns, suppressed) }
        return (listOf(header) + body).joinToString("\n")
    }

    /** The suppression entries in [lines]: blank lines and `#` comments are skipped, each name kept whole. */
    fun suppressedEntries(lines: List<String>): Set<String> =
        lines
            .mapNotNull { raw ->
                val trimmed = raw.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) null else trimmed
            }
            .toSet()

    /** The index of each column name in [header], or an empty map when the header does not name them. */
    private fun columnIndexes(header: String): Map<String, Int> {
        if (!header.trimStart().startsWith(HEADER_PREFIX)) {
            return emptyMap()
        }
        return header.trim().split(Regex("\\s+")).mapIndexed { index, name -> name to index }.toMap()
    }

    /** [line] when it should be kept, or null when it is a suppressed package's major-only row. */
    private fun keepOrDrop(line: String, columns: Map<String, Int>, suppressed: Set<String>): String? {
        val cells = line.trim().split(Regex("\\s+"))
        val nameIndex = columns["Package"] ?: return line
        val wantedIndex = columns["Wanted"] ?: return line
        val latestIndex = columns["Latest"] ?: return line
        val name = cells.getOrNull(nameIndex) ?: return line
        if (name !in suppressed) {
            return line
        }
        val wanted = cells.getOrNull(wantedIndex) ?: return line
        val latest = cells.getOrNull(latestIndex) ?: return line
        val wantedMajor = wanted.takeWhile { it.isDigit() }
        val latestMajor = latest.takeWhile { it.isDigit() }
        if (wantedMajor.isEmpty() || latestMajor.isEmpty()) {
            return line
        }
        return if (wantedMajor == latestMajor) line else null
    }
}
