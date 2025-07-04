import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.github.spotbugs")
}

val libs = the<LibrariesForLibs>()

spotbugs {
    toolVersion = libs.versions.spotbugs.asProvider().get()
}

dependencies {
    spotbugsPlugins(libs.spotbugs.findsecbugs.plugin)
    spotbugsPlugins(libs.spotbugs.fbContrib.plugin)
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
