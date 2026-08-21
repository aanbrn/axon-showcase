## 1. Bump the catalog versions

- [x] 1.1 Apply the thirteen version bumps in `gradle/libs.versions.toml` (spring-framework-bom 6.2.19,
      spring-security-bom 6.5.11, swagger 2.2.54, swagger-ui 5.32.14, reactor-bom 2025.0.7, protobuf 4.36.0,
      httpclient5 5.6.4, micrometer-bom 1.17.1, micrometer-tracing-bom 1.7.1, guava 33.7.1-jre, spotbugs 4.10.4,
      spring-data-opensearch 2.0.7, springdoc-openapi-starter 2.9.0)

## 2. Verify the bumps

- [x] 2.1 Compile the affected modules (`./gradlew compileJava` across the project or the key services) and confirm
      the build succeeds with the new versions
- [x] 2.2 Run `./gradlew dependencyUpdates` and confirm the thirteen bumped rows no longer appear, while
      `spring-data-bom` (SB4 train) and the `log4j-core` noise row remain as expected

## 3. Verify the change artifacts

- [x] 3.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors