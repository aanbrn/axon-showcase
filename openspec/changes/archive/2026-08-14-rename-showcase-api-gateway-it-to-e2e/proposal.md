## Why

`ShowcaseApiGatewayIT` boots all four services plus their infrastructure — it verifies cross-service behavior over the
full command → Kafka → projection → query pipeline, not a single service against its real dependencies. Per the project's
test-tier convention (real-vs-faked collaborators), this is an **end-to-end** concern, not an integration one. Calling
it an "integration test" sets wrong expectations about scope, flake budget, and cost.

## What Changes

- Rename the Gradle test suite `integrationTest` to `e2eTest` in `showcase-api-gateway/build.gradle.kts`
- Move `ShowcaseApiGatewayIT` from `src/integrationTest/java/showcase/api/` to
  `src/e2eTest/java/showcase/api/ShowcaseApiGatewayE2E.java` (suffix `E2E` instead of `IT`)
- Keep the suite's wiring intact: Testcontainers deps, `mustRunAfter` extraction of client integration tests,
  `dependsOn` the four `bootBuildImage` tasks, `disable-axoniq-console-message=true`
- Update AGENTS.md:
  - Check-runs and suite-order lines get `e2eTest` appended after `integrationTest`
  - Add an E2E tier to the Test Tiers block (`src/e2eTest/java`, suffix `E2E`, whole-system verification)
  - Add the suffix bullet "E2E test classes use the suffix `E2E`"
  - Reword the gotcha: "the `showcase-api-gateway` e2eTest must run after ..."

## Capabilities

### New Capabilities

_(none — no new behavior, test infrastructure only)_

### Modified Capabilities

_(none — `skip_specs: true` set in `.openspec.yaml`: no spec-level behavior changes)_

## Impact

- **Build**: `:showcase-api-gateway:e2eTest` becomes the new task name; `check` auto-includes it via the
  `dependsOn(testing.suites)` wiring in `java-conventions.gradle.kts`
- **Tests**: one file relocated and renamed; only the gateway module changes — other modules' `integrationTest` suites
  (command/query/projection services, both clients) are genuine integration tests and stay untouched
- **Invocation**: `./gradlew :showcase-api-gateway:e2eTest` (was `...:integrationTest`)
- **Docs**: AGENTS.md test-tier documentation becomes explicit about the E2E tier