import java.io.File
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.GradleException

tasks.register<Exec>("dependencySecurityCheck") {
    group = "verification"
    description = "Runs the Snyk dependency security scan across all sub-projects"

    workingDir = rootProject.layout.projectDirectory.asFile

    commandLine(snykExecutable(), "test", "--all-sub-projects", "--policy-path=.snyk")

    doFirst {
        if (snykExecutable().isEmpty()) {
            throw GradleException(
                "Snyk CLI is required to run the dependency security scan. Install it from https://snyk.io/download"
            )
        }
    }
}

// Resolves the absolute path of the snyk executable from the current PATH. The absolute path is used in the exec
// commandLine so it does not depend on the daemon JVM's cached PATH for native process spawning (frozen at JVM
// start, ignoring later PATH changes) — System.getenv("PATH") reflects the real shell PATH, so resolving the tool's
// absolute location from it works regardless of how the daemon was spawned. Returns an empty string if snyk cannot
// be found.
fun snykExecutable(): String {
    val path = System.getenv("PATH") ?: return ""
    val executableNames =
        if (SystemUtils.IS_OS_WINDOWS) {
            listOf("snyk.exe", "snyk.bat", "snyk.cmd")
        } else {
            listOf("snyk")
        }
    return path.split(File.pathSeparator).firstNotNullOfOrNull { dir ->
        executableNames
            .firstOrNull { name ->
                val executable = File(dir, name)
                executable.isFile && executable.canExecute()
            }
            ?.let { File(dir, it).absolutePath }
    } ?: ""
}
