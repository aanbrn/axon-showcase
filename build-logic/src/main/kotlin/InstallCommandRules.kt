/**
 * The rules the `AGENTS.md` install-command gate applies to the manual `helm install` block.
 *
 * The logic is pure so it is testable without Gradle: [VerifyInstallCommandsTask] reads the file, calls these rules,
 * and turns a message into a failure.
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
     * The reason the documented [chartRef] at [documentedVersion] disagrees with its [pinnedVersion], or null when they
     * agree. A null [pinnedVersion] means the catalog pins no version for the chart.
     */
    fun mismatchReason(chartRef: String, documentedVersion: String, pinnedVersion: String?): String? {
        if (pinnedVersion == null) {
            return "AGENTS.md installs chart '$chartRef' at '$documentedVersion', but the catalog pins no " +
                "version for it."
        }
        if (documentedVersion != pinnedVersion) {
            return "AGENTS.md installs chart '$chartRef' at '$documentedVersion', but the catalog pins " +
                "'$pinnedVersion'."
        }
        return null
    }
}
