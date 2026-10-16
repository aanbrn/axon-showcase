import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Dependency update rules")
class DependencyUpdateRulesTests {

    private fun shouldReject(
        module: String,
        candidateVersion: String,
        currentVersion: String = "3.9.0",
        catalogVersions: Map<String, String> = mapOf("org.opensearch.client:$module" to "3.9.0"),
        majorDisabled: Set<String> = emptySet(),
        holdBack: Map<String, String> = emptyMap(),
    ): Boolean =
        DependencyUpdateRules.shouldReject(
            "org.opensearch.client",
            module,
            candidateVersion,
            currentVersion,
            catalogVersions,
            majorDisabled,
            holdBack,
        )

    @Test
    @DisplayName("A suppression entry is the text before the first equals sign, so a module colon survives")
    fun entryKeepsModuleColon() {
        val lines =
            listOf(
                "# a comment",
                "",
                "org.axonframework",
                "org.opensearch.client:spring-data-opensearch",
            )

        assertThat(DependencyUpdateRules.disabledEntries(lines))
            .containsExactlyInAnyOrder("org.axonframework", "org.opensearch.client:spring-data-opensearch")
    }

    @Test
    @DisplayName("A hold-back line maps its coordinate to the version line after the equals sign")
    fun holdBackMapsCoordinateToLine() {
        val lines =
            listOf(
                "# a comment",
                "",
                "org.opensearch.client:opensearch-java=3.9",
            )

        assertThat(DependencyUpdateRules.holdBackEntries(lines))
            .containsExactlyEntriesOf(mapOf("org.opensearch.client:opensearch-java" to "3.9"))
    }

    @Test
    @DisplayName("A hold-back line with no version line is ignored")
    fun holdBackWithoutVersionIgnored() {
        assertThat(DependencyUpdateRules.holdBackEntries(listOf("org.opensearch.client:opensearch-java"))).isEmpty()
    }

    @Test
    @DisplayName("An exact-coordinate major-disabled entry matches only its own module")
    fun exactEntryMatchesOnlyItsModule() {
        val entry = "org.opensearch.client:spring-data-opensearch"

        assertThat(DependencyUpdateRules.matchesDisabled(entry, "org.opensearch.client", "spring-data-opensearch"))
            .isTrue()
        assertThat(DependencyUpdateRules.matchesDisabled(entry, "org.opensearch.client", "opensearch-java")).isFalse()
        assertThat(DependencyUpdateRules.matchesDisabled(entry, "org.opensearch.client", "opensearch-rest-client"))
            .isFalse()
    }

    @Test
    @DisplayName("A group-prefix major-disabled entry matches the group and its dot-boundary sub-groups")
    fun groupEntryMatchesDotBoundary() {
        assertThat(DependencyUpdateRules.matchesDisabled("org.axonframework", "org.axonframework", "axon-core"))
            .isTrue()
        assertThat(DependencyUpdateRules.matchesDisabled("org.axonframework", "org.axonframework.messaging", "core"))
            .isTrue()
        assertThat(DependencyUpdateRules.matchesDisabled("org.jgroups", "org.jgroupsx", "x")).isFalse()
    }

    @Test
    @DisplayName("A semver major bump is a higher leading integer")
    fun semverMajorBump() {
        assertThat(DependencyUpdateRules.isMajorBump("3.9.0", "4.0.0")).isTrue()
        assertThat(DependencyUpdateRules.isMajorBump("3.9.0", "3.10.0")).isFalse()
        assertThat(DependencyUpdateRules.isMajorBump("3.9.0", "3.9.1")).isFalse()
    }

    @Test
    @DisplayName("A calendar train change is a major bump while a service release is not")
    fun calendarTrainBump() {
        assertThat(DependencyUpdateRules.isMajorBump("2025.0.7", "2025.1.0")).isTrue()
        assertThat(DependencyUpdateRules.isMajorBump("2025.0.7", "2026.0.0")).isTrue()
        assertThat(DependencyUpdateRules.isMajorBump("2025.0.7", "2025.0.8")).isFalse()
    }

    @Test
    @DisplayName("A candidate on a newer minor line than the held line is held back")
    fun newerMinorLineHeldBack() {
        assertThat(DependencyUpdateRules.isHeldBack("3.10.0", "3.9")).isTrue()
    }

