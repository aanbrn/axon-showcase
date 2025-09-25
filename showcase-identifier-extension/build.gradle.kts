plugins {
    id("java-library-conventions")
}

project.description = "Showcase Identifier Extension"

dependencies {
    implementation(platform(project(":platform")))

    api(libs.axon.messaging)
    api(libs.jakarta.validation.api)

    implementation(libs.ksuid)

    testImplementation(project(":showcase-test"))
    testImplementation(libs.hibernate.validator)
    testImplementation(libs.expressly)
}
