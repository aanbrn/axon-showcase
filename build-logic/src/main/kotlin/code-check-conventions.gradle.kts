import com.github.spotbugs.snom.SpotBugsTask
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.github.spotbugs")
    id("net.ltgt.errorprone")
    id("checkstyle")
    id("com.diffplug.spotless")
}

val libs = the<LibrariesForLibs>()

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configDirectory.set(rootProject.layout.projectDirectory.dir("config/checkstyle"))
    maxErrors = 0
}

spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat()
        licenseHeader("// SPDX-License-Identifier: MIT\n")
    }
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
    val includeFile = rootProject.layout.projectDirectory.file("config/spotbugs/spotbugs-include.xml")
    if (includeFile.asFile.exists()) {
        includeFilter = includeFile
    }
    val excludeFile = rootProject.layout.projectDirectory.file("config/spotbugs/spotbugs-exclude.xml")
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
