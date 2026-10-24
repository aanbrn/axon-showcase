import org.gradle.accessors.dm.LibrariesForLibs
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("java-conventions")
    id("org.springframework.boot")
    id("docker-conventions")
}

val libs = the<LibrariesForLibs>()

springBoot {
    buildInfo {
        excludes.set(listOf("time"))
    }
}

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.add("-Amapstruct.defaultComponentModel=spring")
}

tasks.named<BootBuildImage>("bootBuildImage") {
    builder.set("paketobuildpacks/builder-noble-java-tiny:" + libs.versions.paketo.builder.noble.java.tiny.get())

    buildpacks.addAll(
        listOf(
            "paketo-buildpacks/java",
            "paketo-buildpacks/spring-boot",
            "docker://paketobuildpacks/health-checker:" + libs.versions.paketo.health.checker.get(),
        ),
    )

    environment.putAll(
        mapOf(
            "BP_JVM_VERSION" to libs.versions.java.get(),
            "BP_HEALTH_CHECKER_ENABLED" to "true",
        ),
    )
}
