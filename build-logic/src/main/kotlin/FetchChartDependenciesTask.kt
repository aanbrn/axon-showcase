import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Adds the chart's repository and fetches its external chart dependencies, retrying each network step.
 *
 * <p>The retry is the shared [BoundedRetry] budget: the step is attempted [attempts] times in total before its failure
 * fails the build, and a persistent one still fails it.
 */
abstract class FetchChartDependenciesTask : AbstractHelmRepositoriesTask() {

    /** The chart's dependency descriptor, whose content decides the fetched dependencies. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dependencyDescriptorFile: RegularFileProperty

    /** The chart repositories to register and refresh, by name. */
    @get:Input abstract val repos: MapProperty<String, String>

    /** The attempts each network step makes before its failure is reported. */
    @get:Input abstract val attempts: Property<Int>

    /** The collected chart directory the dependency fetch updates. */
    @get:Internal abstract val chartDir: DirectoryProperty

    /** The lock file the dependency fetch writes. */
    @get:OutputFile abstract val lockFile: RegularFileProperty

    /** The directory holding the fetched sub-charts. */
    @get:OutputDirectory abstract val subchartsDir: DirectoryProperty

    /** Registers and refreshes the repositories, then fetches the chart's dependencies. */
    @TaskAction
    fun fetch() {
        addHelmRepositories(repos.get(), attempts = attempts.get())
        BoundedRetry.execute(
            attempts = attempts.get(),
            onTransientFailure = { failure ->
                logger.warn("Chart dependency fetch failed; retrying: ${failure.message}")
            },
        ) {
            execHelmCaptureOutput("dependency", "update") {
                args(chartDir)
                flag("--skip-refresh", true)
            }
        }
    }

    init {
        attempts.convention(BoundedRetry.DEFAULT_ATTEMPTS)
    }
}
