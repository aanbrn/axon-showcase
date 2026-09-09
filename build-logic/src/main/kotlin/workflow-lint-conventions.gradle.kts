import java.io.File
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.GradleException

tasks.register<Exec>("workflowLint") {
    group = "verification"
    description = "Lints GitHub Actions workflows with actionlint"

    workingDir = rootProject.layout.projectDirectory.asFile

    val workflowFiles = fileTree(rootProject.layout.projectDirectory.dir(".github/workflows")) { include("*.yml") }
    inputs.files(workflowFiles)

    doFirst {
        if (actionlintExecutable().isEmpty()) {
            throw GradleException(
                "actionlint is required to lint GitHub workflows. Install it from " +
                    "https://github.com/rhysd/actionlint (brew, go install, or a release binary)"
            )
        }
    }

    commandLine(actionlintExecutable(), *workflowFiles.files.map { it.absolutePath }.toTypedArray())
}

// Resolves the absolute path of the actionlint executable from the current PATH. The absolute path is used in the
// exec commandLine so it does not depend on the daemon JVM's cached PATH for native process spawning (frozen at JVM
// start, ignoring later PATH changes) — System.getenv("PATH") reflects the real shell PATH, so resolving the tool's
// absolute location from it works regardless of how the daemon was spawned. Returns an empty string if actionlint
// cannot be found.
fun actionlintExecutable(): String {
    val path = System.getenv("PATH") ?: return ""
    val executableNames =
        if (SystemUtils.IS_OS_WINDOWS) {
            listOf("actionlint.exe", "actionlint.bat", "actionlint.cmd")
        } else {
            listOf("actionlint")
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
