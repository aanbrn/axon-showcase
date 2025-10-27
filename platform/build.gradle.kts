plugins {
    `java-platform`
}

project.description = "Build Platform"

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(libs.axon.bom))
    api(platform(libs.jackson.bom))
    api(platform(libs.kotlin.bom))
    api(platform(libs.micrometer.bom))
    api(platform(libs.micrometer.tracing.bom))
    api(platform(libs.mockito.bom))
    api(platform(libs.netty.bom))
    api(platform(libs.opentelemetry.bom))
    api(platform(libs.protobuf.bom))
    api(platform(libs.reactor.bom))
    api(platform(libs.resilience4j.bom))
    api(platform(libs.spring.boot.dependencies))
    api(platform(libs.spring.data.bom))
    api(platform(libs.spring.framework.bom))

    constraints {
        api(libs.commons.compress)
        api(libs.commons.fileupload)
        api(libs.commons.io)
        api(libs.commons.lang3)
        api(libs.guava)
        api(libs.hamcrest)
        api(libs.jgroups)
        api(libs.jgroups.kunernetes)
        api(libs.log4j.api)
        api(libs.log4j.core)
        api(libs.log4j.toSlf4j)
        api(libs.logback.classic)
        api(libs.logback.core)
        api(libs.lombok)
        api(libs.swagger.annotations.jakarta)
        api(libs.swagger.core.jakarta)
        api(libs.swagger.models.jakarta)
        api(libs.swagger.ui)
        api(libs.xstream)
    }
}
