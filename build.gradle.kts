import io.github.build.extensions.oss.gradle.plugins.helm.dsl.HelmRepository
import io.github.build.extensions.oss.gradle.plugins.helm.release.dsl.HelmRelease
import java.util.Properties
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer

plugins {
    id("dependency-security-conventions")
    id("dependency-versions-conventions")
    id("docker-conventions")
    id("helm-releases-conventions")
    alias(libs.plugins.spotless)
    jacoco
    id("workflow-lint-conventions")
}

spotless {
    format("markdown") {
        target(
            "docs/**/*.md",
            "AGENTS.md",
            "README.md",
            "openspec/specs/**/*.md",
            "openspec/changes/**/*.md",
            ".opencode/agent/**/*.md",
            ".opencode/commands/**/*.md",
            ".opencode/skills/**/*.md",
        )
        targetExclude(
            "openspec/changes/archive/**",
            ".opencode/commands/opsx-apply.md",
            ".opencode/commands/opsx-archive.md",
            ".opencode/commands/opsx-explore.md",
            ".opencode/commands/opsx-propose.md",
            ".opencode/commands/opsx-sync.md",
            ".opencode/commands/opsx-update.md",
            ".opencode/skills/axon4to5-*/**",
            ".opencode/skills/openspec-*/**",
        )
        prettier("3.9.6")
            .config(
                mapOf(
                    "printWidth" to 120,
                    "proseWrap" to "always",
                    "singleQuote" to true,
                    "trailingComma" to "all",
                    "semi" to true,
                )
            )
    }

    format("json") {
        target(".opencode/opencode.json")
        prettier("3.9.6").config(mapOf("printWidth" to 120))
    }

    kotlinGradle {
        target("*.gradle.kts", "build-logic/*.gradle.kts", "build-logic/src/**/*.gradle.kts")
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
    }

    kotlin {
        target("build-logic/src/**/*.kt")
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
    }
}

allprojects {
    group = "com.github.aanbrn"
    version = "0.1.0-SNAPSHOT"

    configurations.configureEach {
        resolutionStrategy {
            dependencySubstitution {
                substitute(module("org.lz4:lz4-java"))
                    .using(module(libs.lz4.java.get().toString()))
                    .because("Force relocation of LZ4 implementation")
            }

            eachDependency {
                if (requested.group == "org.lz4" && requested.name == "lz4-java") {
                    useTarget(libs.lz4.java.get().toString())
                    because("Force relocation of LZ4 implementation")
                }
            }
        }
    }
}

tasks.register<JacocoReport>("jacocoRootReport") {
    description = "Aggregates JaCoCo coverage across all modules that apply code-coverage-conventions"

    val covered = subprojects.filter { it.plugins.hasPlugin("code-coverage-conventions") }

    dependsOn(covered.map { "${it.path}:jacocoTestReport" })

    executionData.setFrom(
        covered.map { it.layout.buildDirectory.dir("jacoco").get().asFileTree.matching { include("*.exec") } }
    )
    sourceDirectories.setFrom(
        covered.map { it.extensions.getByType<SourceSetContainer>().named("main").get().allSource.srcDirs }
    )
    classDirectories.setFrom(
        covered.map {
            it.extensions.getByType<SourceSetContainer>().named("main").get().output.classesDirs.asFileTree.matching {
                exclude(
                    "**/*Proto.class",
                    "**/*OrBuilder.class",
                    "**/*OuterClass.class",
                    "**/*Grpc.class",
                )
            }
        }
    )

    reports {
        html.required = true
        xml.required = true
    }
}

val infraChartSpecs =
    mapOf(
        "bitnami/postgresql" to ("postgres" to libs.versions.postgres.image.tag),
        "bitnami/kafka" to ("kafka" to libs.versions.kafka.image.tag),
        "bitnami/opensearch" to ("opensearch" to libs.versions.opensearch.image.tag),
    )

