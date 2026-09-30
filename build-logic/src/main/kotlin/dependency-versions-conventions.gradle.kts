import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("io.github.ben-manes.versions")
}

val catalogToml = rootProject.layout.projectDirectory.file("gradle/libs.versions.toml")
val catalogOwned =
    catalogToml.asFile.useLines { lines ->
        lines
            .mapNotNull { line ->
                Regex("""= \{ group = "([^"]+)", name = "([^"]+)", version.ref""").find(line)?.let {
                    "${it.groupValues[1]}:${it.groupValues[2]}"
                }
            }
            .toSet()
    }

fun readLinesOrEmpty(file: java.io.File): List<String> = if (file.exists()) file.readLines() else emptyList()

val majorDisabledFile = rootProject.layout.projectDirectory.file("config/dependency-updates/major-disabled.properties")
val holdBackFile = rootProject.layout.projectDirectory.file("config/dependency-updates/hold-back.properties")
val majorDisabled = DependencyUpdateRules.disabledEntries(readLinesOrEmpty(majorDisabledFile.asFile))
val holdBack = DependencyUpdateRules.holdBackEntries(readLinesOrEmpty(holdBackFile.asFile))

tasks.withType<DependencyUpdatesTask> {
    gradleReleaseChannel = "CURRENT"
    revision = "release"

    checkConstraints = true
    checkBuildEnvironmentConstraints = true

    rejectVersionIf {
        DependencyUpdateRules.shouldReject(
            candidate.group,
            candidate.module,
            candidate.version,
            currentVersion,
            catalogOwned,
            majorDisabled,
            holdBack,
        )
    }
}
