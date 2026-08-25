import java.io.File
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.GradleException

tasks.register<Exec>("dependencySecurityCheck") {
    group = "verification"
    description = "Runs the Snyk dependency security scan across all sub-projects"

    workingDir = rootProject.layout.projectDirectory.asFile

    commandLine("snyk", "test", "--all-sub-projects")

    doFirst {
        if (!snykExecutableOnPath()) {
            throw GradleException(
                "Snyk CLI is required to run the dependency security scan. Install it from https://snyk.io/download"
            )
        }
    }
}

fun snykExecutableOnPath(): Boolean {
    val path = System.getenv("PATH") ?: return false
    val executableNames =
        if (SystemUtils.IS_OS_WINDOWS) {
            listOf("snyk.exe", "snyk.bat", "snyk.cmd")
        } else {
            listOf("snyk")
        }
    return path.split(File.pathSeparator).any { dir ->
        executableNames.any { name ->
            val executable = File(dir, name)
            executable.isFile && executable.canExecute()
        }
    }
}