val helmRepoUrls = providers.provider {
    val helmExtension = project.extensions.getByName("helm") as ExtensionAware
    @Suppress("UNCHECKED_CAST")
    val repositories = helmExtension.extensions.getByName("repositories") as NamedDomainObjectContainer<HelmRepository>
    repositories.associate { repo ->
        repo.name to repo.url.get().toString()
    }
}

val infraChecks = providers.provider {
    val helmExtension = project.extensions.getByName("helm") as ExtensionAware
    @Suppress("UNCHECKED_CAST")
    val releases = helmExtension.extensions.getByName("releases") as NamedDomainObjectContainer<HelmRelease>
    releases.mapNotNull { release ->
        val chartRef = release.chart.map { it.chartLocation }.getOrNull()
        val spec = chartRef?.let { infraChartSpecs[it] }
        if (chartRef == null || spec == null) {
            null
        } else {
            InfraImageVersionCheck(
                component = spec.first,
                chartRef = chartRef,
                chartVersion = release.version.get(),
                imageTag = spec.second.get(),
                valuesDirs =
                    release.valuesDirs.get().map { dir ->
                        rootProject.layout.projectDirectory.asFile.toPath().relativize(dir.toPath()).toString()
                    },
            )
        }
    }
}

tasks.register("verifyInfraImageVersions", VerifyInfraImageVersionsTask::class.java) {
    group = "verification"
    description = "Verifies each infra image tag matches the image tag preconfigured in its pinned Bitnami Helm chart"

    checks.set(infraChecks)

    repos.set(helmRepoUrls)

    valuesFiles.from(
        infraChecks.map { checks ->
            checks
                .flatMap { it.valuesDirs }
                .map { valuesDir ->
                    fileTree(rootProject.layout.projectDirectory.dir(valuesDir)) { include("values*.yaml") }
                }
        }
    )

    resultFile.set(layout.buildDirectory.file("verification/infra-image-versions.txt"))
}

val helmChartChecks =
    listOf(
        HelmChartUpdateCheck(
            name = "bitnami-common",
            chartRef = "bitnami/common",
            pinnedVersion = libs.versions.bitnami.common.get(),
            repo = "bitnami",
        ),
        HelmChartUpdateCheck(
            name = "bitnami-postgresql",
            chartRef = "bitnami/postgresql",
            pinnedVersion = libs.versions.bitnami.postgresql.get(),
            repo = "bitnami",
        ),
        HelmChartUpdateCheck(
            name = "bitnami-kafka",
            chartRef = "bitnami/kafka",
            pinnedVersion = libs.versions.bitnami.kafka.get(),
            repo = "bitnami",
        ),
        HelmChartUpdateCheck(
            name = "bitnami-opensearch",
            chartRef = "bitnami/opensearch",
            pinnedVersion = libs.versions.bitnami.opensearch.get(),
            repo = "bitnami",
        ),
        HelmChartUpdateCheck(
            name = "prometheus-community-stack",
            chartRef = "prometheus-community/kube-prometheus-stack",
            pinnedVersion = libs.versions.prometheus.community.stack.get(),
            repo = "prometheus-community",
        ),
        HelmChartUpdateCheck(
            name = "grafana-tempo",
            chartRef = "grafana/tempo",
            pinnedVersion = libs.versions.grafana.tempo.get(),
            repo = "grafana",
        ),
    )

tasks.register("helmUpdates", HelmUpdatesTask::class.java) {
    group = "help"
    description = "Displays the Helm CLI and chart updates for the project."

    helmCliVersion.set(libs.versions.helm.asProvider().get())

    chartChecks.set(helmChartChecks)

    repoUrls.set(helmRepoUrls)

    majorDisabled.set(
        providers.provider {
            val file = rootProject.layout.projectDirectory.file("config/helm-updates/major-disabled.properties")
            if (file.asFile.exists()) {
                Properties()
                    .apply { file.asFile.inputStream().use { load(it) } }
                    .stringPropertyNames()
                    .filter { it.isNotBlank() }
            } else {
                emptyList()
            }
        }
    )

    reportFile.set(layout.buildDirectory.file("helm-updates/report.txt"))

    outputs.upToDateWhen { false }
}

