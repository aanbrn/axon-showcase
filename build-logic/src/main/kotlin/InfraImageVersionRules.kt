/**
 * The rules the infra-image gate applies to a pinned official tag and the chart's preconfigured tag.
 *
 * The logic is pure so it is testable without a Helm invocation: [VerifyInfraImageVersionsTask] queries the chart,
 * calls these rules, and turns a message into a failure.
 */
internal object InfraImageVersionRules {

    /** The tag under a top-level `image:` key in YAML [lines], or null when the values declare none. */
    fun topLevelImageTag(lines: List<String>): String? {
        val imageIndex = lines.indexOfFirst { it == "image:" }
        if (imageIndex < 0) {
            return null
        }
        return lines
            .drop(imageIndex + 1)
            .takeWhile { it.startsWith("  ") }
            .firstOrNull { it.trimStart().startsWith("tag:") }
            ?.substringAfter("tag:")
            ?.trim()
    }

    /**
     * The reason [check]'s official tag is a floating reference, or null when it declares at least the minor version.
     *
     * A bare major (`17`) is re-pointed by Docker Hub to the latest 17.x, so it cannot be a single source of truth.
     */
    fun floatingReferenceReason(check: InfraImageVersionCheck): String? {
        if (numericPrefix(check.imageTag).split('.').size >= 2) {
            return null
        }
        return "${check.component} image tag '${check.imageTag}' is a floating reference; " +
            "the official tag must declare at least the minor version (e.g. '17.6', not '17')."
    }

    /**
     * The reason [check]'s official tag disagrees with [chartImageTag], or null when they agree at the tag's
     * granularity.
     *
     * The official tag is never mutated: the chart app version is truncated to the official tag's segment count and
     * compared exactly, so a two-segment official tag (`17.6`) matches a chart app version `17.6.0` at minor
     * granularity, while a full-patch official tag (`3.9.0`) requires an exact chart app version match.
     */
    fun mismatchReason(check: InfraImageVersionCheck, chartImageTag: String): String? {
        val imageAppVersion = numericPrefix(check.imageTag)
        val chartAppVersion = numericPrefix(chartImageTag)
        val imageSegments = imageAppVersion.split('.').size
        if (truncateToSegments(chartAppVersion, imageSegments) == imageAppVersion) {
            return null
        }
        return "Infra image version mismatch: ${check.component} image tag '${check.imageTag}' is inconsistent with " +
            "chart '${check.chartRef}@${check.chartVersion}' preconfigured image tag '$chartImageTag' " +
            "(app version '$chartAppVersion')."
    }

    /** Truncates [version] to its first [segments] dot-separated segments. */
    fun truncateToSegments(version: String, segments: Int): String = version.split('.').take(segments).joinToString(".")

    /** The leading digits-and-dots run of a version. */
    private fun numericPrefix(version: String): String = version.takeWhile { it.isDigit() || it == '.' }
}
