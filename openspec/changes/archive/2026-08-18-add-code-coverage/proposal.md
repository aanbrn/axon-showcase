# Proposal: Add code coverage with JaCoCo

## Why

The repository had no code-coverage measurement at all: no JaCoCo plugin, no report task, no baseline. For a reference
application with an explicit test-tier model, coverage is a core quality signal — it shows which modules are thin and,
with a gate, prevents silent regression. This change stands up measurement and a regression gate.

## What Changes

- Add a separate, opt-in **`code-coverage-conventions`** build convention that applies JaCoCo and configures coverage
  tasks, so modules choose to participate rather than every JVM module inheriting it.
- Apply the convention to the modules where coverage matters: the four services (`command`, `query`, `projection`,
  `api-gateway`), the two clients, the logic libraries (`resilience4j-extension`, `mapstruct-extension`,
  `identifier-extension`, `showcase-test`), and `query-proto` (report only, gate disabled). The remaining data-model
  modules (`command-api`, `query-api`, `projection-model`) are left out — mostly getters/DTOs with inherently low
  coverage.
- Generate a **per-module report** covering the unit + component + integration test tiers (`jacocoTestReport`),
  aggregated from all `.exec` files in the module's `build/jacoco/`, excluding generated code (protobuf patterns).
- Add a **root aggregate report** (`jacocoRootReport` in the root build) merging all opted-in modules' exec, source,
  and class dirs into a single project-wide number (HTML + XML).
- Add a **coverage gate** (`jacocoTestCoverageVerification`) wired into `check`, enforced at a single baseline
  (`config/jacoco/coverage-baseline.properties`, `coverage.instruction.minimum = 0.80`). A module can opt out of the
  gate per-module via `extra["coverage.gate.enabled"] = false` (report still generated); this is set for the modules
  currently below 0.80 (`api-gateway` 53%, `query-service` 71%, `query-client` 79%, `query-proto` 26%), which remain
  visible report targets. A follow-up change adds tests to those modules and re-enables their gates.
- Note: the gate measures **full** coverage (all local tiers, incl. integration via Testcontainers) — a
  unit/component-only gate was rejected because integration-heavy services show misleadingly low fast-tier coverage.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — build-infrastructure only; no spec-level behavior change, so `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- New `build-logic/src/main/kotlin/code-coverage-conventions.gradle.kts` — JaCoCo application, report and gate config,
  generated-code exclusion, and the per-module gate opt-out.
- The opted-in modules' `build.gradle.kts` — `id("code-coverage-conventions")`; the four below-baseline modules also
  set `extra["coverage.gate.enabled"] = false`.
- Root `build.gradle.kts` — the `jacocoRootReport` aggregate task.
- New `config/jacoco/coverage-baseline.properties` — the coverage floor (0.80).
- Per-module reports under each opted-in module's `build/reports/jacoco/`; the aggregate under
  `build/reports/jacoco/jacocoRootReport/`; `check` enforces the baseline on gated modules.
- No application code, dependency, or runtime behavior changes.
