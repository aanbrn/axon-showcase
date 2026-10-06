import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("java-conventions")
    id("com.google.protobuf")
}

val libs = the<LibrariesForLibs>()

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:" + libs.versions.protobuf.asProvider().get()
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:" + libs.versions.grpc.asProvider().get()
        }
    }
    generateProtoTasks {
        all().configureEach {
            plugins {
                create("grpc")
            }
        }
    }
}

dependencies {
    implementation(libs.protobuf.java)
}
