# Single-source infrastructure image versions

## Why

The same infrastructure components (PostgreSQL, Kafka, OpenSearch) are used in three places — the Helm deployment,
`docker-compose.yml` for local dev, and Testcontainers for integration/e2e tests — but their versions flow from
independent sources. Today the catalog pins broad-major `*-image` literals (`17`, `3.9.2`, `3`) for compose/Tests and
range `bitnami-*` chart versions (`16.x.x`, `31.x.x`, `2.x.x`) for the Helm charts, so Kafka resolves to `3.9.2`
(official image) in compose/Tests but `3.9.0` (Bitnami chart) in Helm, and PostgreSQL and OpenSearch only agree on a
major. There is no single authority that keeps the deployed, locally-run, and tested versions in sync, so the app can
behave differently across environments.

## What Changes

- Make the test-surface tags concrete and single-sourced: replace the broad-major `*-image` literals with
  `*-image-tag` coordinates in `gradle/libs.versions.toml` (`postgres-image-tag = "17.6"`, `kafka-image-tag = "3.9.0"`,
  `opensearch-image-tag = "3.2.0"`), and point docker-compose and the Testcontainers IT/e2e suites at the new accessors.
- Pin the Bitnami **chart versions** in `gradle/libs.versions.toml`: `bitnami-postgresql = "16.7.27"`,
  `bitnami-kafka = "31.5.0"`, `bitnami-opensearch = "2.0.10"` (concrete, replacing the `x.x.x` ranges). Each chart ships
  its own preconfigured default `image.tag`, which the infra releases deploy as-is — no `image.tag` override anywhere.
- Add a **verification task** (wired into `check`) that derives its checks from the actual `helm.releases` container,
  resolves each pinned chart's preconfigured default `image.tag` (`helm show values bitnami/<chart> --version <pinned>`)
  and asserts its leading numeric app version equals the `*-image-tag` after stripping trailing `.0` zero-padding from
  both sides (so `17.6` vs `17.6.0`, or `17` vs `17.0.0`, are equivalent), failing the build if they drift. The task
  also scans the repo's infra values files and fails if any of them pins `image.tag`, so the deployment cannot silently
  reintroduce a separately maintained override. Deriving the checks from the configured releases means renaming or
  removing a release retargets or drops its check instead of leaving a stale gate.
- Document the model in `AGENTS.md` (which coordinate drives which surface, and how to bump an infra component).

## Capabilities

### New Capabilities

- `showcase/quality/infra-image-versions`: the repository keeps the image references of its infrastructure components
  (PostgreSQL, Kafka, OpenSearch) single-sourced — concrete official image tags for the test surfaces, pinned Bitnami
  chart versions for the deployment — with a build gate that catches drift between the chart-preconfigured Bitnami
  image tag and the test image version.

### Modified Capabilities

(none)

## Impact

- **Version catalog**: replaces `postgres-image`/`kafka-image`/`opensearch-image` (broad majors) with
  `postgres-image-tag`/`kafka-image-tag`/`opensearch-image-tag` (concrete tags), and pins `bitnami-postgresql`/
  `bitnami-kafka`/`bitnami-opensearch` to concrete chart versions.
- **Build logic**: `docker-conventions.gradle.kts` (compose env vars) and `java-conventions.gradle.kts` (test system
  properties) read the `*-image-tag` accessors; a new root `verifyInfraImageVersions` task is wired into `check`.
- **Helm releases** (`build.gradle.kts`): unchanged structurally — the releases set only the chart version, and the
  charts' preconfigured `image.tag` is deployed; the values files keep `image.repository` (the `bitnamilegacy/*` repo).
- **docker-compose.yml**: unchanged structurally (still `${POSTGRES_VERSION}` etc.), but the injected versions become
  concrete image tags.
- **Tests**: Testcontainers (IT + e2e) keep the `postgres.image.version`/`kafka.image.version`/
  `opensearch.image.version` system properties, now fed concrete image tags.
- **Risk**: the verify task now needs the Helm CLI (plugin-managed client) and network access to the Bitnami chart
  repository at `check` time — `helmUpdateRepositories` caches the repo index with a TTL, and the helm client is
  downloaded by the plugin, so no CI setup step is needed.