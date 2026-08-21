# AGENTS.md

## Conventions

- Wrap code and text at 120 characters.

## Project Overview

**axon-showcase** — a CQRS/Event Sourcing reference app using the Axon Framework. Java 21, Spring Boot 3.5.16, Gradle
8.14.5 (Kotlin DSL), monorepo with 18 modules.

This repo uses **spec-driven development**: behavior is captured as OpenSpec specs in `openspec/specs/showcase/`
(organized by architectural role: `gateway`, `write-side`, `read-side`, `clients`, `extensions`, `deployment`,
`quality`).
Code changes go through the `opsx-*` opencode commands / `openspec-*` skills (propose → apply → archive). Follow these
workflows for new work, and treat the captured specs as the behavioral source of truth.

## OpenSpec Workflow Agreement

**Never archive a change automatically after apply.** Stop after implementation, report, and let the user review the
changes made and decide when (or whether) to archive.

**Never push to the remote automatically.** Commit locally when asked, but only `git push` when the user explicitly
requests it (e.g., "push" or "commit and push").

## Prerequisites

- Java 21+
- Docker & Docker Compose (for local dev / integration tests)
- Gradle wrapper included (use `./gradlew`)
- Helm 4.x + Kubernetes cluster (for deployment)
- Snyk CLI (for `./gradlew dependencySecurityCheck`)

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

# Run end-to-end tests (gateway: boots all services + infra via Testcontainers)
./gradlew :showcase-api-gateway:e2eTest

# Check runs: compile → spotbugs → errorprone → test → componentTest → integrationTest → e2eTest
./gradlew :showcase-command-service:check

# Load tests (Gatling)
./gradlew :load-tests:test

# Dependency security scan (Snyk; requires the Snyk CLI on PATH, not part of check)
./gradlew dependencySecurityCheck

