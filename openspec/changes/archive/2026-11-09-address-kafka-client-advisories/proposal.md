# Proposal

## Why

The scheduled `Dependency Security` run `42413633653` (2026-11-07) failed on two high-severity
`org.apache.kafka:kafka-clients@3.9.2` advisories in 12 of 20 sub-projects. The 3.9.x line ends at 3.9.2, so it has no
patch; the fix starts at 4.2.2, while the dependency arrives from Spring Boot 3.5.16's BOM. A local probe (compile, the
unit and component suites, all three services' integration suites against `apache/kafka:3.9.0`, and the gateway e2e
suite over the full pipeline) passes with `kafka-clients` 4.4.0, so the fix is available without waiting for the
deferred Spring Boot 4 migration.

## What Changes

- `gradle/libs.versions.toml` — a new `kafka-clients = "4.4.0"` version entry and its `org.apache.kafka:kafka-clients`
  library entry, so the version is catalog-owned and `dependencyUpdates` tracks it.
- `platform/build.gradle.kts` — `api(libs.kafka.clients)` in the platform's `constraints` block, so every classpath that
  reaches `kafka-clients` resolves `4.4.0` instead of the BOM's `3.9.2`.
- `docs/adr/0031-*.md` — a new ADR recording the deliberate move ahead of Spring Kafka 3.3.16's supported
  `kafka-clients` line, its verification, and its `Revisit when:` gate, which owns the condition that supersedes it.
- `docs/ideas.md` — a parked entry for the broker-side exposure: the deployed `apache/kafka:3.9.0` image runs a release
  inside both advisories' affected ranges, so the broker-side code paths they describe are present, while the
  deployment's actual exposure is not assessed here; the newest Bitnami `kafka` chart in the local index pins Kafka
  `4.0.0`, still inside the ranges, so no chart bump fixes it today.
- `openspec/changes/address-kafka-client-advisories/specs/showcase/quality/dependency-security/spec.md` — a `MODIFIED`
  delta adding `org.apache.kafka:kafka-clients` to the constrained coordinates and a scenario for it.
- `.opencode/agent/review-quick.md` — the `record-shape` finding class joins the canonical list, because round 2 raised
  a finding (a unit record file that violates its checker's shape) that fitted no existing class, and the definition
  requires the calling agent to add one in the same change; the edit lands with the implementation, not during planning.
- `openspec/specs/showcase/quality/dependency-security/spec.md` — the capability `## Purpose` gains the new coordinate;
  a delta cannot carry a Purpose, so the edit rides the archive commit as its own task.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-security`: the "Vulnerable transitive dependencies are constrained to patched versions"
  requirement gains `org.apache.kafka:kafka-clients` at `4.4.0` or newer and the scenario that verifies it.

## Impact

- **No production code changes**: the constraint changes dependency resolution only, and no source file changes.
- **Consumers**: `showcase-command-service`, `showcase-projection-service`, and `showcase-api-gateway` (plus
  `showcase-test` and the modules that resolve the platform) move from `kafka-clients` 3.9.2 to 4.4.0.
- **Verification already measured on the probe**: `compileJava` succeeds, the three services' unit and component suites
  pass, their integration suites pass against `apache/kafka:3.9.0` — 36 command-service, 20 projection-service, and 11
  gateway tests, 0 failures — and the gateway e2e suite passes over the full command → Kafka → projection → query
  pipeline with the same client (29 tests, 0 failures); unchanged-tree control runs pass on the same environment.
- **Broker-side scope**: the deployed `apache/kafka:3.9.0` image runs a release inside both advisories' affected ranges,
  so the broker-side code paths they describe are present; the deployment's actual exposure is not assessed by this
  change and is parked as its own item.
- **Compatibility caveat**: Spring Kafka 3.3.16 is built against `kafka-clients` 3.8/3.9 and Axon Kafka 4.12.1 against
  3.x, so the combination is ahead of the supported line and is recorded as a deliberate decision in the ADR.
- **The scan is expected to go green**: after the constraint, `snyk test` should report no vulnerable path for these two
  advisories; the credentialed run in task 2.7 confirms it, and until that run is green the clearance is unconfirmed.
- **Review tooling**: `.opencode/agent/review-quick.md` gains one finding class, so a later review can classify a unit
  record that breaks its checker's shape; no spec enumerates the class list, so the edit owes no delta.
