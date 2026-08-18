plugins {
    id("java-library-conventions")
    id("code-coverage-conventions")
}

project.description = "Showcase Resilience4j Extension"

dependencies {
    implementation(platform(project(":platform")))

    implementation(libs.spring.boot.autoconfigure)

    testImplementation(libs.mockito.core)
}
