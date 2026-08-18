# Proposal: Improve api-gateway coverage

## Why

`showcase-api-gateway` reports 53% coverage, below the 0.80 gate baseline, so its coverage gate is disabled. Its main
uncovered surface is the `ShowcaseApiApplication` bean wiring (JGroups connector, distributed command bus, cache beans,
cache customizer, security filter chain) plus the controller's validation-error mapping.

## What Changes

- Add an integration test (`ShowcaseApiApplicationIT`, new `integrationTest` suite) that boots the full gateway Spring
  context and verifies the `ShowcaseApiApplication` bean wiring: JGroups connector, primary distributed command bus,
  `CaffeineCacheManager` custom caches, security filter chain, and the two caches.
- Extract the validation-exception → problem-detail mapping (the `HandlerMethodValidationException` visitor plus the
  `WebExchangeBindException` mapping) from the controller into `ShowcaseApiErrorResolver`, covered by a component test
  (`ShowcaseApiErrorResolverCT`) that exercises every visitor method.
- Replace the mocked async caches in `ShowcaseApiControllerCT` with real in-process Caffeine caches, asserting cache
  contents instead of mock interactions.
- Re-enable the module's coverage gate (remove `extra["coverage.gate.enabled"] = false`) once above 0.80.
- Verify `./gradlew :showcase-api-gateway:jacocoTestReport` shows >= 80% (~82%) and the gate passes.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — test-only; no spec-level behavior change, so `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- New integration/component tests under `showcase-api-gateway/src/`; the gate opt-out removed from
  `showcase-api-gateway/build.gradle.kts`.
- One behavior-preserving production change: the controller's validation-error mapping is extracted into the
  `ShowcaseApiErrorResolver` component. No runtime behavior changes.
