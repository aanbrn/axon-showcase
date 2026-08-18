import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

import java.math.BigDecimal
import java.util.Properties

plugins {
    jacoco
}

val libs = the<LibrariesForLibs>()

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

val allSuiteTestTasks = tasks.withType<Test>().matching { it.name != "e2eTest" }

val jacocoExecDir = layout.buildDirectory.dir("jacoco").get().asFile

val mainClasses = extensions.getByType<SourceSetContainer>()
    .named("main")
    .get()
    .output
    .classesDirs

fun generatedClassExcludes(): List<String> {
    val moduleExcludes = project.extra.properties["coverage.generatedClassExcludes"] as? List<*> ?: emptyList<Any?>()
    return listOf(
        "**/*Proto.class",
        "**/*OrBuilder.class",
        "**/*OuterClass.class",
        "**/*Grpc.class"
    ) + moduleExcludes.map { it.toString() }
}

val coverageBaselineMinimum: BigDecimal = run {
    val baselineFile = rootProject.layout.projectDirectory
        .file("config/jacoco/coverage-baseline.properties")
        .asFile
    val props = Properties()
    if (baselineFile.exists()) {
        baselineFile.inputStream().use { props.load(it) }
    }
    props.getProperty("coverage.instruction.minimum", "0.80").toBigDecimal()
}

val coverageGateEnabled = providers.provider { project.extra.properties["coverage.gate.enabled"] != false }

tasks.named<JacocoReport>("jacocoTestReport") {
    classDirectories.setFrom(mainClasses.asFileTree.matching { exclude(generatedClassExcludes()) })
    executionData.setFrom(fileTree(jacocoExecDir) { include("*.exec"); exclude("e2eTest.exec") })
    dependsOn(allSuiteTestTasks)
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    classDirectories.setFrom(mainClasses.asFileTree.matching { exclude(generatedClassExcludes()) })
    executionData.setFrom(fileTree(jacocoExecDir) { include("*.exec"); exclude("e2eTest.exec") })
    dependsOn(allSuiteTestTasks)

    violationRules {
        rule {
            limit {
                minimum = coverageBaselineMinimum
            }
        }
    }
}

tasks.named("check") {
    if (coverageGateEnabled.get()) {
        dependsOn(tasks.named("jacocoTestCoverageVerification"))
    }
}
