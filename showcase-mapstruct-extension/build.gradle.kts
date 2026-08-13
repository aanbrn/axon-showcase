plugins {
    id("java-library-conventions")
}

dependencies {
    implementation(libs.mapstruct.processor)

    testImplementation(libs.mockito.core)
}
