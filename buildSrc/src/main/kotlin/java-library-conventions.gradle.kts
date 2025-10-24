import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("java-conventions")
    `java-library`
}

val libs = the<LibrariesForLibs>()

dependencies {
    api(libs.jspecify)
}
