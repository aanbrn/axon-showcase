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
- Python 3 (for `./scripts/setup-idea.sh`'s inspection-profile upsert; macOS ships it via Command Line Tools)

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

# Run end-to-end tests (separate opt-in task; boots services + infra via Testcontainers, builds service images)
./gradlew :showcase-api-gateway:e2eTest

# Check runs: compile → spotless/checkstyle/spotbugs/errorprone → test → componentTest → integrationTest
# (add -PskipITs to drop integration for a Docker-free check; e2e is never part of check)
./gradlew :showcase-command-service:check

# Load tests (Gatling)
./gradlew :load-tests:test

# Dependency security scan (Snyk; requires the Snyk CLI on PATH, not part of check)
./gradlew dependencySecurityCheck
# The scan passes --policy-path=.snyk (the root Snyk policy). Suppressed findings are tracked
# there with a short-term expires (2026-11-28, quarterly) so they re-surface if not resolved in time:
# the Spring Framework 6.2.x / Spring Security 6.5.x cluster is fixed only by the deferred
# Spring Boot 4 migration (ADR-0004). The checkstyle tool (13.11.0) no longer carries any vulnerable
# transitives, so no tooling findings remain to suppress. See the /dependency-security-check command
# for the version-pinned ignore format and the Snyk rate-limit gotcha: the free org allows 200 Open
# Source tests/billing period, counted only for manifests with identified vulnerabilities — so
# the policy-suppressed task consumes no quota (a passing scan works even once the limit is
# exhausted by unfiltered runs), and only raw `snyk test` runs that find issues hit the cap.

# Dependency update report (only catalog-owned coordinates; majors suppressed for groups in
# config/dependency-updates/major-disabled.properties)
./gradlew dependencyUpdates
```

The `/dependency-updates` opencode command runs this task and summarizes the available updates; the
`/gradle-update` command updates the Gradle wrapper to the latest stable version when one is available, and the
`/opsx-tool-update` command regenerates the OpenSpec command/skill instruction files after a new `openspec` CLI
release.

Build-environment constraints can surface as spurious "current version" rows in the report: build tooling such as
SpotBugs publishes module constraints that `checkBuildEnvironmentConstraints` reads and reports as the current version.
For example, a `log4j-core [2.17.1 -> 2.26.1]` row appears even though `log4j-core` resolves to `2.26.1` everywhere —
`2.17.1` is the floor of an external Log4Shell guard published by `spotbugs-annotations`. These rows are a known
`gradle-versions-plugin` limitation, not real updates (see upstream ben-manes/gradle-versions-plugin#755); do not chase
them (see ADR-0007).

Major-blocking entries in `config/dependency-updates/major-disabled.properties` carry a pointer comment naming the
coordinate and its rationale; the authoritative reasoning for each suppressed coordinate lives in the
`showcase/quality/dependency-management` spec.

For calendar-versioned coordinates (leading segment is a 4-digit year, e.g. Spring `YYYY.MINOR.MICRO` such as
`reactor-bom 2025.0.7`), the report treats a change in the `YYYY.TRAIN` pair (the first two version segments) as a major
update — matching Spring's release-train definition where `2025.0` and `2025.1` are distinct trains — while a change
only in the service-release (third) segment within the same train is a minor/patch update. Semver coordinates keep the
leading-integer major comparison.

**Test suite order matters:** `test` → `componentTest` → `integrationTest` → `e2eTest`. `check` runs the first
three by default (`-PskipITs` drops integration for Docker-free runs); `e2eTest` is a separate opt-in task, and the
only e2e suite is `showcase-api-gateway`'s (it builds all four service images).

**Test tiers** — a test's tier is decided by its collaborators (what is real vs. faked), not by how long it takes to
run:

- **Unit** (`src/test/java`, suffix `Tests`): the subject under test is isolated — its collaborators are mocks/fakes, no
  Spring context. Verifies single-class logic (e.g. `KsuidIdentifierFactoryTests`).
- **Component** (`src/componentTest/java`, suffix `CT`): the subject is composed with real, in-process collaborators —
  real serializers, Axon `AggregateTestFixture`/`SagaTestFixture`, or a Spring context with WireMock — but external
  infrastructure is never started. Verifies a component behaves correctly against its real neighbors (e.g.
  `QueryMessageRequestMapperCT`, `ShowcaseAggregateCT`, `ShowcaseQueryClientCT`).
- **Integration** (`src/integrationTest/java`, suffix `IT`): real external infrastructure via Testcontainers
  (PostgreSQL, Kafka, OpenSearch). Verifies services against the real things they talk to.
- **End-to-end** (`src/e2eTest/java`, suffix `E2E`): a real deployed system is booted and exercised against
  all-real collaborators, transport-independent — HTTP for the gateway/query-service, the distributed command bus
  (JGroups) for the command-service. The gateway e2e boots the full four-service pipeline and verifies cross-service
  propagation over the full command → Kafka → projection → query pipeline (e.g. `ShowcaseApiGatewayE2E`).

`disable-axoniq-console-message=true` is set both in integration tests and in each service's main application source
(e.g., `ShowcaseApiApplication.java`).

**DB scripts** — before running the command-service standalone (outside Docker), ensure the PostgreSQL event store is
initialized:

```bash
./db.sh init   # creates user `showcase` and database `showcase-events` if absent
./db.sh reset  # drops the database and recreates it
```

## Continuous Integration

`.github/workflows/ci.yml` runs a single `build` job on every pull request and every push to `main`:

- **Pull requests** run the Docker-free fast gate: `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` plus
  `openspec validate --all` — the coverage gate is disabled because the 0.80 baseline is calibrated on
  integration-test coverage, which PRs skip by design.
- **Pushes to `main`** run the full gate: `./gradlew check` (with integration tests and the coverage gate) plus
  `openspec validate --all`.

The job uses `gradle/actions/setup-gradle` to restore the Gradle User Home (dependencies, wrapper, and local build
cache) across runs — it never caches workspace `build/` directories, since stale `jacoco` exec data would corrupt the
coverage gate. The `main-required-checks` branch ruleset requires the `build` check for every merge into `main`, with
no bypass actors.

`.github/workflows/e2e.yml` runs the heavy end-to-end suite (`:showcase-api-gateway:e2eTest`, which builds all four
service images and boots the full pipeline) on a nightly schedule and via `workflow_dispatch`. It is observational —
never a merge gate, no secrets, and it shares the same `gradle/actions/setup-gradle` caching rules as `ci.yml`.

`.github/workflows/snyk.yml` runs the credentialed dependency security scan (`./gradlew dependencySecurityCheck`, all
sub-projects with the root `.snyk` policy) on a weekly schedule and via `workflow_dispatch`, authenticated with the
`SNYK_TOKEN` secret. It is observational — never a merge gate.

`.github/workflows/dependency-updates.yml` runs the Gradle dependency update report (`./gradlew dependencyUpdates`)
on a weekly schedule and via `workflow_dispatch`, opening or updating the "Dependency updates" issue with only the
actionable sections of `build/dependencyUpdates/report.txt` (stable catalog updates + the Gradle wrapper status)
using the `GITHUB_TOKEN` (`issues: write`). When there are actionable updates it posts a comment mentioning the
repository owner (so they are notified); runs with no updates update the issue silently. It is observational — never
a merge gate.

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
- **Checkstyle**: style gate wired into `check` via `code-check-conventions.gradle.kts`; ruleset at
  `config/checkstyle/checkstyle.xml`, generated sources excluded via `config/checkstyle/suppressions.xml`
- **SpotBugs**: finds bugs with findsecbugs and fbContrib plugins; uses `spotbugs-include.xml` and
  `spotbugs-exclude.xml` filters in `config/spotbugs/` if present (see `code-check-conventions.gradle.kts`)
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
  `-XX:+AllowRedefinitionToAddDeleteMethods` and `-XX:+EnableDynamicAgentLoading` (e.g. the query-client
  `componentTest` and the gateway `e2eTest` suites); leave them off suites that don't (e.g. a `componentTest` with only
  an `ApplicationContextRunner` test)
- **Asserting log output**: use `OutputCaptureExtension` (`CapturedOutput`) when the code under test runs **in the
  test JVM** (e.g. `ShowcaseProjectorIT`'s projector logging, `ShowcaseApiControllerCT`'s gateway fallback logging).
  It cannot capture a separate process's output — to assert a **containerized** service's logs (the code-under-test
  runs in a different JVM), collect them via `withLogConsumer` into a `static StringBuilder` and poll it, as the
  command-client e2e did before the suite was consolidated (see `69f2811`)
- **`@DirtiesContext`**: add it only where a full-context boot leaks global JVM state — JGroups (ports and system
  properties) and JCache (a JVM-global cache manager). Contexts that are safely cacheable don't need it: service slices,
  and `@Nested` classes with distinct `@ActiveProfiles` (which already get separate cached contexts). Keep it on the
  gateway/command-service full-context ITs (and the gateway e2e test, which pulls in JGroups); drop it
  elsewhere
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
- **No comments** in source code (per project convention). The sole exception is the `// SPDX-License-Identifier: MIT`
  header that Spotless enforces on every Java file (the project is MIT licensed; see the LICENSE file)
- **Javadoc**: classes, methods, and fields carry a Javadoc comment describing their purpose (see
  `ShowcaseApiErrorResolver`, `ShowcaseApiController`); wrap at 120 characters
- **Avoid redundancy**: don't write redundant code — e.g. redundant `throws` clauses on test methods, explicit type
  arguments that diamond inference or target typing resolve, or repeated boilerplate that Lombok covers. Use the
  simplest construct that compiles and stays readable
- **Formatting**: format Java sources and Gradle Kotlin DSL (`*.gradle.kts`) files with `./gradlew spotlessApply`
  (Spotless: palantir-java-format for Java, ktfmt for `.gradle.kts`, both fixed 120 columns) — the canonical format
  step, enforced by `spotlessCheck` in `check` with no IDE required. After each edit, run `spotlessApply` (via the
  `codefmt` skill's Spotless path) before reporting the change done; the IntelliJ formatter is no longer canonical,
  and import order is owned by the formatter.
  - The 120-character wrapping convention still applies manually to content the formatter does not touch (Markdown,
    YAML, and so on); verify with `awk 'length > 120'` over edited files.
  - For assertion lambdas inside `argumentSet(...)` parameterized sources, prefer a block lambda body
    (`(x) -> { ... }`) so the formatter indents the statements normally instead of deep-aligning one long expression.
    The resulting "Statement lambda can be replaced with expression lambda" inspection is suppressed with
    `@SuppressWarnings("CodeBlock2Expr")` on the source method (the correct token — not `StatementLambdaInspection`).
- **IDE inspections (optional)**: the build gates are the canonical verification — after each edit, run
  `./gradlew spotlessApply` and the touched module's quality gates (`compileJava`/`check`); no IDE is required. If the
  IDE is available, you may additionally run its inspections on the touched files (through the Steroid MCP
  `steroid_execute_code` / `runInspectionsDirectly`) and fix warnings, but this is not required and never a gate.
  Prefer assertions like `assertThat(x).isNotNull()` over `Objects.requireNonNull(x)` when guarding nullable values in
  tests, since the IDE recognizes them for dataflow.
- **Vendored agent skills**: the three `axon4to5-*` skills under `.opencode/skills/` are vendored from the
  `AxonIQ/agent-skills` repository, plugin `axoniq-migration` version 0.2.2 (Apache-2.0), copied verbatim from
  `plugins/axoniq-migration/skills/`. To refresh, re-copy the skill directories from that upstream tree at the
  desired plugin version and update the recorded version here and in the `showcase/quality/agent-skills` spec — a
  deliberate, reviewed change, not silent drift.

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

**Infra image versions are single-sourced** in `gradle/libs.versions.toml`: `*-image-tag` coordinates
(`postgres-image-tag`, `kafka-image-tag`, `opensearch-image-tag`) for the official Docker Hub images used by
docker-compose and the Testcontainers IT/e2e suites (`postgres`, `apache/kafka`, `opensearchproject/opensearch`;
postgres omits a trailing `.0`), and pinned `bitnami-*` chart versions (`bitnami-postgresql`, `bitnami-kafka`,
`bitnami-opensearch`) for the Helm deployment. Each chart ships its own preconfigured `image.tag`, which the Helm
charts deploy as-is — no `image.tag` override in build logic or values files. To bump an infra component, update its
`*-image-tag` and/or its `bitnami-*` chart version together. The `verifyInfraImageVersions` task (part of `check`)
derives its checks from the actual `helm.releases` container, resolves each pinned chart's preconfigured `image.tag` via
the Helm CLI (`helm show values bitnami/<chart> --version <pinned>`, using the plugin-managed client and
`helmUpdateRepositories`' TTL-cached repo index), fails the build if its app version drifts from the `*-image-tag`
after stripping trailing `.0` zero-padding from both sides (so `17.6` vs `17.6.0`, or `17` vs `17.0.0`, are
equivalent), and fails if any infra values file (`helm/values/*/values*.yaml`) pins `image.tag` —
so the repo cannot reintroduce a separate override. Deriving the checks from the configured releases means renaming an
infra release retargets its check and removing one drops it. External `image.tag` overrides at deploy time (e.g. `--set`
in a release pipeline) are outside this in-repo gate.

**Kafka 3.9.0 Testcontainers note**: Kafka 3.9.0 has a validation bug (KAFKA-18281) that rejects Testcontainers'
default listener config (`0.0.0.0` binds). The `KafkaContainer` usages in the IT/e2e suites override
`KAFKA_LISTENERS` to `PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094` (empty hosts make the listeners implicit)
so 3.9.0 starts; keep that override when bumping the Kafka image tag.

## Key Environment Variables

- `DB_PASSWORD=showcase` — PostgreSQL password for command-service
- `BPL_DEBUG_ENABLED=true` / `BPL_DEBUG_PORT=8000-8003` — JVM debug (JDWP) agent ports (`8000`–`8003` are the published
  debug ports in `docker-compose.yml`; see Local Development)
- `THC_PATH=/actuator/health` — health check path
- `JAVA_OPTS=-XX:MaxDirectMemorySize=128m -XX:MaxGCPauseMillis=20` — container JVM tuning

## Gotchas

- IntelliJ's built-in formatter (its `Default` code style) disagrees with the Spotless format (palantir for Java,
  ktfmt for `.gradle.kts`), so the auto-reformat triggers (**Actions on Save → Reformat code / Optimize imports**,
  **Auto Import → Optimize imports on the fly**) only cause drift if the **palantir-java-format**/**ktfmt** plugins
  are not active. The repo's IntelliJ config is **not versioned** — `.idea/` is git-ignored entirely. Run
  `./scripts/setup-idea.sh` (locates the IDE, runs `installPlugins` for both plugins, and writes the project config
  from the committed templates in `config/idea/`) so a fresh clone gets a formatter-matched IDE after one run. The
  ktfmt config uses the plugin's **Custom** style configured to reproduce ktfmt's kotlinlang style at 120 columns with
  unused-import removal, because the plugin's `Kotlinlang` mode hard-codes ktfmt's 100-column default and ignores the
  line-length option (see README → Local Development → IntelliJ IDEA Setup).
- **palantir-java-format does not manage imports**: since 2.47.0 the plugin only takes over **Reformat Code**, and
  `Optimize Imports` is always run by IDEA's native optimizer, governed by `.editorconfig` (the import layout
  `ij_java_imports_layout = $*,|,*` and `ij_java_use_single_class_imports=true` with the two on-demand counts at
  `999`). This is what stops IDEA collapsing to wildcard imports; without it, `spotlessApply` cannot auto-expand a
  wildcard (palantir never touches imports), so a wildcard must be expanded by hand or via Optimize Imports. The
  build gate is Spotless `forbidWildcardImports()` (fails on any `import x.*;`). A change to the code-style scheme
  requires an IDEA restart to take effect.
- E2E tests for `showcase-api-gateway` depend on Docker images of all other services being built (`bootBuildImage`). Run
  those first.
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
- Spring Data Elasticsearch's `DateFormat.strict_date_optional_time_nanos` maps to a **microsecond** Java pattern
  (`SSSSSS`, not 9 digits) despite its name — see upstream spring-data-elasticsearch#3334. `ShowcaseEntity` uses a
  custom `NANOS_DATE_PATTERN` (`yyyy-MM-dd['T'HH:mm:ss.SSSSSSSSSXXX]`) with `format = {}` instead; do not "simplify"
  it back to the built-in enum. The truncation is invisible on macOS (microsecond clocks) and surfaces only on
  nanosecond clocks (Linux CI).
- Custom Gradle test suites (`componentTest`, `integrationTest`, `e2eTest`) do not inherit the project's
  `implementation`-only dependencies — each suite re-declares what it needs (client component suites duplicate
  axon/opensearch/wiremock/resilience4j deps, and `showcase-query-proto` must be listed explicitly). A suite can be
  referenced in `shouldRunAfter(...)` only when bound as a `val` (e.g. `val integrationTest =
  suites.register<JvmTestSuite>("integrationTest")`).
- `@Nested` test classes are incompatible with Spring Boot slice tests (`@WebFluxTest`/`@WebMvcTest`): nested classes
  load the full application context instead of the slice and fail on infrastructure beans (e.g. the gateway's JGroups
  `DistributedCommandBusProperties`). Keep slice-test classes flat (see `ShowcaseApiControllerCT`).
- Testcontainers 2.0.5 moved `PostgreSQLContainer` from `org.testcontainers.containers` (now a deprecated shim) to the
  non-generic `org.testcontainers.postgresql.PostgreSQLContainer` — use the new import without the `<?>`/`<>` type
  arguments.
