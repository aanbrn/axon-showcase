/**
 * The rules the install-command gate applies to the guidance document that quotes the manual `helm install` block.
 *
 * The logic is pure so it is testable without Gradle: [VerifyInstallCommandsTask] reads the document, passes its
 * project-relative path as the source label, calls these rules, and turns a message into a failure.
 */
internal object InstallCommandRules {

    /** The `(chartRef, version)` pairs of the `helm install <name> <chartRef> --version <v>` lines in [lines]. */
    fun installCommands(lines: List<String>): List<Pair<String, String>> = lines.mapNotNull { line ->
        val tokens = line.trim().split(Regex("\\s+"))
        if (tokens.size < 4 || tokens[0] != "helm" || tokens[1] != "install") {
            return@mapNotNull null
        }
        val versionIndex = tokens.indexOf("--version")
        if (versionIndex < 0 || versionIndex + 1 >= tokens.size) {
            return@mapNotNull null
        }
        tokens[3] to tokens[versionIndex + 1]
    }

    /**
     * The reason [source], which states [commandCount] install commands, cannot be checked at all — a document the gate
     * scans that holds no `helm install … --version` line would otherwise pass vacuously — or null when it can.
     */
    fun noCommandsReason(source: String, commandCount: Int): String? =
        if (commandCount == 0) {
            "$source states no 'helm install … --version' command, so the gate would pass vacuously."
        } else {
            null
        }

    /**
     * The reason [source] documents [chartRef] at [documentedVersion] when the catalog pins [pinnedVersion], or null
     * when they agree. A null [pinnedVersion] means the catalog pins no version for the chart.
     */
    fun mismatchReason(source: String, chartRef: String, documentedVersion: String, pinnedVersion: String?): String? {
        if (pinnedVersion == null) {
            return "$source installs chart '$chartRef' at '$documentedVersion', but the catalog pins no " +
                "version for it."
        }
        if (documentedVersion != pinnedVersion) {
            return "$source installs chart '$chartRef' at '$documentedVersion', but the catalog pins " +
                "'$pinnedVersion'."
        }
        return null
    }
}
