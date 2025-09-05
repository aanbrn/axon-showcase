import org.apache.commons.lang3.SystemUtils
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.plugins.ExtraPropertiesExtension.UnknownPropertyException
import java.util.concurrent.locks.ReentrantLock

val libs = the<LibrariesForLibs>()

fun dockerCli(command: String): List<String> {
    return if (SystemUtils.IS_OS_WINDOWS) {
        listOf("cmd.exe", "/c", "docker.exe $command")
    } else {
        listOf("/bin/sh", "-c", "docker $command")
    }
}

tasks.register<Exec>("composeBuildAndUp") {
    group = "docker"
    description =
        if (project == rootProject) {
            "Builds images and starts the system"
        } else {
            "Builds an image and starts the ${project.name} service"
        }

    dependsOn(
        allprojects.flatMap { project -> project.tasks.named { it == "bootBuildImage" } }
    )

    commandLine = dockerCli(
        if (project == rootProject) {
            "compose up -d"
        } else {
            "compose up -d ${project.name}"
        }
    )
}

tasks.register<Exec>("composeBuildAndRestart") {
    group = "docker"
    description =
        if (project == rootProject) {
            "Builds images and restarts the system"
        } else {
            "Builds an image and restarts the ${project.name} service"
        }

    dependsOn(
        allprojects.flatMap { project -> project.tasks.named { it == "bootBuildImage" } }
    )

    commandLine = dockerCli(
        if (project == rootProject) {
            "compose restart"
        } else {
            "compose restart ${project.name}"
        }
    )
}

tasks.register<Exec>("composeUp") {
    group = "docker"
    description =
        if (project == rootProject) {
            "Starts the system"
        } else {
            "Starts the ${project.name} service"
        }

    commandLine = dockerCli(
        if (project == rootProject) {
            "compose up -d"
        } else {
            "compose up -d ${project.name}"
        }
    )
}

tasks.register<Exec>("composeRestart") {
    group = "docker"
    description =
        if (project == rootProject) {
            "Restarts the system"
        } else {
            "Restarts the ${project.name} service"
        }

    commandLine = dockerCli(
        if (project == rootProject) {
            "compose restart"
        } else {
            "compose restart ${project.name}"
        }
    )
}

tasks.register<Exec>("composeStop") {
    group = "docker"
    description =
        if (project == rootProject) {
            "Stops the system"
        } else {
            "Stops the ${project.name} service"
        }

    commandLine = dockerCli(
        if (project == rootProject) {
            "compose stop"
        } else {
            "compose stop ${project.name}"
        }
    )
}

tasks.register<Exec>("composeDown") {
    group = "docker"
    description =
        if (project == rootProject) {
            "Stops and removes the system"
        } else {
            "Stops and removes the ${project.name} service"
        }

    commandLine = dockerCli(
        if (project == rootProject) {
            "compose down"
        } else {
            "compose down ${project.name}"
        }
    )
}

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

val defaultProject = allprojects.find {
    it.layout.projectDirectory.asFile == gradle.startParameter.projectDir
} ?: rootProject

tasks.withType<Exec>().matching { task ->
    task.name.startsWith("compose")
}.configureEach {
    environment["PROJECT_VERSION"] = project.version
    environment["POSTGRES_VERSION"] = libs.versions.postgres.image.get()
    environment["OPENSEARCH_VERSION"] = libs.versions.opensearch.image.get()
    environment["KAFKA_VERSION"] = libs.versions.kafka.image.get()
    workingDir = rootProject.layout.projectDirectory.asFile

    doFirst {
        dockerLock.lock()
    }

    doLast {
        dockerLock.unlock()
    }

    onlyIf {
        gradle.startParameter.taskRequests.flatMap { it.args }.any {
            it == if (project == defaultProject) {
                name
            } else if (defaultProject == rootProject) {
                project.path + ":" + name
            } else {
                project.path.removePrefix(defaultProject.path) + ":" + name
            }
        }
    }
}
