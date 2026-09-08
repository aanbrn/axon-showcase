import com.github.gradle.node.npm.task.NpmTask
import gradle.kotlin.dsl.accessors._31ffc96443a0302ceb6c1c60c45624ec.node
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

// The Procfile and start.sh are staged into the built bundle so the Paketo nginx/procfile buildpacks see them in the
// build context (--path build/dist), while they remain source files for the frontend module. A Copy (not Sync) so
// the Vite-built bundle in build/dist is preserved.
val stageImageFiles =
    tasks.register<Copy>("stageImageFiles") {
        description = "Stages Procfile and start.sh into the built bundle for the container image."
        dependsOn(npmBuild)
        into(layout.buildDirectory.dir("dist"))
        from("Procfile", "start.sh")
    }

// The dockerBuildImage task exposes bootBuildImage-style module-owned inputs (imageName, imagePlatform,
// environment) on the PackBuildImageTask type; the mechanism (builder, buildpacks, app dir, baked BP_* build
// settings) is provided here in the convention.
val dockerBuildImage =
    tasks.register<PackBuildImageTask>("dockerBuildImage") {
        group = "build"
        description = "Builds a container image from the built frontend with Paketo buildpacks."
        dependsOn(stageImageFiles)

        builder.set("paketobuildpacks/builder-jammy-base")
        buildpacks.set(
            listOf(
                "paketo-buildpacks/nginx",
                "paketo-buildpacks/procfile",
            )
        )
        environment.putAll(
            mapOf(
                "BP_WEB_SERVER" to "nginx",
                "BP_WEB_SERVER_ROOT" to "/workspace",
                "BP_NGINX_STUB_STATUS_PORT" to "9090",
            )
        )
        appDir.set(layout.buildDirectory.dir("dist"))
        inputs.file("Procfile")
        inputs.file("start.sh")
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

        // Mark the compose tasks this e2e depends on / finalizes with as scheduled, so the
        // docker-conventions onlyIf guard lets them run when reached as dependencies (not just when
        // requested by name). Set at configuration time because onlyIf for the dependency tasks is
        // evaluated before this task's actions run. This is explicit rather than a task-graph scan, which
        // can re-enter Gradle's scheduler on lazily configured tasks (e.g. the gateway e2e's
        // bootBuildImage deps). The key uses the compose task's own `path` (`:composeBuildAndUp`).
        rootProject.extra.set(
            "composeTaskScheduled-:composeBuildAndUp",
            true,
        )
        rootProject.extra.set(
            "composeTaskScheduled-:composeDown",
            true,
        )

        // composeBuildAndUp returns as soon as containers start (no --wait: a one-shot kafka-init exits,
        // which `--wait` treats as a failure). The gateway is the last service the e2e talks to, so poll
        // its health endpoint before Playwright runs.
        doFirst {
            val gatewayUrl = "http://localhost:8080/actuator/health"
            val deadline = System.currentTimeMillis() + 5 * 60 * 1000
            while (System.currentTimeMillis() < deadline) {
                val process = ProcessBuilder("curl", "-s", "-o", "/dev/null", "-w", "%{http_code}", gatewayUrl).start()
                val exit = process.waitFor(15, TimeUnit.SECONDS)
                val code = if (exit) process.inputStream.bufferedReader().readText().trim() else ""
                if (code == "200") {
                    return@doFirst
                }
                Thread.sleep(5 * 1000)
            }
            throw GradleException("The API gateway did not become healthy at $gatewayUrl within 5 minutes.")
        }
    }

tasks.named("check") {
    dependsOn(npmLint, npmFormatCheck, npmTest)
}

tasks.named("assemble") {
    dependsOn(npmBuild)
}
