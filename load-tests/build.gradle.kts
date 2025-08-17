plugins {
    id("load-testing-conventions")
}

dependencies {
    gatling(platform(project(":platform")))

    gatlingImplementation(testFixtures(project(":showcase-command-api")))

    gatlingCompileOnly(libs.lombok)

    gatlingAnnotationProcessor(libs.lombok)
}
