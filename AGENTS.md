# AGENTS.md

## Conventions

- Wrap code and text at 120 characters.

## Project Overview

**axon-showcase** — a CQRS/Event Sourcing reference app using the Axon Framework. Java 21, Spring Boot 3.5.16, Gradle
8.14.5 (Kotlin DSL), monorepo with 18 modules.

## Prerequisites

- Java 21+
- Docker & Docker Compose (for local dev / integration tests)
- Gradle wrapper included (use `./gradlew`)
- Helm 3.x + Kubernetes cluster (for deployment)

## Build & Test

```bash
# Full build (all modules)
./gradlew build

# Build a single service (also produces Docker image via bootBuildImage)
./gradlew :showcase-command-service:bootBuildImage

# Run tests for a single module
./gradlew :showcase-command-service:test

# Run component tests (faster, no containers)
./gradlew :showcase-command-service:componentTest

# Run integration tests (requires Docker, spins up Testcontainers)
./gradlew :showcase-command-service:integrationTest

# Check runs: compile → spotbugs → errorprone → test → componentTest → integrationTest
./gradlew :showcase-command-service:check

# Load tests (Gatling)
./gradlew :load-tests:test
```

**Test suite order matters:** `test` → `componentTest` → `integrationTest`. Integration tests for `showcase-api-gateway`
must run after `showcase-command-client` and `showcase-query-client` integration tests (`mustRunAfter`).

**Test tiers** — a test's tier is decided by its collaborators (what is real vs. faked), not by how long it takes to run:

- **Unit** (`src/test/java`, suffix `Tests`): the subject under test is isolated — its collaborators are mocks/fakes,
  no Spring context. Verifies single-class logic (e.g. `KsuidIdentifierFactoryTests`).
- **Component** (`src/componentTest/java`, suffix `CT`): the subject is composed with real, in-process collaborators —
  real serializers, Axon `AggregateTestFixture`/`SagaTestFixture`, or a Spring context with WireMock — but external
  infrastructure is never started. Verifies a component behaves correctly against its real neighbors
  (e.g. `QueryMessageRequestMapperCT`, `ShowcaseAggregateCT`).
- **Integration** (`src/integrationTest/java`, suffix `IT`): real external infrastructure via Testcontainers
  (PostgreSQL, Kafka, OpenSearch). Verifies services against the real things they talk to.

`disable-axoniq-console-message=true` is set both in integration tests and in each service's main application
source (e.g., `ShowcaseApiApplication.java`).

**DB scripts** — before running the command-service standalone (outside Docker), ensure the PostgreSQL event store is
initialized:

```bash
./db-init.sh   # creates user `showcase` and database `showcase-events`
./db-drop.sh   # drops the database
```

## Architecture

CQRS with 4 services + an API gateway:

- **showcase-command-service** — write side, publishes events to Kafka, uses PostgreSQL event store
- **showcase-projection-service** — consumes Kafka events, writes projections to OpenSearch
- **showcase-query-service** — read side, queries OpenSearch
- **showcase-api-gateway** — REST entry point (`/showcases`), routes to command/query services

Key modules (libraries, not services):

- `showcase-command-api` / `showcase-query-api` — API interfaces; their testFixtures are used by clients and services
- `showcase-command-client` / `showcase-query-client` — reactive clients for remote services
- `showcase-query-proto` — Protobuf definitions for query side
- `showcase-projection-model` — shared query model definitions
- `showcase-test` — shared test utilities
- `showcase-identifier-extension` — KSUID identifier support
- `showcase-mapstruct-extension` — MapStruct extensions
- `showcase-resilience4j-extension` — Resilience4j integration
- `platform` — Java platform BOM for dependency version management
- `build-logic` — Gradle convention plugins
- `helm` — Gradle module for Helm releases; contains sub-module `helm:chart`
- `load-tests` — Gatling-based load tests

## Conventions

- **Lombok**: `addNullAnnotations = jspecify`; copyable annotations include `@Qualifier` and `@Value`
- **MapStruct**: default component model is `spring` (`-Amapstruct.defaultComponentModel=spring`)
- **ErrorProne**: NullAway on `showcase.*` packages in production code; disabled in `TestJava` tasks
- **SpotBugs**: finds bugs with findsecbugs and fbContrib plugins; uses `spotbugs-include.xml` and
  `spotbugs-exclude.xml` filters if present (see `code-check-conventions.gradle.kts`)
