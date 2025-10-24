plugins {
    id("java-library-conventions")
}

project.description = "Showcase Resilience4j Extension"

dependencies {
    implementation(platform(project(":platform")))

    implementation(libs.spring.boot.autoconfigure)

    implementation(libs.guava)
}
