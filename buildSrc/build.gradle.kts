plugins {
    `kotlin-dsl`
}

project.description = "Build Convention Plugins"

dependencies {
    implementation(libs.kotlin.plugin)
    implementation(libs.lombok.plugin)
    implementation(libs.spring.boot.plugin)
    implementation(libs.protobuf.plugin)
    implementation(libs.gatling.plugin)
    implementation(libs.dependencyVersions.plugin)
    implementation(libs.spotbugs.plugin)
    implementation(libs.helm.plugin)
    implementation(libs.helm.releases.plugin)

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.commons.lang3)

    constraints {
        api(libs.log4j.api)
        api(libs.log4j.core)
        api(libs.log4j.toSlf4j)
        api(libs.spotbugs)
        api(libs.spotbugs.annotations)
    }
}