- **LZ4 relocation**: root build forces `org.lz4:lz4-java` substitution (see `build.gradle.kts`)
- **All JavaCompile tasks** add `-parameters` flag
- **Unit test classes** use the suffix `Tests` (e.g., `KSUIDTests`, `KsuidIdentifierFactoryTests`)
- **Component test classes** use the suffix `CT` (e.g., `QueryMessageRequestMapperCT`)
- **Integration test classes** use the suffix `IT`
- **No comments** in source code (per project convention)

## Docker Images

Each boot service builds a Docker image:

- `aanbrn/axon-showcase-command-service:${project.version}`
- `aanbrn/axon-showcase-api-gateway:${project.version}`
- `aanbrn/axon-showcase-query-service:${project.version}`
- `aanbrn/axon-showcase-projection-service:${project.version}`

Image names are set in each service's `bootBuildImage` task configuration.

## Kubernetes Deployment

```bash
# Deploy to local cluster (must be ordered)
helm install kps prometheus-community/kube-prometheus-stack --namespace monitoring --create-namespace --wait
helm install tempo grafana/tempo --namespace monitoring --wait
helm install axon-showcase-db-events bitnami/postgresql --wait
helm install axon-showcase-kafka bitnami/kafka --wait
helm install axon-showcase-os-views bitnami/opensearch --wait
helm install axon-showcase ./helm/chart --wait

# Or use Gradle Helm plugin (builds images, then installs all releases to the local target)
./gradlew helmInstallToLocal
```

**Helm release order**: kps → tempo → db-events/kafka/os-views → axon-showcase. Uninstall in reverse.

**Chart validation**: the chart is linted as part of packaging (`helmPackageMainChart` → `helmLintMainChart`). Lint runs
strict (warnings are errors) and lints the Bitnami `common` subchart, rendering two extra value sets:

```bash
./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal
```

Value files live in `helm/chart/src/test/helm/` (`helm-lint-full.yaml` enables all optional features,
`helm-lint-minimal.yaml` disables the default-on ones).

Custom values can be placed in `helm/values/<release-name>/values-local.yaml`.

## Local Development

```bash
# Infrastructure
docker compose up -d

# Run services individually (each on separate port)
./gradlew :showcase-api-gateway:bootRun        # :8080
./gradlew :showcase-command-service:bootRun     # :8001
./gradlew :showcase-query-service:bootRun       # :8002
./gradlew :showcase-projection-service:bootRun  # :8003
```

Docker Compose (`docker-compose.yml`) starts all infrastructure **and** the Java services (using pre-built Docker
images `aanbrn/axon-showcase-*:${PROJECT_VERSION}`). Build the images first (`./gradlew bootBuildImage`) or set
`PROJECT_VERSION` accordingly. To run services from source instead, use `bootRun` as shown above.

## Key Environment Variables

- `DB_PASSWORD=showcase` — PostgreSQL password for command-service
- `BPL_DEBUG_ENABLED=true` / `BPL_DEBUG_PORT=8000-8003` — Spring Boot Actuator debug
- `THC_PATH=/actuator/health` — health check path
- `JAVA_OPTS=-XX:MaxDirectMemorySize=128m -XX:MaxGCPauseMillis=20` — container JVM tuning

## Gotchas

- Integration tests for `showcase-api-gateway` depend on Docker images of all other services being built
  (`bootBuildImage`). Run those first.
- The `showcase-api-gateway` integrationTest must run after `showcase-command-client` and `showcase-query-client`
  integration tests.
- citi gradle-helm-plugin tasks are not configuration-cache compatible — do not enable
  `org.gradle.configuration-cache=true` (verify with `--configuration-cache` before adding it).
- `helmInstallToLocal` tags `"*"` select all releases; deployment order defined by `mustInstallAfter`/
  `mustUninstallAfter` in `build.gradle.kts`.
- NullAway is strict on `showcase.*` packages — ensure proper `@Nullable`/`@NonNull` annotations from `jspecify`.
