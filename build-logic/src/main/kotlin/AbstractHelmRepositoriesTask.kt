import io.github.build.extensions.oss.gradle.plugins.helm.command.tasks.AbstractHelmCommandTask

abstract class AbstractHelmRepositoriesTask : AbstractHelmCommandTask() {
    protected fun addHelmRepositories(repos: Map<String, String>, tolerateFailures: Boolean = false) {
        repos.forEach { (name, url) ->
            try {
                // execHelm submits asynchronously to the worker queue and returns immediately; use the
                // output-capturing variant so each setup step blocks until it completes, keeping repo add ->
                // repo update -> show values in order even on a fresh machine where the repos do not yet exist.
                execHelmCaptureOutput("repo", "add") {
                    args(name, url)
                }
                execHelmCaptureOutput("repo", "update") {
                    args(name)
                }
            } catch (e: Exception) {
                if (tolerateFailures) {
                    logger.warn("Helm repo '$name' ($url) is unreachable; its charts are reported as no-data")
                } else {
                    throw e
                }
            }
        }
    }
}