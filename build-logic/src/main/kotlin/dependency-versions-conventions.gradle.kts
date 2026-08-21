import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.util.Properties

plugins {
    id("io.github.ben-manes.versions")
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

fun majorOf(version: String): Int? {
    val numeric = version.takeWhile { it.isDigit() }
    return numeric.toIntOrNull()
}

fun matchesDisabled(entry: String, group: String, name: String): Boolean {
    return if (entry.contains(":")) {
        "$group:$name" == entry
    } else {
        group == entry || group.startsWith("$entry.")
    }
}

val catalogToml = rootProject.layout.projectDirectory.file("gradle/libs.versions.toml")
val catalogOwned = catalogToml.asFile.useLines { lines ->
    lines.mapNotNull { line ->
        Regex("""= \{ group = "([^"]+)", name = "([^"]+)", version.ref""")
            .find(line)
            ?.let { "${it.groupValues[1]}:${it.groupValues[2]}" }
    }.toSet()
}

val majorDisabledFile =
    rootProject.layout.projectDirectory.file("config/dependency-updates/major-disabled.properties")
val majorDisabled = if (majorDisabledFile.asFile.exists()) {
    Properties().apply { majorDisabledFile.asFile.inputStream().use { load(it) } }
        .stringPropertyNames()
        .filter { it.isNotBlank() }
        .toSet()
} else {
    emptySet()
}

tasks.withType<DependencyUpdatesTask> {
    gradleReleaseChannel = "CURRENT"

    checkConstraints = true
    checkBuildEnvironmentConstraints = true

    rejectVersionIf {
        val coordinate = "${candidate.group}:${candidate.module}"
        val notOwned = coordinate !in catalogOwned && candidate.version != currentVersion
        val blockedMajor =
            majorDisabled.any { matchesDisabled(it, candidate.group, candidate.module) } &&
                    (majorOf(candidate.version) ?: 0) > (majorOf(currentVersion) ?: 0)
        isNonStable(candidate.version) && !isNonStable(currentVersion) || notOwned || blockedMajor
    }
}
