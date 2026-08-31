## 1. Concrete single-sourced catalog coordinates

- [x] 1.1 Replace the broad-major `*-image` literals with concrete `*-image-tag` coordinates in
      `gradle/libs.versions.toml` (`postgres-image-tag = "17.6"`, `kafka-image-tag = "3.9.0"`,
      `opensearch-image-tag = "3.2.0"`), pin `bitnami-postgresql` to `16.7.27`, `bitnami-kafka` to `31.5.0`,
      `bitnami-opensearch` to `2.0.10` (replacing the `x.x.x` ranges), and verify the Gradle version-catalog accessors
      resolve the new coordinates
- [x] 1.2 Use the `*-image-tag` accessors in `docker-conventions.gradle.kts` (compose env vars) and
      `java-conventions.gradle.kts` (test system properties), verifying build-logic compiles and the compose/test
      surfaces resolve the expected tags (`postgres:17.6`, `apache/kafka:3.9.0`, `opensearchproject/opensearch:3.2.0`)

## 2. Drift verification via the chart-preconfigured tag

- [x] 2.1 Add the root `verifyInfraImageVersions` task (wired into `check`) that resolves each pinned chart's
      preconfigured default `image.tag` via the Helm CLI (`helm show values bitnami/<chart> --version <pinned>`),
      extracts its leading numeric app-version segment, and fails the build if it differs from the `*-image-tag`'s
      leading numeric segment after stripping trailing `.0` zero-padding from both (so `17.6` vs `17.6.0`, or `17` vs
      `17.0.0`, are equivalent), using the plugin-managed Helm client and the `helmUpdateRepositories` task (TTL-cached
      repo index)
- [x] 2.2 Verify the task passes when in sync and fails when a `bitnami-*` or `*-image-tag` coordinate is deliberately
      changed, and that it runs as part of `./gradlew check` without a CI helm-setup step
- [x] 2.3 Extend `verifyInfraImageVersions` to scan each infra release's values files (`helm/values/*/values*.yaml`)
      and fail if any of them pins `image.tag` (enforcing the "SHALL NOT override" rule), and verify it passes with the
      current values files and fails when an `image.tag` pin is added
- [x] 2.4 Derive the verify task's checks from the actual `helm.releases` container (chart reference, chart version,
      and values directories read from each release, with only a chart-ref → component/image-tag lookup kept in
      `build.gradle.kts`), and verify that renaming a release (and its values directory) retargets its check and that
      removing a release drops it

## 3. Full test-suite verification

- [x] 3.1 Verify the change with the full test suite, since Kafka is used in the integration and e2e tests — run
      `./gradlew check` (with integration tests and the coverage gate) and `./gradlew :showcase-api-gateway:e2eTest`,
      confirming the `verifyInfraImageVersions` task, all module checks, and the e2e suite pass, and `openspec validate
      --all` passes

## 4. Kafka 3.9.0 test workaround

- [x] 4.1 Apply the KAFKA-18281 workaround to the `KafkaContainer` usages (`ShowcaseProjectorIT`,
      `ShowcaseApiGatewayE2E`) by overriding `KAFKA_LISTENERS` to `PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094`
      (empty hosts make them implicit), so Kafka 3.9.0 starts under Testcontainers, and verify the projection-service
      integration tests pass (8 tests, 0 failures)

## 5. Docs

- [x] 5.1 Update `AGENTS.md` documenting that infra image versions are single-sourced in the catalog
      (`*-image-tag` for docker-compose/Testcontainers official images, pinned `bitnami-*` chart versions for the Helm
      deployment, whose preconfigured `image.tag` the charts deploy), how each surface resolves its image family, how
      to bump a version, and the Kafka 3.9.0 KAFKA-18281 listener workaround for Testcontainers, verifying the added
      lines stay within the 120-character limit