## 1. Suppress spring-data-opensearch major updates

- [x] 1.1 Add the three exact coordinates (`org.opensearch.client:spring-data-opensearch`,
      `org.opensearch.client:spring-data-opensearch-starter`, `org.opensearch.client:
      spring-data-opensearch-testcontainers`) to `config/dependency-updates/major-disabled.properties` with a short
      pointer comment, and verify the spring-data-opensearch major row (`[2.0.6 -> 3.1.1]`) no longer appears in
      `./gradlew dependencyUpdates` output while same-major (2.x) updates remain reported
- [x] 1.2 Confirm `opensearch-java` and `opensearch-rest-client` majors are still reported (the transport clients
      must not be suppressed by the new entries)

## 2. Verify the change artifacts

- [x] 2.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors
- [x] 2.2 Confirm the delta spec at `specs/showcase/quality/dependency-management/spec.md` declares the
      spring-data-opensearch suppression requirement with scenarios