---
name: jvm-testing
description:
  JVM test authoring, execution, and diagnosis for this repository — test display names, tier placement, Spring slice
  constraints, mocking, log capture, Testcontainers, BlockHound, the coverage gate, and the test commands.
---

# JVM testing

It carries the JVM testing content the root `AGENTS.md` used to hold; the root's classification table owns when it loads
and states this pack's trigger.

Source: `AGENTS.md` `## Conventions` (test display names, Spring bean mocks, test tier placement, nested resilience
groups, BlockHound, asserting log output, `@DirtiesContext`, code coverage), `## Build & Test` (test commands, test
suite order, test tiers), and `## Gotchas` → `### Testing, measurement & load verification` (the gateway e2e images,
`@Nested` with slice tests, Testcontainers).

For how to author a check or a test that can actually fail — the bad state, the control, the input set, the assertion —
load the `check-and-test-authoring` skill; this pack does not restate its modes.

## Test naming and placement

- **Test display names**: every test class and every `@Test`/`@ParameterizedTest` method (plus `@Nested` groups) carries
  a static-sentence `@DisplayName` (e.g., `@DisplayName("Showcase aggregate component tests")`,
  `@DisplayName("Finishing a showcase with a valid command succeeds")`). Do not use `{0}`-style placeholders — named
  `argumentSet("...", ...)` invocations already render their own detail. A display name that enumerates several cases
  must have the body assert every one of them — an enumerated name with one asserted case is an unverified claim. A
  contract-module test named "may not depend on a client or a service application" asserted only the client case, so the
  rule's second clause was dead and survived repeated review passes; IntelliJ's always-false warning caught it, not a
  test or a review. A test must also assert a decision the production code encodes, not a language operator or a case
  another test already covers: a helper that was only `name in set` (the real suppression policy lives at the call site)
  and a `BuildpackUpdatesTaskTests` case that duplicated a `VersionsTests` case verbatim both passed while verifying
  nothing — a reviewer caught both, and they were deleted rather than kept for the count. When a rule is a one-line
  wrapper of a language construct, test the caller's decision; before adding a case, check no existing test asserts it.
  A test written to guard a refactor must fail against the pre-refactor code — run it before the change (or with the old
  path temporarily restored) and confirm it goes red, since a test green on both sides verifies nothing and is deleted
  rather than kept for the count. The `ShowcaseRestController` cache-fallback refactor shipped under the existing 76
  scenario tests for this reason. An assertion whose shape must change — a matcher can no longer match exactly because
  the value it compares gained a member — must keep every subject the old matcher pinned: rewriting
  `expect(fetchMock).toHaveBeenCalledWith('<url>', { method: 'PUT' })` into field reads because the added `traceparent`
  header broke the exact match dropped the call's URL check at every rewritten site, and only the quick review caught
  it; assert the URL alongside the new field (`fetchMock.mock.calls[0][0]`), since a silently dropped subject leaves a
  test that still passes. A refactor can silently stop a test from exercising its subject: a function moved into a new
  module needs its tests moved with it (deleting the old test file left a re-created four-case event→predicate mapping
  with two cases covered), and a negative test driving a captured mock callback must assert the callback was captured —
  `callback?.()` no-ops when the wiring is absent, so the assertion passes vacuously. An error-translation chain must be
  tested by driving the whole method, not only its branches: `ShowcaseQueryTransportService.translate` re-mapped its own
  `INVALID_ARGUMENT` output to `INTERNAL` because the default arm caught the `StatusRuntimeException` the chain had just
  produced, and the per-branch cases were all green — only the component test exercising `dispatch` end to end saw it.
  captured: propagate-ui-trace-context (#362) captured: rethink-web-ui-reconciliation captured: grpc-query-transport
- **Spring bean mocks in tests**: use `@MockitoBean` (from `org.springframework.test.context.bean.override.mockito`),
  not the deprecated-for-removal `@MockBean` (`org.springframework.boot.test.mock.mockito`), which has been deprecated
  since Spring Boot 3.4
- **Test tier placement**: a test's tier is decided by its collaborators (see the tiers below). Verify the application's
  bean wiring (`@SpringBootApplication` config) at the **integration** tier via a real context boot — do not write
  component tests that mock the app's own collaborators. Component tests compose real in-process collaborators (e.g. a
  real mapper) with only external infrastructure faked. A test that subscribes to a stream which never completes (an SSE
  endpoint's `Flux`) is a **unit** test with a bounded subscription (`take(1)`, `blockFirst(timeout)`) — inside a
  `@WebFluxTest`/slice test the open exchange leaks and breaks unrelated cases in the same run (the keep-alive slice
  version failed 15 of 76 `ShowcaseRestControllerCT` cases; all 108 passed without it). captured: keep-sse-stream-alive
  (#301)
- **Nested test groups for resilience features**: a `@Nested` class that groups Resilience4j scenarios is named
  `<Feature>Behavior` (e.g., `TimeLimiterBehavior`, `RetryBehavior`, `CircuitBreakerBehavior`), both for uniformity and
  to avoid shadowing the library's `CircuitBreaker` type
- **BlockHound jvmArgs**: only suites whose tests call `BlockHound.install()` need
  `-XX:+AllowRedefinitionToAddDeleteMethods` and `-XX:+EnableDynamicAgentLoading` (e.g. the query-client `componentTest`
  and the gateway `e2eTest` suites); leave them off suites that don't (e.g. a `componentTest` with only an
  `ApplicationContextRunner` test)

- **Asserting log output**: use `OutputCaptureExtension` (`CapturedOutput`) when the code under test runs **in the test
  JVM** (e.g. `ShowcaseProjectorIT`'s projector logging). It cannot capture a separate process's output — to assert a
  **containerized** service's logs (the code-under-test runs in a different JVM), collect them via `withLogConsumer`
  into a `static StringBuilder` and poll it, as the command-client e2e did before the suite was consolidated (see
  `69f2811`). When an integration test fails with an opaque timeout, read the service-side log the runner captured into
  the test report's XML (`system-out`) before diagnosing the assertion — a 5s `TimeoutException` on a `WebTestClient`
  exchange hid the real `NoSuchMethodError`. captured: bump-dependencies-2026-09-25
- **`@DirtiesContext`**: add it only where a full-context boot leaks global JVM state — JGroups (ports and system
  properties) and JCache (a JVM-global cache manager). Contexts that are safely cacheable don't need it: service slices,
  and `@Nested` classes with distinct `@ActiveProfiles` (which already get separate cached contexts). Keep it on the
  gateway/command-service full-context ITs (each boots a JGroups-enabled service); drop it elsewhere
- **Code coverage**: JVM modules opt in via `code-coverage-conventions`. Coverage is measured per module with
  `jacocoTestReport` (unit + component + integration exec data) and aggregated with the root `jacocoRootReport`. The
  `jacocoTestCoverageVerification` gate is wired into `check` at the baseline in
  `config/jacoco/coverage-baseline.properties` and requires Docker (integration tests). A module can extend the
  generated-class excludes via `coverage.generatedClassExcludes`. The **web UI's** coverage is a separate Vitest/V8
  gate: `npmTestCoverage` (in the frontend `check`) enforces the statement minimum in
  `config/web-ui-coverage/coverage-baseline.properties` (fraction form, scaled to Vitest's percentage thresholds)

## Tiers and suite order

**Test suite order matters:** `test` → `componentTest` → `integrationTest` → `e2eTest`. `check` runs the first three by
default (`-PskipITs` drops integration for Docker-free runs — add `-Pcoverage.gate.enabled=false` to keep the coverage
gate green, see the coverage rule above and the coverage-gate gotcha in `gradle-build`); `e2eTest` is a separate opt-in
task (two suites — the gateway's and the web UI's; see the tiers below).

```bash
# Run tests for a single module
./gradlew :showcase-command-service:test

# Run component tests (faster, no containers)
./gradlew :showcase-command-service:componentTest

# Run integration tests (requires Docker, spins up Testcontainers)
./gradlew :showcase-command-service:integrationTest

# Run end-to-end tests (separate opt-in task; boots services + infra via Testcontainers, builds service images)
./gradlew :showcase-api-gateway:e2eTest
```

**Test tiers** — a test's tier is decided by its collaborators (what is real vs. faked), not by how long it takes to
run:

- **Unit** (`src/test/java`, suffix `Tests`): the subject under test is isolated — its collaborators are mocks/fakes, no
  Spring context. Verifies single-class logic (e.g. `KsuidIdentifierFactoryTests`).
- **Component** (`src/componentTest/java`, suffix `CT`): the subject is composed with real, in-process collaborators —
  real serializers, Axon `AggregateTestFixture`/`SagaTestFixture`, or a Spring context with a stubbed remote call — but
  external infrastructure is never started. Verifies a component behaves correctly against its real neighbors (e.g.
  `QueryMessageMapperCT`, `ShowcaseAggregateCT`, `ShowcaseQueryClientCT`).
- **Integration** (`src/integrationTest/java`, suffix `IT`): real external infrastructure via Testcontainers
  (PostgreSQL, Kafka, OpenSearch). Verifies services against the real things they talk to. A dependency bump of a
  runtime-path library is verified **here**: a same-major release can be binary-incompatible with another pinned library
  while compilation, the unit/component tests, and the Docker-free check all pass — `opensearch-java` `3.9.0`→`3.10.0`
  changed `Hit.matchedQueries()`'s return type, which `spring-data-opensearch` 2.x's `DocumentAdapters.from` calls — so
  run the full `check` with integration tests before reporting such a bump done. captured: bump-dependencies-2026-09-25
- **End-to-end** (`src/e2eTest/java`, suffix `E2E`): a real deployed system is booted and exercised against all-real
  collaborators, transport-independent — HTTP for the gateway's client-facing API, gRPC for the gateway → query-service
  read path, the distributed command bus (JGroups) for the command-service. The gateway e2e boots the full four-service
  pipeline and verifies cross-service propagation over the full command → Kafka → projection → query pipeline (e.g.
  `ShowcaseApiGatewayE2E`). The web UI e2e (`showcase-web-ui/e2e`, Playwright) boots the same pipeline via docker
  compose and drives the browser against it: create → appears, start → STARTED, saga auto-start reflected over SSE, live
  events appended to the timeline, and a duplicate title surfacing the gateway validation error.

## Test execution traps

- **The gateway e2e suite needs the four service images present in the local Docker daemon.** The Gradle
  `:showcase-api-gateway:e2eTest` task declares the four `bootBuildImage` tasks (gateway, command, projection, query) as
  `dependsOn`, so running it through Gradle builds them. An IDE JUnit run does not fire `dependsOn`, so before running
  the `ShowcaseApiGatewayE2E` class directly, build them:
  `./gradlew bootBuildImage :showcase-command-service:bootBuildImage :showcase-projection-service:bootBuildImage :showcase-query-service:bootBuildImage`.
  Verify the images exist for the version declared in `gradle.properties`
  (`docker images aanbrn/axon-showcase-command-service` lists the local tags); a missing image fails the suite at
  container start.

- **`@Nested` test classes are incompatible with Spring Boot slice tests (`@WebFluxTest`/`@WebMvcTest`).** Nested
  classes load the full application context instead of the slice and fail on infrastructure beans (e.g. the gateway's
  JGroups `DistributedCommandBusProperties`). Keep slice-test classes flat (see `ShowcaseRestControllerCT`).

- **Testcontainers 2.0.5 moved `PostgreSQLContainer` from `org.testcontainers.containers` (now a deprecated shim) to the
  non-generic `org.testcontainers.postgresql.PostgreSQLContainer`.** Use the new import without the `<?>`/`<>` type
  arguments. The old import still compiles through the shim, so a green build does not confirm the migration — check the
  import.