tasks.register("buildpackUpdates", BuildpackUpdatesTask::class.java) {
    group = "help"
    description = "Displays the Paketo builder and buildpack updates for the project."

    checks.set(
        listOf(
            BuildpackUpdateCheck(
                name = "paketo-builder-jammy-base",
                repository = "paketobuildpacks/builder-jammy-base",
                pinnedVersion = libs.versions.paketo.builder.jammy.base.get(),
            ),
            BuildpackUpdateCheck(
                name = "paketo-nginx",
                repository = "paketobuildpacks/nginx",
                pinnedVersion = libs.versions.paketo.nginx.get(),
            ),
            BuildpackUpdateCheck(
                name = "paketo-procfile",
                repository = "paketobuildpacks/procfile",
                pinnedVersion = libs.versions.paketo.procfile.get(),
            ),
        )
    )

    reportFile.set(layout.buildDirectory.file("buildpack-updates/report.txt"))

    outputs.upToDateWhen { false }
}

tasks.register("toolingUpdates", ToolingUpdatesTask::class.java) {
    group = "help"
    description = "Displays the tool versions pinned in workflow files that have a newer release."

    checks.set(
        listOf(
            ToolingUpdateCheck(
                name = "openspec-cli",
                workflowFile = "ci.yml",
                pinPattern = "@fission-ai/openspec@([0-9][^\\s]*)",
                source = ToolingVersionSource.NPM_LATEST,
                sourceRef = "@fission-ai/openspec",
            ),
            ToolingUpdateCheck(
                name = "snyk-cli",
                workflowFile = "snyk.yml",
                pinPattern = "snyk-version:\\s*(v?[0-9][^\\s]*)",
                source = ToolingVersionSource.GITHUB_RELEASE,
                sourceRef = "snyk/cli",
            ),
            ToolingUpdateCheck(
                name = "pack-cli",
                workflowFile = "e2e.yml",
                pinPattern = "pack-version:\\s*(v?[0-9][^\\s]*)",
                source = ToolingVersionSource.GITHUB_RELEASE,
                sourceRef = "buildpacks/pack",
            ),
        )
    )

    pinFiles.from(
        layout.projectDirectory.file(".github/workflows/ci.yml"),
        layout.projectDirectory.file(".github/workflows/snyk.yml"),
        layout.projectDirectory.file(".github/workflows/e2e.yml"),
    )

    reportFile.set(layout.buildDirectory.file("tooling-updates/report.txt"))

    outputs.upToDateWhen { false }
}

tasks.register("verifyModuleDependencies", VerifyModuleDependenciesTask::class.java) {
    group = "verification"
    description = "Verifies the modules' declared dependencies against the sanctioned module graph"

    resultFile.set(layout.buildDirectory.file("verification/module-dependencies.txt"))

    // The production source sets of every module: `main`, which is what ships, and
    // `testFixtures`, which other modules consume as an artifact. Both are declared directly in each build script, so
    // their configurations always hold their dependencies — unlike a test suite's, which are populated only once that
    // suite's test tasks are realized, making a walk over them report a different graph depending on which task graph
    // ran. Test suites are deliberately out of scope: the graph this enforces is the one that ships, and a suite
    // depending on a service application is a legitimate way to exercise that service.
    edges.set(
        provider {
            allprojects
                .filter { it != rootProject }
                .flatMap { project ->
                    val sourceSets =
                        project.extensions.findByType(SourceSetContainer::class.java) ?: return@flatMap emptyList()
                    listOf("main", "testFixtures")
                        .mapNotNull { sourceSets.findByName(it) }
                        .flatMap { sourceSet ->
                            // Every declaration configuration the source set exposes, so a dependency cannot escape
                            // by being declared runtimeOnly or compileOnlyApi.
                            listOf(
                                    sourceSet.apiConfigurationName,
                                    sourceSet.implementationConfigurationName,
                                    sourceSet.compileOnlyConfigurationName,
                                    sourceSet.compileOnlyApiConfigurationName,
                                    sourceSet.runtimeOnlyConfigurationName,
                                    sourceSet.annotationProcessorConfigurationName,
                                )
                                .distinct()
                        }
                        .mapNotNull { project.configurations.findByName(it) }
                        .flatMap { configuration ->
                            configuration.dependencies.withType(ProjectDependency::class.java).mapNotNull { dependency
                                ->
                                val target = dependency.path.removePrefix(":").substringAfterLast(":")
                                if (target == "platform") null else ModuleEdge(project.name, target)
                            }
                        }
                }
        }
    )
}

