plugins {
    id("java-library-conventions")
}

project.description = "Showcase Projection Model"

dependencies {
    api(libs.spring.data.elasticsearch)

    testFixturesApi(testFixtures(project(":showcase-command-api")))

    testFixturesImplementation(project(":showcase-test"))
}
