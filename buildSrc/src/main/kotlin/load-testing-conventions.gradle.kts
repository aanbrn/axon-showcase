import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("java-conventions")
    id("io.gatling.gradle")
}

val libs = the<LibrariesForLibs>()
