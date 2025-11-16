import org.gradle.accessors.dm.LibrariesForLibs
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("java-conventions")
    id("org.springframework.boot")
    id("docker-conventions")
}

val libs = the<LibrariesForLibs>()

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.add("-Amapstruct.defaultComponentModel=spring")
}

tasks.named<BootBuildImage>("bootBuildImage") {
    if (project.hasProperty("imagePlatform")) {
        imagePlatform = project.property("imagePlatform") as String
    }

    buildpacks.addAll(
        listOf(
            "paketo-buildpacks/java",
            "paketo-buildpacks/spring-boot",
            "paketobuildpacks/health-checker"
        )
    )

    environment.putAll(
        mapOf(
            "BP_JVM_VERSION" to libs.versions.java.get(),
            "BP_HEALTH_CHECKER_ENABLED" to "true"
        )
    )
}
