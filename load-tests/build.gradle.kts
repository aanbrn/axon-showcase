import org.gradle.api.tasks.JavaExec

plugins {
    id("load-testing-conventions")
}

val loadTestProperties =
    listOf(
        "baseUrl",
        "profile",
        "rate",
        "ratio",
        "duration",
        "sseConnections",
        "kneeRate",
        "thinkTime",
        "detailShare",
        "startShare",
        "finishShare",
        "hold",
        "baselineFile",
    )

gatling {
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

tasks.register<JavaExec>("baselineStats") {
    group = "Gatling"
    description = "Records the baseline reference from a Gatling simulation log"
    classpath = gatlingSourceSet.runtimeClasspath
    mainClass.set("showcase.loadtests.BaselineStats")
    jvmArgs(gatlingExtension.jvmArgs)
    argumentProviders.add {
        listOf(
            providers.gradleProperty("log").get(),
            providers.gradleProperty("baselineOut").get(),
            providers.gradleProperty("target").get(),
        )
    }
}

dependencies {
    gatling(platform(project(":platform")))

    gatlingImplementation(testFixtures(project(":showcase-command-api")))

    gatlingCompileOnly(libs.lombok)

    gatlingAnnotationProcessor(libs.lombok)
}
