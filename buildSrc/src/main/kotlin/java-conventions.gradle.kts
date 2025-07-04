@file:Suppress("UnstableApiUsage")

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    java
    `jvm-test-suite`
    `java-test-fixtures`
    id("io.freefair.lombok")
    id("code-check-conventions")
}

val libs = the<LibrariesForLibs>()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

val mockitoAgent: Configuration = configurations.create("mockitoAgent")

dependencies {
    implementation(platform(project(":platform")))

    testFixturesImplementation(platform(project(":platform")))

    annotationProcessor(libs.mapstruct.processor)

    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

val mockitoAgentAsPath: String = mockitoAgent.asPath

testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter()

            dependencies {
                implementation(platform(project(":platform")))

                implementation(libs.assertj.core)

                runtimeOnly(libs.logback.classic)
            }

            targets {
                all {
                    testTask.configure {
                        jvmArgs("-javaagent:$mockitoAgentAsPath")

                        systemProperty("project.version", project.version)
                        systemProperty("postgres.image.version", libs.versions.postgres.image.get())
                        systemProperty("kafka.image.version", libs.versions.kafka.image.get())
                        systemProperty("elasticsearch.image.version", libs.versions.elasticsearch.image.get())
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites)
}
