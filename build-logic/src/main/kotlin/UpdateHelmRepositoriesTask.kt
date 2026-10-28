import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Adds and refreshes the given Helm repositories, retrying the per-repository add/update pair.
 *
 * <p>The retry is the shared [BoundedRetry] budget: a transient failure is attempted again before it fails the build,
 * and a repository that stays unreachable still fails it.
 */
abstract class UpdateHelmRepositoriesTask : AbstractHelmRepositoriesTask() {

    /** The repositories to register and refresh, by name. */
    @get:Input abstract val repos: MapProperty<String, String>

    /** Registers and refreshes each repository. */
    @TaskAction
    fun update() {
        addHelmRepositories(repos.get())
    }
}
