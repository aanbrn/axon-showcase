import com.github.spotbugs.snom.SpotBugsTask
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.github.spotbugs")
    id("net.ltgt.errorprone")
    id("checkstyle")
}

val libs = the<LibrariesForLibs>()

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configDirectory.set(rootProject.layout.projectDirectory.dir("config/checkstyle"))
    maxErrors = 0
}

spotbugs {
    showProgress = true
    toolVersion = libs.versions.spotbugs.asProvider().get()
}

dependencies {
    spotbugs(libs.spotbugs)
    spotbugs(libs.commons.lang3)
    spotbugs(libs.log4j.core)

    spotbugsPlugins(libs.spotbugs.findsecbugs.plugin)
    spotbugsPlugins(libs.spotbugs.fbContrib.plugin)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

tasks.withType<SpotBugsTask> {
    val includeFile = rootProject.layout.projectDirectory.file("spotbugs-include.xml")
    if (includeFile.asFile.exists()) {
        includeFilter = includeFile
    }
    val excludeFile = rootProject.layout.projectDirectory.file("spotbugs-exclude.xml")
    if (excludeFile.asFile.exists()) {
        excludeFilter = excludeFile
    }

    reports.create("html") {
        required = true
        setStylesheet("fancy-hist.xsl")
    }
    reports.create("xml") {
        required = true
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-XDaddTypeAnnotationsToSymbol=true")

    if (name.endsWith("TestJava")) {
        options.errorprone {
            disable("NullAway")
        }
    } else {
        options.errorprone {
            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", "showcase")

            disable("StringConcatToTextBlock")
            disableWarningsInGeneratedCode = true
        }
    }
}
