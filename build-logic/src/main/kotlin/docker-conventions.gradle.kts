import java.io.File
import java.util.concurrent.locks.ReentrantLock
import org.apache.commons.lang3.SystemUtils
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.GradleException
import org.gradle.api.plugins.ExtraPropertiesExtension.UnknownPropertyException

val libs = the<LibrariesForLibs>()

val composeServices = if (project == rootProject) listOf<String>() else listOf(project.name)

fun registerComposeTask(
    taskName: String,
    action: List<String>,
    systemDescription: String,
    serviceDescription: String,
    buildFirst: Boolean = false,
) {
    tasks.register<Exec>(taskName) {
        group = "docker"
        description = if (project == rootProject) systemDescription else serviceDescription

        if (buildFirst) {
            dependsOn(allprojects.flatMap { project -> project.tasks.named { it == "bootBuildImage" } })
        }

        commandLine = listOf("docker", "compose") + action + composeServices

        workingDir = rootProject.layout.projectDirectory.asFile

        environment["PROJECT_VERSION"] = project.version
        environment["POSTGRES_VERSION"] = libs.versions.postgres.image.get()
        environment["OPENSEARCH_VERSION"] = libs.versions.opensearch.image.get()
        environment["KAFKA_VERSION"] = libs.versions.kafka.image.get()

        doFirst {
            if (!dockerCliOnPath()) {
                throw GradleException(
                    "The Docker CLI is required to run the compose tasks. Install Docker and make 'docker' available on PATH."
                )
            }
            dockerLock.lock()
        }

        doLast {
            dockerLock.unlock()
        }

        onlyIf {
            gradle.startParameter.taskRequests
                .flatMap { it.args }
                .any {
                    it ==
                        if (project == defaultProject) {
                            name
                        } else if (defaultProject == rootProject) {
                            project.path + ":" + name
                        } else {
                            project.path.removePrefix(defaultProject.path) + ":" + name
                        }
                }
        }
    }
}

registerComposeTask(
    "composeUp",
    listOf("up", "-d"),
    "Starts the system",
    "Starts the ${project.name} service",
)
registerComposeTask(
    "composeRestart",
    listOf("restart"),
    "Restarts the system",
    "Restarts the ${project.name} service",
)
registerComposeTask(
    "composeStop",
    listOf("stop"),
    "Stops the system",
    "Stops the ${project.name} service",
)
registerComposeTask(
    "composeDown",
    listOf("down"),
    "Stops and removes the system",
    "Stops and removes the ${project.name} service",
)
registerComposeTask(
    "composeBuildAndUp",
    listOf("up", "-d"),
    "Builds images and starts the system",
    "Builds an image and starts the ${project.name} service",
    buildFirst = true,
)
registerComposeTask(
    "composeBuildAndRestart",
    listOf("restart"),
    "Builds images and restarts the system",
    "Builds an image and restarts the ${project.name} service",
    buildFirst = true,
)

val dockerLock =
    synchronized(rootProject) {
        try {
            rootProject.extra.get("dockerLock") as ReentrantLock
        } catch (_: UnknownPropertyException) {
            val lock = ReentrantLock()
            rootProject.extra.set("dockerLock", lock)
            lock
        }
    }

val defaultProject =
    allprojects.find {
        it.layout.projectDirectory.asFile == gradle.startParameter.projectDir
    } ?: rootProject

fun dockerCliOnPath(): Boolean {
    val path = System.getenv("PATH") ?: return false
    val executableNames =
        if (SystemUtils.IS_OS_WINDOWS) {
            listOf("docker.exe")
        } else {
            listOf("docker")
        }
    return path.split(File.pathSeparator).any { dir ->
        executableNames.any { name ->
            val executable = File(dir, name)
            executable.isFile && executable.canExecute()
        }
    }
}