    @Test
    @DisplayName("A patch within the held line is not held back")
    fun patchWithinLineNotHeldBack() {
        assertThat(DependencyUpdateRules.isHeldBack("3.9.1", "3.9")).isFalse()
        assertThat(DependencyUpdateRules.isHeldBack("3.9.0", "3.9")).isFalse()
    }

    @Test
    @DisplayName("A major jump for a held coordinate is not held back")
    fun majorJumpNotHeldBack() {
        assertThat(DependencyUpdateRules.isHeldBack("4.0.0", "3.9")).isFalse()
    }

    @Test
    @DisplayName(
        "The composed decision rejects a newer-line candidate and an unowned coordinate, and keeps a same-line patch and a major jump",
    )
    fun composedDecision() {
        val holdBack = mapOf("org.opensearch.client:opensearch-java" to "3.9")

        assertThat(shouldReject("opensearch-java", "3.10.0", holdBack = holdBack)).isTrue()
        assertThat(shouldReject("opensearch-java", "3.9.1", holdBack = holdBack)).isFalse()
        assertThat(shouldReject("opensearch-java", "4.0.0", holdBack = holdBack)).isFalse()
        assertThat(shouldReject("opensearch-java", "3.9.1", catalogVersions = emptyMap())).isTrue()
    }

    @Test
    @DisplayName("A hold-back entry holds only its own coordinate, not a sibling in the same group")
    fun holdBackIsPerCoordinate() {
        val holdBack = mapOf("org.opensearch.client:opensearch-java" to "3.9")

        assertThat(shouldReject("opensearch-java", "3.10.0", holdBack = holdBack)).isTrue()
        assertThat(shouldReject("opensearch-rest-client", "3.10.0", holdBack = holdBack)).isFalse()
    }

    @Test
    @DisplayName("A non-stable candidate against a stable current release is rejected")
    fun nonStableCandidateRejected() {
        assertThat(DependencyUpdateRules.isNonStable("3.10.0")).isFalse()
        assertThat(DependencyUpdateRules.isNonStable("3.10.0.RELEASE")).isFalse()
        assertThat(DependencyUpdateRules.isNonStable("3.10.0-SNAPSHOT")).isTrue()

        assertThat(shouldReject("opensearch-java", "3.10.0-SNAPSHOT")).isTrue()
    }

    @Test
    @DisplayName("A version that declares no leading integer is not held back")
    fun noLeadingIntegerNotHeldBack() {
        assertThat(DependencyUpdateRules.isHeldBack("RELEASE", "3.9")).isFalse()
        assertThat(DependencyUpdateRules.isHeldBack("3.10.0", "RELEASE")).isFalse()
    }

    @Test
    @DisplayName(
        "An exact major-disabled entry blocks its module's major jump and leaves another module's jump reported",
    )
    fun exactMajorDisabledBlocksItsModuleOnly() {
        val majorDisabled = setOf("org.opensearch.client:spring-data-opensearch")

        assertThat(
                shouldReject(
                    "spring-data-opensearch",
                    "3.0.0",
                    currentVersion = "2.0.8",
                    majorDisabled = majorDisabled,
                ),
            )
            .isTrue()
        assertThat(shouldReject("opensearch-java", "4.0.0", majorDisabled = majorDisabled)).isFalse()
    }

    @Test
    @DisplayName("Parsed entries drive the composed decision end to end")
    fun parsedEntriesDriveTheDecision() {
        val majorDisabled =
            DependencyUpdateRules.disabledEntries(listOf("org.opensearch.client:spring-data-opensearch"))
        val holdBack = DependencyUpdateRules.holdBackEntries(listOf("org.opensearch.client:opensearch-java=3.9"))
        val catalog =
            mapOf(
                "org.opensearch.client:spring-data-opensearch" to "2.0.8",
                "org.opensearch.client:opensearch-java" to "3.9.0",
            )

        assertThat(
                shouldReject(
                    "spring-data-opensearch",
                    "3.0.0",
                    currentVersion = "2.0.8",
                    catalogVersions = catalog,
                    majorDisabled = majorDisabled,
                ),
            )
            .isTrue()
        assertThat(
                shouldReject(
                    "opensearch-java",
                    "4.0.0",
                    catalogVersions = catalog,
                    majorDisabled = majorDisabled,
                    holdBack = holdBack,
                ),
            )
            .isFalse()
        assertThat(
                shouldReject(
                    "opensearch-java",
                    "3.10.0",
                    catalogVersions = catalog,
                    majorDisabled = majorDisabled,
                    holdBack = holdBack,
                ),
            )
            .isTrue()
    }

