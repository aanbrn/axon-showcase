plugins {
    id("java-library-conventions")
}

project.description = "Showcase Tracing Extension"

dependencies {
    implementation(platform(project(":platform")))

    implementation(libs.axon.springBoot.autoconfigure) {
        exclude(group = libs.axon.serverConnector.get().group, module = libs.axon.serverConnector.get().name)
    }
    implementation(libs.axon.tracing.opentelemetry)
    implementation(libs.spring.boot.autoconfigure)
}
