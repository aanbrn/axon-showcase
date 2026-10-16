import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Build-logic pin lookup and selection")
class BuildLogicUpdatesTaskTests {

    /**
     * A verbatim slice of the live portal metadata for `org.jetbrains.kotlin:kotlin-gradle-plugin` around its
     * `2.5.0-Beta1` release: the document names that pre-release in `<latest>`/`<release>` as well as in `<versions>`,
     * so the slice pins both what the pattern must read and what it must leave alone.
     */
    private val realMetadata =
        "<latest>2.5.0-Beta1</latest><release>2.5.0-Beta1</release>" +
            "<versions><version>2.4.21</version><version>2.4.21-RC</version><version>2.5.0-Beta1</version></versions>" +
            "<lastUpdated>20260923114938</lastUpdated>"

    /** The verbatim `dependencies` block of `build-logic/build.gradle.kts`. */
    private val realDependencyBlock =
        """
        dependencies {
            api(platform(libs.jackson2.bom))

            implementation(libs.kotlin.plugin)
            implementation(libs.lombok.plugin)
            implementation(libs.spring.boot.plugin)
            implementation(libs.protobuf.plugin)
            implementation(libs.gatling.plugin)
            implementation(libs.dependencyVersions.plugin)
            implementation(libs.errorprone.plugin)
            implementation(libs.spotbugs.plugin)
            implementation(libs.spotless.plugin)
            implementation(libs.helm.plugin)
            implementation(libs.helm.releases.plugin)
            implementation(libs.node.plugin)

            implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

            implementation(libs.commons.lang3)

            implementation(libs.jackson2.databind)

            testImplementation(kotlin("test"))
            testImplementation(platform(libs.assertj.bom))
            testImplementation(libs.assertj.core)

            constraints {
                api(libs.log4j.api)
                api(libs.log4j.core)
                api(libs.log4j.toSlf4j)
                api(libs.spotbugs)
            }
        }
        """
            .trimIndent()

    /** The catalog aliases the block above consumes as Gradle plugins. */
    private val pluginAliases =
        setOf(
            "kotlin-plugin",
            "lombok-plugin",
            "spring-boot-plugin",
            "protobuf-plugin",
            "gatling-plugin",
            "dependencyVersions-plugin",
            "errorprone-plugin",
            "spotbugs-plugin",
            "spotless-plugin",
            "helm-plugin",
            "helm-releases-plugin",
            "node-plugin",
        )

    @Test
    @DisplayName("The metadata URL path-segments a dotted group")
    fun metadataUrlSegmentsDottedGroup() {
        assertThat(BuildLogicPins.metadataUri("io.github.ben-manes", "gradle-versions-plugin"))
            .hasToString("https://plugins.gradle.org/m2/io/github/ben-manes/gradle-versions-plugin/maven-metadata.xml")
        assertThat(BuildLogicPins.metadataUri("com.diffplug.spotless", "com.diffplug.spotless.gradle.plugin"))
            .hasToString(
                "https://plugins.gradle.org/m2/com/diffplug/spotless/com.diffplug.spotless.gradle.plugin" +
                    "/maven-metadata.xml",
            )
    }

    @Test
    @DisplayName("The version pattern reads <versions> entries and ignores the document's <latest>/<release>")
    fun versionPatternIgnoresLatestAndRelease() {
        assertThat(BuildLogicPins.VERSION.findAll(realMetadata).map { it.groupValues[1] }.toList())
            .containsExactly("2.4.21", "2.4.21-RC", "2.5.0-Beta1")
    }

    @Test
    @DisplayName("The consumed aliases are the build script's plugin pins, not its other libraries")
    fun consumedAliasesAreThePluginPins() {
        assertThat(BuildLogicPins.consumedAliases(realDependencyBlock)).isEqualTo(pluginAliases)
    }

    @Test
    @DisplayName("A declaration that matches the build script's plugin pins has no mismatch")
    fun faithfulDeclarationHasNoMismatch() {
        assertThat(BuildLogicPins.declarationMismatch(pluginAliases.toList(), realDependencyBlock)).isNull()
    }