    @Test
    @DisplayName("The catalog pairs each referenced library's coordinate with the version its reference names")
    fun catalogPairsCoordinateWithReferencedVersion() {
        val lines =
            listOf(
                "[versions]",
                "log4j = \"2.26.1\"",
                "opensearch-java = \"3.9.0\"",
                "",
                "[libraries]",
                "log4j-api = { group = \"org.apache.logging.log4j\", name = \"log4j-api\", version.ref = \"log4j\" }",
                "log4j-core = { group = \"org.apache.logging.log4j\", name = \"log4j-core\", version.ref = \"log4j\" }",
                "opensearch-java = { group = \"org.opensearch.client\", name = \"opensearch-java\", version.ref = \"opensearch-java\" }",
            )

        assertThat(DependencyUpdateRules.catalogVersions(lines))
            .containsExactlyInAnyOrderEntriesOf(
                mapOf(
                    "org.apache.logging.log4j:log4j-api" to "2.26.1",
                    "org.apache.logging.log4j:log4j-core" to "2.26.1",
                    "org.opensearch.client:opensearch-java" to "3.9.0",
                ),
            )
    }

    @Test
    @DisplayName("A library entry whose version reference is absent from the versions table is skipped")
    fun catalogEntryWithAbsentReferenceSkipped() {
        val lines =
            listOf(
                "[versions]",
                "log4j = \"2.26.1\"",
                "[libraries]",
                "log4j-core = { group = \"org.apache.logging.log4j\", name = \"log4j-core\", version.ref = \"absent\" }",
            )

        assertThat(DependencyUpdateRules.catalogVersions(lines)).isEmpty()
    }

    @Test
    @DisplayName("A catalog library entry missing a field yields no pair rather than a wrong version")
    fun catalogEntryMissingFieldYieldsNoPair() {
        val lines =
            listOf(
                "[versions]",
                "log4j = \"2.26.1\"",
                "[libraries]",
                "log4j-core = { group = \"org.apache.logging.log4j\", version.ref = \"log4j\" }",
            )

        assertThat(DependencyUpdateRules.catalogVersions(lines)).isEmpty()
    }

    @Test
    @DisplayName("A candidate the catalog already declares is rejected as nothing to update")
    fun candidateAlreadyDeclaredIsRejected() {
        assertThat(rejectCandidate("2.26.1")).isTrue()
    }

    @Test
    @DisplayName("A candidate older than the version the catalog declares is rejected as nothing to update")
    fun candidateOlderThanCatalogIsRejected() {
        assertThat(rejectCandidate("2.26.0")).isTrue()
    }

    @Test
    @DisplayName("A candidate newer than the version the catalog declares is still reported")
    fun candidateNewerThanCatalogIsReported() {
        assertThat(rejectCandidate("2.27.0")).isFalse()
    }

    /** Whether the rule rejects [candidate] for `log4j-core`, whose catalog version is `2.26.1`. */
    private fun rejectCandidate(candidate: String): Boolean =
        DependencyUpdateRules.shouldReject(
            "org.apache.logging.log4j",
            "log4j-core",
            candidate,
            "2.17.1",
            mapOf("org.apache.logging.log4j:log4j-core" to "2.26.1"),
            emptySet(),
            emptyMap(),
        )

    @Test
    @DisplayName("An up-to-date coordinate is not rejected, so it keeps its place in the report's up-to-date section")
    fun upToDateCoordinateIsNotRejected() {
        val catalog = mapOf("org.apache.logging.log4j:log4j-core" to "2.26.1")

        assertThat(
                DependencyUpdateRules.shouldReject(
                    "org.apache.logging.log4j",
                    "log4j-core",
                    "2.26.1",
                    "2.26.1",
                    catalog,
                    emptySet(),
                    emptyMap(),
                ),
            )
            .isFalse()
    }
}
