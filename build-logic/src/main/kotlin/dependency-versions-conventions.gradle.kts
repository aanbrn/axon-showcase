import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("io.github.ben-manes.versions")
}

val catalogToml = rootProject.layout.projectDirectory.file("gradle/libs.versions.toml")
val catalogVersions = DependencyUpdateRules.catalogVersions(catalogToml.asFile.readLines())

fun readLinesOrEmpty(file: java.io.File): List<String> = if (file.exists()) file.readLines() else emptyList()

val majorDisabledFile = rootProject.layout.projectDirectory.file("config/dependency-updates/major-disabled.txt")
val holdBackFile = rootProject.layout.projectDirectory.file("config/dependency-updates/hold-back.txt")
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
            catalogVersions,
            majorDisabled,
            holdBack,
        )
    }
}