    @Test
    @DisplayName("An unlisted pin, a stale entry and a repeated entry each fail the declaration, named")
    fun declarationFailuresAreNamed() {
        val declared = pluginAliases.toList()

        assertThat(BuildLogicPins.declarationMismatch(declared - "helm-plugin", realDependencyBlock))
            .contains("unlisted")
            .contains("helm-plugin")
        assertThat(BuildLogicPins.declarationMismatch(declared + "gone-plugin", realDependencyBlock))
            .contains("declared but not consumed")
            .contains("gone-plugin")
        assertThat(BuildLogicPins.declarationMismatch(declared + "helm-plugin", realDependencyBlock))
            .contains("declared twice")
            .contains("helm-plugin")
    }

    @Test
    @DisplayName("A newer stable release becomes a report row")
    fun newerStableReleaseBecomesARow() {
        assertThat(row("lombok-plugin", listOf("9.5.0", "9.8.2"), "io.freefair.gradle", "lombok-plugin", "9.5.0"))
            .isEqualTo("lombok-plugin: 9.5.0 -> 9.8.2")
    }

    @Test
    @DisplayName("A lookup that yields no version becomes an unread row rather than a current one")
    fun unreadLookupBecomesARow() {
        val coordinate = Pair("com.github.node-gradle.node", "com.github.node-gradle.node.gradle.plugin")
        val failed = row("node-plugin", null, coordinate.first, coordinate.second, "7.1.0")
        val emptyBody = row("node-plugin", emptyList(), coordinate.first, coordinate.second, "7.1.0")

        assertThat(failed).isEqualTo("node-plugin: 7.1.0 -> unread (the portal returned no version)")
        assertThat(emptyBody).isEqualTo(failed)
    }

    @Test
    @DisplayName("A pre-release above the pin is filtered and the newest stable release is reported")
    fun preReleaseIsFiltered() {
        val versions = listOf("2.4.20", "2.4.21", "2.4.21-RC", "2.5.0-Beta1")

        assertThat(row("kotlin-plugin", versions, "org.jetbrains.kotlin", "kotlin-gradle-plugin", "2.4.20"))
            .isEqualTo("kotlin-plugin: 2.4.20 -> 2.4.21")
    }

    @Test
    @DisplayName("A deferred major is filtered without hiding the acceptable release behind it")
    fun deferredMajorDoesNotHideTheCandidate() {
        val springBoot = Pair("org.springframework.boot", "spring-boot-gradle-plugin")
        val versions = listOf("3.5.15", "3.5.16", "4.1.1")

        assertThat(row("spring-boot-plugin", versions, springBoot.first, springBoot.second, "3.5.15"))
            .isEqualTo("spring-boot-plugin: 3.5.15 -> 4.1.1")
        assertThat(
                row(
                    "spring-boot-plugin",
                    versions,
                    springBoot.first,
                    springBoot.second,
                    "3.5.15",
                    majorDisabled = setOf("org.springframework"),
                ),
            )
            .isEqualTo("spring-boot-plugin: 3.5.15 -> 3.5.16")
    }

    @Test
    @DisplayName("A train the hold-back list defers is filtered")
    fun heldBackTrainIsFiltered() {
        val helm = Pair("io.github.build-extensions-oss.helm", "helm-plugin")
        val versions = listOf("3.2.0", "3.3.0")

        assertThat(row("helm-plugin", versions, helm.first, helm.second, "3.2.0"))
            .isEqualTo("helm-plugin: 3.2.0 -> 3.3.0")
        assertThat(
                row(
                    "helm-plugin",
                    versions,
                    helm.first,
                    helm.second,
                    "3.2.0",
                    holdBack = mapOf("io.github.build-extensions-oss.helm:helm-plugin" to "3.2.0"),
                ),
            )
            .isNull()
    }

    /** The report row for a coordinate pinned at [current], with the suppression lists under test applied. */
    private fun row(
        alias: String,
        versions: List<String>?,
        group: String,
        module: String,
        current: String,
        majorDisabled: Set<String> = emptySet(),
        holdBack: Map<String, String> = emptyMap(),
    ): String? =
        BuildLogicPins.rowFor(
            alias,
            current,
            versions,
            group,
            module,
            mapOf("$group:$module" to current),
            majorDisabled,
            holdBack,
        )
}
