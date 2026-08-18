plugins {
    id("java-library-conventions")
    id("code-coverage-conventions")
}

dependencies {
    implementation(libs.mapstruct.processor)

    testImplementation(libs.mockito.core)
}
