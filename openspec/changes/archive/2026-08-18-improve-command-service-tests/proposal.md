# Proposal: Add command application integration test and scheduler metrics coverage

## Why

`showcase-command-service` lacks a full-context test of the `ShowcaseCommandApplication` bean wiring (unlike the
gateway and query service), and `ShowcaseDbSchedulerMetrics`' metric-registration logic is uncovered. The integration
tests also still reference the deprecated `org.testcontainers.containers.PostgreSQLContainer`, which Testcontainers
2.0.5 moved to `org.testcontainers.postgresql`.

## What Changes

- Add `ShowcaseCommandApplicationIT`, an integration test booting the full command-service context (Testcontainers
  PostgreSQL) and verifying the `ShowcaseCommandApplication` bean wiring: JGroups connector, primary distributed
  command bus, saga store, caches, snapshot trigger, DB scheduler, and metrics beans.
- Replace the deprecated `org.testcontainers.containers.PostgreSQLContainer` with `org.testcontainers.postgresql`
  (non-generic) across the command-service, command-client, and api-gateway tests.
- Add `ShowcaseDbSchedulerMetricsCT`, a component test verifying the scheduler and execution metric registration
  against a real Micrometer `SimpleMeterRegistry`.
- Test-only; no application code changes.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — test-only; `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- New integration/component tests under `showcase-command-service/src/`; the deprecated Testcontainers
  `PostgreSQLContainer` import replaced in `showcase-command-service`, `showcase-command-client`, and
  `showcase-api-gateway` tests. No production code changes.
