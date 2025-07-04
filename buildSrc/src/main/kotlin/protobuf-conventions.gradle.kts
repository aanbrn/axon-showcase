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
}

dependencies {
    implementation(libs.protobuf.java)
}
