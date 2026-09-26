import org.gradle.api.tasks.JavaExec

plugins {
    id("java-conventions")
    id("io.gatling.gradle")
}

val loadTestProperties = listOf("baseUrl", "profile", "rate", "ratio", "duration", "sseConnections")

gatling {
    includes = emptyList()
    excludes = emptyList()
    systemProperties =
        loadTestProperties.mapNotNull { name -> providers.gradleProperty(name).orNull?.let { name to it } }.toMap()
}

val gatlingSourceSet = extensions.getByType(SourceSetContainer::class.java)["gatling"]
val gatlingExtension = extensions.getByType(io.gatling.gradle.GatlingPluginExtension::class.java)

tasks.register<JavaExec>("kneeFinder") {
    group = "Gatling"
    description = "Derives the load knee from a Gatling simulation log"
    classpath = gatlingSourceSet.runtimeClasspath
    mainClass.set("showcase.loadtests.KneeFinder")
    jvmArgs(gatlingExtension.jvmArgs)
    argumentProviders.add {
        listOf(providers.gradleProperty("log").get(), providers.gradleProperty("knee").get())
    }
}