tasks.named("check") {
    dependsOn("verifyInfraImageVersions")
    dependsOn("workflowLint")
    dependsOn("verifyModuleDependencies")
    // build-logic is an included build, so its tests are not reached by this project's check.
    dependsOn(gradle.includedBuild("build-logic").task(":test"))
}

helm {
    releases {
        all {
            valuesDir("helm/values/$name")

            wait = true
            waitForJobs = true

            test { enabled = false }
        }

        create("kps") {
            from("prometheus-community/kube-prometheus-stack")

            version = libs.versions.prometheus.community.stack

            namespace = "monitoring"
            createNamespace = true

            tags.add("monitoring")

            mustUninstallAfter(
                "tempo",
                "axon-showcase-db-events",
                "axon-showcase-kafka",
                "axon-showcase-os-views",
                "axon-showcase",
            )
        }

        create("tempo") {
            from("grafana/tempo")

            version = libs.versions.grafana.tempo

            namespace = "monitoring"
            createNamespace = true

            tags.add("monitoring")

            mustInstallAfter("kps")

            mustUninstallAfter("axon-showcase")
        }

        create("axon-showcase-db-events") {
            from("bitnami/postgresql")

            version = libs.versions.bitnami.postgresql

            namespace = "axon-showcase"
            createNamespace = true

            tags.addAll(listOf("database", "db-events"))

            mustInstallAfter("kps")

            mustUninstallAfter("axon-showcase")
        }

        create("axon-showcase-os-views") {
            from("bitnami/opensearch")

            version = libs.versions.bitnami.opensearch

            namespace = "axon-showcase"
            createNamespace = true

            tags.addAll(listOf("database", "os-views"))

            mustInstallAfter("kps")

            mustUninstallAfter("axon-showcase")
        }

        create("axon-showcase-kafka") {
            from("bitnami/kafka")

            version = libs.versions.bitnami.kafka

            namespace = "axon-showcase"
            createNamespace = true

            tags.add("kafka")

            mustInstallAfter("kps")

            mustUninstallAfter("axon-showcase")
        }

        create("axon-showcase") {
            from(chart(":helm:chart", "main"))

            namespace = "axon-showcase"
            createNamespace = true

            tags.add("application")

            installDependsOn(
                ":showcase-command-service:bootBuildImage",
                ":showcase-projection-service:bootBuildImage",
                ":showcase-query-service:bootBuildImage",
                ":showcase-api-gateway:bootBuildImage",
                ":showcase-web-ui:dockerBuildImage",
            )

            mustInstallAfter(
                "kps",
                "tempo",
                "axon-showcase-db-events",
                "axon-showcase-kafka",
                "axon-showcase-os-views",
            )
        }
    }

    releaseTargets {
        create("local") {
            selectTags = "*"
            val localKubeContext = providers.gradleProperty("helm.local.kubeContext")
            if (localKubeContext.isPresent) {
                kubeContext.set(localKubeContext)
            }
        }
    }
}