# Dependency update report (only catalog-owned coordinates; majors suppressed for groups in
# config/dependency-updates/major-disabled.properties)
./gradlew dependencyUpdates
```

The `/dependency-updates` opencode command runs this task and summarizes the available updates; the
`/gradle-update` command updates the Gradle wrapper to the latest stable version when one is available.

Build-environment constraints can surface as spurious "current version" rows in the report: build tooling such as
SpotBugs publishes module constraints that `checkBuildEnvironmentConstraints` reads and reports as the current version.
For example, a `log4j-core [2.17.1 -> 2.26.1]` row appears even though `log4j-core` resolves to `2.26.1` everywhere —
`2.17.1` is the floor of an external Log4Shell guard published by `spotbugs-annotations`. These rows are a known
`gradle-versions-plugin` limitation, not real updates (see upstream ben-manes/gradle-versions-plugin#755); do not chase
them (see ADR-0007).

Major-blocking entries in `config/dependency-updates/major-disabled.properties` carry a pointer comment naming the
coordinate and its rationale; the authoritative reasoning for each suppressed coordinate lives in the
`showcase/quality/dependency-management` spec.

**Test suite order matters:** `test` → `componentTest` → `integrationTest` → `e2eTest`. The `showcase-api-gateway`
`e2eTest` must run after `showcase-command-client` and `showcase-query-client` integration tests (`mustRunAfter`).

**Test tiers** — a test's tier is decided by its collaborators (what is real vs. faked), not by how long it takes to
run:

- **Unit** (`src/test/java`, suffix `Tests`): the subject under test is isolated — its collaborators are mocks/fakes, no
  Spring context. Verifies single-class logic (e.g. `KsuidIdentifierFactoryTests`).
- **Component** (`src/componentTest/java`, suffix `CT`): the subject is composed with real, in-process collaborators —
  real serializers, Axon `AggregateTestFixture`/`SagaTestFixture`, or a Spring context with WireMock — but external
  infrastructure is never started. Verifies a component behaves correctly against its real neighbors (e.g.
  `QueryMessageRequestMapperCT`, `ShowcaseAggregateCT`).
- **Integration** (`src/integrationTest/java`, suffix `IT`): real external infrastructure via Testcontainers
  (PostgreSQL, Kafka, OpenSearch). Verifies services against the real things they talk to.
- **End-to-end** (`src/e2eTest/java`, suffix `E2E`): the whole system is booted — all four service containers plus
  Testcontainers infrastructure — and exercised over HTTP. Verifies cross-service propagation over the full command →
  Kafka → projection → query pipeline (e.g. `ShowcaseApiGatewayE2E`).

`disable-axoniq-console-message=true` is set both in integration tests and in each service's main application source
(e.g., `ShowcaseApiApplication.java`).

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

- **Lombok**: use Lombok where possible (e.g., `@RequiredArgsConstructor`, `@Data`, `@Builder`, `@Value`) instead of
  writing boilerplate manually; `addNullAnnotations = jspecify`; copyable annotations include `@Qualifier` and `@Value`
- **MapStruct**: default component model is `spring` (`-Amapstruct.defaultComponentModel=spring`)
- **ErrorProne**: NullAway on `showcase.*` packages in production code; disabled in `TestJava` tasks
- **SpotBugs**: finds bugs with findsecbugs and fbContrib plugins; uses `spotbugs-include.xml` and
  `spotbugs-exclude.xml` filters if present (see `code-check-conventions.gradle.kts`)
- **LZ4 relocation**: root build forces `org.lz4:lz4-java` substitution (see `build.gradle.kts`)
- **All JavaCompile tasks** add `-parameters` flag
- **Unit test classes** use the suffix `Tests` (e.g., `KSUIDTests`, `KsuidIdentifierFactoryTests`)
- **Component test classes** use the suffix `CT` (e.g., `QueryMessageRequestMapperCT`)
- **Integration test classes** use the suffix `IT`
- **E2E test classes** use the suffix `E2E`
- **Test display names**: every test class and every `@Test`/`@ParameterizedTest` method (plus `@Nested` groups) carries
  a static-sentence `@DisplayName` (e.g., `@DisplayName("Showcase aggregate component tests")`,
  `@DisplayName("Finishing a showcase with a valid command succeeds")`). Do not use `{0}`-style placeholders — named
  `argumentSet("...", ...)` invocations already render their own detail
- **Spring bean mocks in tests**: use `@MockitoBean` (from `org.springframework.test.context.bean.override.mockito`),
  not the deprecated-for-removal `@MockBean` (`org.springframework.boot.test.mock.mockito`), which has been deprecated
  since Spring Boot 3.4
- **Test tier placement**: a test's tier is decided by its collaborators (see Test tiers). Verify the application's
  bean wiring (`@SpringBootApplication` config) at the **integration** tier via a real context boot — do not write
  component tests that mock the app's own collaborators. Component tests compose real in-process collaborators (e.g.
  a real mapper) with only external infrastructure faked
- **Nested test groups for resilience features**: a `@Nested` class that groups Resilience4j scenarios is named
  `<Feature>Behavior` (e.g., `TimeLimiterBehavior`, `RetryBehavior`, `CircuitBreakerBehavior`), both for uniformity and
  to avoid shadowing the library's `CircuitBreaker` type
- **BlockHound jvmArgs**: only suites whose tests call `BlockHound.install()` need
  `-XX:+AllowRedefinitionToAddDeleteMethods` and `-XX:+EnableDynamicAgentLoading` (e.g. the client `integrationTest`
  and `e2eTest`); leave them off suites that don't (e.g. `componentTest` with only an `ApplicationContextRunner` test)
- **`@DirtiesContext`**: add it only where a full-context boot leaks global JVM state — JGroups (ports and system
  properties) and JCache (a JVM-global cache manager). Contexts that are safely cacheable don't need it: service slices,
  and `@Nested` classes with distinct `@ActiveProfiles` (which already get separate cached contexts). Keep it on the
  gateway/command-service full-context ITs (and the command-client IT, which pulls in JGroups); drop it elsewhere
- **Code coverage**: modules opt in via `code-coverage-conventions`. Coverage is measured per module with
  `jacocoTestReport` (unit + component + integration exec data) and aggregated with the root `jacocoRootReport`. The
  `jacocoTestCoverageVerification` gate is wired into `check` at the baseline in
  `config/jacoco/coverage-baseline.properties` and requires Docker (integration tests). A module can extend the
  generated-class excludes via `coverage.generatedClassExcludes`
- **Architecture Decision Records**: record cross-cutting architecture decisions as numbered ADRs under `docs/adr/`
  (Nygard format — Status/Context/Decision/Consequences). OpenSpec captures behavior and change plans; ADRs capture
  the *why* behind structural choices. Capture a decision as an ADR when it is made, not after the fact
- **Docs refresh on change**: on every change, verify whether `AGENTS.md` and `README.md` need to be refreshed to
  reflect the new state (commands, config, conventions, gotchas) and update them before reporting the change done
- **No comments** in source code (per project convention)
- **Javadoc**: classes, methods, and fields carry a Javadoc comment describing their purpose (see
  `ShowcaseApiErrorResolver`, `ShowcaseApiController`); wrap at 120 characters
- **Avoid redundancy**: don't write redundant code — e.g. redundant `throws` clauses on test methods, explicit type
  arguments that diamond inference or target typing resolve, or repeated boilerplate that Lombok covers. Use the
  simplest construct that compiles and stays readable
- **Formatting**: format edited files with the IntelliJ IDE formatter (Code → Reformat Code), not an external CLI
  formatter, so the result matches the project's configured code style. After each edit, run the IntelliJ formatter
  (`ReformatCodeProcessor`, plus `OptimizeImportsProcessor` for JVM sources) through the Steroid MCP
  (`steroid_execute_code` against the open `axon-showcase` project) before reporting the change done.
  - Keep every line within 120 characters; verify with `awk 'length > 120'` over edited files. Avoid redundant line
    wraps: keep a line on one line whenever it fits within 120 characters, rather than breaking early after `=` or
    splitting short chains. Only wrap when a line genuinely exceeds 120 — and when a long assignment must wrap, break
    it right after the `=` and then chain the expression, e.g.
    ```java
    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SomeAutoConfig.class));
    ```
    rather than deep-aligning a chained call on a single long line.
  - For assertion lambdas inside `argumentSet(...)` parameterized sources, prefer a block lambda body
    (`(x) -> { ... }`) so the formatter indents the statements normally instead of deep-aligning one long expression.
    The resulting "Statement lambda can be replaced with expression lambda" inspection is suppressed with
    `@SuppressWarnings("CodeBlock2Expr")` on the source method (the correct token — not `StatementLambdaInspection`).
- **IDE inspections**: after each edit, run the IDE inspections on the touched files (through the Steroid MCP
  `steroid_execute_code` / `runInspectionsDirectly`) and fix the reported warnings before reporting the change done.
  This applies to test classes as well — run inspections on edited `*Tests`, `CT`, `IT`, and `E2E` files and fix any
  warnings they report, not just production code. Prefer assertions like `assertThat(x).isNotNull()` over
  `Objects.requireNonNull(x)` when guarding nullable values in tests, since the IDE recognizes them for dataflow. Ignore
  the `NewClassNamingConvention` inspection on `CT`-, `IT`-, and `E2E`-suffixed test classes alike — the inspection's
  regex reflects a generic `Test`/`IT` naming convention rather than the project's suffix-based test tiers.

## Docker Images

Each boot service builds a Docker image:

- `aanbrn/axon-showcase-command-service:${project.version}`
- `aanbrn/axon-showcase-api-gateway:${project.version}`
- `aanbrn/axon-showcase-query-service:${project.version}`
- `aanbrn/axon-showcase-projection-service:${project.version}`

Image names are set in each service's `bootBuildImage` task configuration. To build for a non-default platform (e.g.,
ARM64 host), pass `-PimagePlatform=linux/amd64` (supported by the `spring-boot-conventions` plugin).

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
./gradlew :showcase-command-service:bootRun     # :8081
./gradlew :showcase-query-service:bootRun       # :8083
./gradlew :showcase-projection-service:bootRun  # :8082
```

