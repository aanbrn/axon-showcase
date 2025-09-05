plugins {
    id("java-library-conventions")
}

project.description = "Showcase Projection Model"

dependencies {
    implementation(platform(project(":platform")))

    api(libs.spring.data.opensearch) {
        exclude(
            group = libs.opensearch.client.restHighLevel.get().group,
            module = libs.opensearch.client.restHighLevel.get().name
        )
    }

    testFixturesApi(testFixtures(project(":showcase-command-api")))

    testFixturesImplementation(project(":showcase-test"))
}
