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

tasks.named("gatlingRecorder") {
    notCompatibleWithConfigurationCache(
        "The Gatling plugin's task holds a Configuration and a SourceSet, which the configuration cache cannot serialize",
    )
}

tasks.register<JavaExec>("kneeFinder") {
    group = "Gatling"
    description = "Derives the load knee from a Gatling simulation log"
    classpath = gatlingSourceSet.runtimeClasspath
    mainClass.set("showcase.loadtests.KneeFinder")
    jvmArgs(gatlingExtension.jvmArgs)
    val logProperty = providers.gradleProperty("log")
    val kneeProperty = providers.gradleProperty("knee")
    argumentProviders.add {
        listOf(logProperty.get(), kneeProperty.get())
    }
}

tasks.register<JavaExec>("baselineStats") {
    group = "Gatling"
    description = "Compares a Gatling log against the recorded baseline reference, then writes or withholds it"
    classpath = gatlingSourceSet.runtimeClasspath
    mainClass.set("showcase.loadtests.BaselineStats")
    jvmArgs(gatlingExtension.jvmArgs)
    val logProperty = providers.gradleProperty("log")
    val baselineOutProperty = providers.gradleProperty("baselineOut")
    val targetProperty = providers.gradleProperty("target")
    val refreshBaselineProperty = providers.gradleProperty("refreshBaseline")
    val tolerancePercentProperty = providers.gradleProperty("tolerancePercent")
    val operatingPointProperty = providers.gradleProperty("operatingPoint")
    argumentProviders.add {
        listOf(
            logProperty.get(),
            baselineOutProperty.get(),
            targetProperty.get(),
            refreshBaselineProperty.getOrElse("false"),
            tolerancePercentProperty.getOrElse("50"),
            operatingPointProperty.get(),
        )
    }
}

tasks.register<JavaExec>("baselineTrend") {
    group = "Gatling"
    description = "Reports the dated load-test records as a chronological series"
    classpath = gatlingSourceSet.runtimeClasspath
    mainClass.set("showcase.loadtests.BaselineTrend")
    jvmArgs(gatlingExtension.jvmArgs)
    val defaultRecordsDir = rootProject.layout.projectDirectory.dir("docs/load-tests").asFile.absolutePath
    val recordsProperty = providers.gradleProperty("records")
    argumentProviders.add {
        listOf(recordsProperty.getOrElse(defaultRecordsDir))
    }
}

dependencies {
    gatling(platform(project(":platform")))

    gatlingImplementation(testFixtures(project(":showcase-command-api")))

    gatlingCompileOnly(libs.lombok)

    gatlingAnnotationProcessor(libs.lombok)
}