Docker Compose (`docker-compose.yml`) starts all infrastructure **and** the Java services (using pre-built Docker images
`aanbrn/axon-showcase-*:${PROJECT_VERSION}`). Build the images first (`./gradlew bootBuildImage`) or set
`PROJECT_VERSION` accordingly. To run services from source instead, use `bootRun` as shown above.

**Ports:** the HTTP ports (`server.port` in each service's `application.yml`) are the API Gateway `8080`, Command Service
`8081`, Query Service `8083`, Projection Service `8082`. In `docker-compose.yml`, the published `8000`–`8003` mappings are
**JVM debug ports** (`BPL_DEBUG_PORT`), not the services' HTTP ports — only the API Gateway publishes its HTTP port
(`8080`); the other services' HTTP ports are reachable only via the Docker network or `bootRun`.

The `docker-conventions` plugin adds root-level `compose*` Gradle tasks that wrap Docker Compose and set
`PROJECT_VERSION` + image versions automatically (also `composeBuildAndUp`, `composeBuildAndRestart`):
`./gradlew composeUp`, `./gradlew composeDown`.

## Key Environment Variables

- `DB_PASSWORD=showcase` — PostgreSQL password for command-service
- `BPL_DEBUG_ENABLED=true` / `BPL_DEBUG_PORT=8000-8003` — JVM debug (JDWP) agent ports (`8000`–`8003` are the published
  debug ports in `docker-compose.yml`; see Local Development)
- `THC_PATH=/actuator/health` — health check path
- `JAVA_OPTS=-XX:MaxDirectMemorySize=128m -XX:MaxGCPauseMillis=20` — container JVM tuning

## Gotchas

- E2E tests for `showcase-api-gateway` depend on Docker images of all other services being built (`bootBuildImage`). Run
  those first.
- The `showcase-api-gateway` e2eTest must run after `showcase-command-client` and `showcase-query-client`
  integration tests.
- The `io.github.build-extensions-oss.helm` / `io.github.build-extensions-oss.helm-releases` gradle-helm-plugin tasks are
  not configuration-cache compatible — do not enable `org.gradle.configuration-cache=true` (verify with
  `--configuration-cache` before adding it).
- The gradle-helm-plugin 3.1.2 calls the deprecated `Project.getProperties()` (a `--warning-mode all` deprecation that
  becomes a hard error in Gradle 10). Tracked upstream as build-extensions-oss/gradle-helm-plugin#145; bump the plugin
  when a fix is released.
- `helmInstallToLocal` tags `"*"` select all releases; deployment order defined by `mustInstallAfter`/
  `mustUninstallAfter` in `build.gradle.kts`.
- NullAway is strict on `showcase.*` packages — ensure proper `@Nullable`/`@NonNull` annotations from `jspecify`.
- Jackson 3 artifacts (`tools.jackson.core:*`) are present on the query-service and projection-service runtime
  classpaths transitively via `co.elastic.clients:elasticsearch-java`, constrained by the platform's `jackson3-bom`
  (kept current on minor versions). This is dependency hygiene, not the deferred Jackson 3 backend migration — Jackson
  2 remains the serialization backend in application code (see ADR-0003).
- Custom Gradle test suites (`componentTest`, `integrationTest`, `e2eTest`) do not inherit the project's
  `implementation`-only dependencies — each suite re-declares what it needs (client integration suites duplicate
  axon/opensearch/wiremock/resilience4j deps, and `showcase-query-proto` must be listed explicitly). A suite can be
  referenced in `shouldRunAfter(...)` only when bound as a `val` (e.g. `val integrationTest =
  suites.register<JvmTestSuite>("integrationTest")`).
- `@Nested` test classes are incompatible with Spring Boot slice tests (`@WebFluxTest`/`@WebMvcTest`): nested classes
  load the full application context instead of the slice and fail on infrastructure beans (e.g. the gateway's JGroups
  `DistributedCommandBusProperties`). Keep slice-test classes flat (see `ShowcaseApiControllerCT`).
- Testcontainers 2.0.5 moved `PostgreSQLContainer` from `org.testcontainers.containers` (now a deprecated shim) to the
  non-generic `org.testcontainers.postgresql.PostgreSQLContainer` — use the new import without the `<?>`/`<>` type
  arguments.
