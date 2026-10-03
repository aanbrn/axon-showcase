import com.github.gradle.node.npm.task.NpmTask
import gradle.kotlin.dsl.accessors._31ffc96443a0302ceb6c1c60c45624ec.node
import java.math.BigDecimal
import java.util.Properties
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
        inputs.file("index.html")
        inputs.file("vite.config.ts")
        inputs.file("tsconfig.json")
        inputs.file("package.json")
        inputs.file("package-lock.json")
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

val npmFormat =
    tasks.register<NpmTask>("npmFormat") {
        group = "verification"
        description = "Formats the frontend sources with Prettier."
        dependsOn(npmCi)
        args.set(listOf("run", "format"))
        inputs.files(fileTree("src"))
    }

val npmTest =
    tasks.register<NpmTask>("npmTest") {
        group = "verification"
        description = "Runs the frontend unit tests without coverage (standalone; `check` runs `npmTestCoverage`)."
        dependsOn(npmCi)
        args.set(listOf("run", "test"))
        inputs.files(fileTree("src"))
        inputs.file("eslint.config.js")
        inputs.file("vite.config.ts")
        inputs.file("tsconfig.json")
        inputs.file("package.json")
        inputs.file("package-lock.json")
        outputs.dir(layout.buildDirectory.dir("reports"))
    }

val npmTestCoverage =
    tasks.register<NpmTask>("npmTestCoverage") {
        group = "verification"
        description = "Runs the frontend unit tests with coverage and enforces the committed statement minimum."
        dependsOn(npmCi)
        args.set(listOf("run", "test:coverage"))
        inputs.files(fileTree("src"))
        inputs.file("eslint.config.js")
        inputs.file("vite.config.ts")
        inputs.file("tsconfig.json")
        inputs.file("package.json")
        inputs.file("package-lock.json")
        inputs.file(rootProject.layout.projectDirectory.file("config/web-ui-coverage/coverage-baseline.properties"))
        // The committed baseline (a fraction, mirroring the JVM baseline) is injected as the environment variable the
        // config reads; the config scales it to Vitest's percentage thresholds.
        environment.put(
            "VITEST_COVERAGE_MIN",
            coverageStatementsMinimum().toPlainString(),
        )
        outputs.dir(layout.buildDirectory.dir("coverage"))
    }

/** The frontend statement-coverage minimum, read from the committed baseline file (fraction form, e.g. 0.80). */
fun coverageStatementsMinimum(): BigDecimal {
    val file = rootProject.layout.projectDirectory.file("config/web-ui-coverage/coverage-baseline.properties").asFile
    val properties = Properties()
    if (file.exists()) {
        file.inputStream().use { properties.load(it) }
    }
    return properties.getProperty("coverage.statements.minimum", "0.80").toBigDecimal()
}

val npmTypeCheck =
    tasks.register<NpmTask>("npmTypeCheck") {
        group = "verification"
        description = "Type-checks the frontend sources with tsc."
        dependsOn(npmCi)
        args.set(listOf("run", "typecheck"))
        inputs.files(fileTree("src"))
        inputs.file("tsconfig.json")
    }

/** The web UI packages whose major updates the `npmOutdated` report suppresses, read from the repo's config list. */
fun npmMajorDisabled(): Set<String> {
    val file = rootProject.layout.projectDirectory.file("config/web-ui-updates/major-disabled.txt").asFile
    return if (file.exists()) NpmOutdatedRules.suppressedEntries(file.readLines()) else emptySet()
}

val npmOutdated =
    tasks.register<NpmTask>("npmOutdated") {
        group = "help"
        description =
            "Reports the web UI's outdated npm dependencies, major-suppressed packages removed " +
                "(writes build/npm-outdated.txt)."
        dependsOn(npmCi)
        args.set(listOf("run", "outdated:report"))
        inputs.file(rootProject.layout.projectDirectory.file("config/web-ui-updates/major-disabled.txt"))

        doLast {
            val raw = layout.buildDirectory.file("npm-outdated-raw.txt").get().asFile
            val report = layout.buildDirectory.file("npm-outdated.txt").get().asFile
            val error = layout.buildDirectory.file("npm-outdated.err.txt").get().asFile
            val exit = layout.buildDirectory.file("npm-outdated.exit").get().asFile
            if (raw.exists()) {
                report.writeText(NpmOutdatedRules.filterReport(raw.readLines(), npmMajorDisabled()))
            }
            val updates = report.takeIf { it.exists() }?.readText().orEmpty()
            if (updates.isNotBlank()) {
                logger.lifecycle("\nWeb UI dependency updates:\n$updates")
            } else if (exit.takeIf { it.exists() }?.readText()?.trim() != "0") {
                logger.error(
                    "Web UI dependency update report failed:\n" + error.takeIf { it.exists() }?.readText().orEmpty()
                )
            } else {
                logger.lifecycle("\nNo web UI dependency updates available.")
            }
        }
    }

val npmAudit =
    tasks.register<NpmTask>("npmAudit") {
        group = "verification"
        description = "Audits the web UI's npm dependencies, failing on high-severity vulnerabilities."
        dependsOn(npmCi)
        args.set(listOf("audit", "--audit-level=high"))
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

        builder.set("paketobuildpacks/builder-jammy-base:" + libs.versions.paketo.builder.jammy.base.get())
        buildpacks.set(
            listOf(
                "paketo-buildpacks/nginx@" + libs.versions.paketo.nginx.get(),
                "paketo-buildpacks/procfile@" + libs.versions.paketo.procfile.get(),
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
    dependsOn(npmLint, npmFormatCheck, npmTypeCheck, npmTestCoverage)
}

tasks.named("assemble") {
    dependsOn(npmBuild)
}
