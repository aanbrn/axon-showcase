plugins {
    id("frontend-conventions")
}

project.description = "Showcase Web UI"

// Deployable image name, overriding the PackBuildImageTask `${project.name}:<version>` default.
// The API base URL is not baked in — it is provided at runtime via SHOWCASE_API_BASE_URL (see start.sh).
tasks.named<PackBuildImageTask>("dockerBuildImage") {
    imageName.set("aanbrn/axon-showcase-web-ui:${project.version}")
}
