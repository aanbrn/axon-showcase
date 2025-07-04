plugins {
    `java-platform`
}

project.description = "Build Platform"

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(libs.axon.bom))
    api(platform(libs.opentelemetry.bom))
    api(platform(libs.spring.boot.dependencies))
    api(platform(libs.spring.data.bom))
    api(platform(libs.spring.framework.bom))
    api(platform(libs.mockito.bom))
    api(platform(libs.protobuf.bom))
    api(platform(libs.resilience4j.bom))
    api(platform(libs.kotlin.bom))

    constraints {
        api(libs.commons.compress)
        api(libs.commons.fileupload)
        api(libs.commons.io)
        api(libs.jgroups)
        api(libs.logback.classic)
        api(libs.logback.core)
        api(libs.xstream)
        api(libs.guava)
        api(libs.hamcrest)
    }
}
