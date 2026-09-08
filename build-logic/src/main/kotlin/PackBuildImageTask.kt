import java.io.File
import javax.inject.Inject
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.process.ExecOperations

/**
 * A task that builds a container image from the built frontend using Cloud Native Buildpacks via the `pack` CLI.
 *
 * <p>Exposes module-owned inputs (`imageName`, `imagePlatform`, `environment`) like Spring Boot's `bootBuildImage`,
 * while the convention plugin supplies the mechanism (builder, buildpacks, app directory, and any baked defaults).
 */
abstract class PackBuildImageTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    /** The image name and tag; defaults to `${project.name}:${project.version}`, overridable by the module. */
    @get:Input
    abstract val imageName: Property<String>

    // Captured at configuration time: Task.project is deprecated at execution time (fails in Gradle 10).
    private val projectDir = project.projectDir

    init {
        imageName.convention(project.provider { "${project.name}:${project.version}" })
    }

    /** Optional target platform, e.g. `linux/amd64` (passed through to `pack build --platform`). */
    @get:Input
    @get:Optional
    @get:Option(option = "imagePlatform", description = "The target platform (os/architecture/variant) for the image")
    abstract val imagePlatform: Property<String>

    /** The CNB builder image. */
    @get:Input
    abstract val builder: Property<String>

    /** The buildpacks to apply. */
    @get:Input
    abstract val buildpacks: ListProperty<String>

    /** Build-time and launch-time environment (`BP_*` and `BPE_DEFAULT_*`), passed as `--env KEY=VALUE`. */
    @get:Input
    abstract val environment: MapProperty<String, String>

    /** The built application directory passed to `pack build --path`. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val appDir: DirectoryProperty

    @TaskAction
    fun buildImage() {
        val packCli = packCli()
        if (packCli.isEmpty()) {
            throw GradleException(
                "The pack CLI is required to build the container image. Install it (" + installHint() +
                    ") and make 'pack' available on PATH.",
            )
        }
        val args =
            mutableListOf(
                packCli,
                "build",
                imageName.get(),
                "--path",
                appDir.get().asFile.absolutePath,
                "--builder",
                builder.get(),
            )
        buildpacks.get().forEach { args += listOf("--buildpack", it) }
        environment.get().forEach { (name, value) -> args += listOf("--env", "$name=$value") }
        imagePlatform.orNull?.let { args += listOf("--platform", it) }
        execOperations.exec {
            commandLine(args)
            workingDir(projectDir)
        }
    }

    private fun installHint(): String =
        "see https://buildpacks.io/docs/for-platform-operators/how-to/integrate-ci/pack/"

    // Resolves the absolute path of the pack executable from the current PATH. The absolute path is used in the exec
    // commandLine so it does not depend on the daemon JVM's cached PATH for native process spawning (frozen at JVM
    // start, ignoring later PATH changes) — System.getenv("PATH") reflects the real shell PATH, so resolving the
    // tool's absolute location from it works regardless of how the daemon was spawned. Returns an empty string if
    // pack cannot be found.
    private fun packCli(): String {
        val path = System.getenv("PATH") ?: return ""
        val executableNames =
            if (SystemUtils.IS_OS_WINDOWS) {
                listOf("pack.exe")
            } else {
                listOf("pack")
            }
        return path.split(File.pathSeparator).firstNotNullOfOrNull { dir ->
            executableNames.firstOrNull { name ->
                val executable = File(dir, name)
                executable.isFile && executable.canExecute()
            }?.let { File(dir, it).absolutePath }
        } ?: ""
    }
}
