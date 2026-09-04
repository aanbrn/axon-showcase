import com.github.gradle.node.npm.task.NpmTask
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("base")
    id("com.github.node-gradle.node")
}

val libs = the<LibrariesForLibs>()

node {
    version.set(libs.versions.node.asProvider().get())
    download.set(true)
    npmInstallCommand.set("ci")
}

val npmCi =
    tasks.register<NpmTask>("npmCi") {
        description = "Installs the frontend dependencies from the lock file."
        args.set(listOf("ci"))
        inputs.files(
            file("package.json"),
            file("package-lock.json"),
        )
        outputs.dir(file("node_modules"))
    }

val npmBuild =
    tasks.register<NpmTask>("npmBuild") {
        group = "build"
        description = "Builds the production bundle into build/dist."
        dependsOn(npmCi)
        args.set(listOf("run", "build"))
        inputs.files(fileTree("src"))
        inputs.file("vite.config.ts")
        inputs.file("tsconfig.json")
        outputs.dir(layout.buildDirectory.dir("dist"))
    }

val npmLint =
    tasks.register<NpmTask>("npmLint") {
        group = "verification"
        description = "Runs the frontend linter."
        dependsOn(npmCi)
        args.set(listOf("run", "lint"))
        inputs.files(fileTree("src"))
    }

val npmFormatCheck =
    tasks.register<NpmTask>("npmFormatCheck") {
        group = "verification"
        description = "Checks the frontend formatting with Prettier."
        dependsOn(npmCi)
        args.set(listOf("run", "format:check"))
        inputs.files(fileTree("src"))
    }

val npmTest =
    tasks.register<NpmTask>("npmTest") {
        group = "verification"
        description = "Runs the frontend unit tests."
        dependsOn(npmCi)
        args.set(listOf("run", "test"))
        inputs.files(fileTree("src"))
        outputs.dir(layout.buildDirectory.dir("reports"))
    }

val npmDev =
    tasks.register<NpmTask>("viteDev") {
        group = "application"
        description = "Starts the Vite dev server (blocking; Ctrl+C to stop)."
        dependsOn(npmCi)
        args.set(listOf("run", "dev"))
    }

val npmE2e =
    tasks.register<NpmTask>("e2eTest") {
        group = "verification"
        description = "Runs the web UI end-to-end tests with Playwright against the real stack."
        dependsOn(npmBuild)
        dependsOn(rootProject.tasks.named("composeBuildAndUp"))
        finalizedBy(rootProject.tasks.named("composeDown"))
        args.set(listOf("run", "e2e"))
        inputs.files(fileTree("e2e"))
        inputs.file("playwright.config.ts")
    }

tasks.named("check") {
    dependsOn(npmLint, npmFormatCheck, npmTest)
}

tasks.named("assemble") {
    dependsOn(npmBuild)
}
