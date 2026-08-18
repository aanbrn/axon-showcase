## Context

See proposal.md - Why. The `build-logic` directory defines shared conventions applied to all 18 JVM modules. There was
no code-coverage measurement at all: no JaCoCo plugin, no report task, no baseline. Coverage must be added in a way
that fits a multi-module monorepo with four test tiers (`test`, `componentTest`, `integrationTest`, `e2eTest`) and a
mix of logic-heavy services and near-getter data-model modules.

## Goals / Non-Goals

**Goals:**
- Stand up JaCoCo coverage with per-module and aggregate reports.
- Provide a regression gate wired into `check` at a meaningful threshold.
- Keep the setup opt-in so modules without meaningful coverage do not inherit a meaningless gate.

**Non-Goals:**
- Enforcing a uniform threshold on every module (rejected — data models and services differ too much).
- Excluding every generated artifact perfectly (protobuf message classes have arbitrary names).
- Raising the threshold to a target in this change; the below-baseline modules are covered by a follow-up change.

## Decisions

**D1: Separate opt-in `code-coverage-conventions` plugin, not mixed into `java-conventions`.**
Modules apply `id("code-coverage-conventions")` where coverage matters. Alternatives: centralize in `java-conventions`
(rejected — would force a gate on data-model modules) and per-module JaCoCo wiring (rejected — repetitive).

**D2: Explicit `plugins { jacoco }` block + typed `tasks.named<JacocoReport>` accessors.**
The typed task accessors (`tasks.jacocoTestReport`) are not generated for precompiled script plugins' JaCoCo tasks, so
the convention uses `tasks.named<JacocoReport>("jacocoTestReport")`. The JaCoCo version is managed in the version
catalog. Long `executionData`/`classDirectories` expressions are wrapped for readability.

**D3: Full-coverage gate (unit + component + integration), not a unit/component-only gate.**
Integration-heavy services (e.g. command-service) show ~5% fast-tier coverage because their logic is integration-
tested; a fast-tier gate would be meaningless. The gate therefore measures full local-tier coverage, accepting that
`check` (which already runs integration) needs Docker.

**D4: One uniform baseline (0.80) + per-module gate opt-out.**
Rather than per-module baselines, a single baseline is enforced, and modules below it set
`extra["coverage.gate.enabled"] = false` (report still generated). This keeps the gate simple while exempting the
below-baseline modules as visible improvement targets. The flag is read lazily so a module's setting is honored.

**D5: Generated-code exclusion (protobuf patterns).**
Generated classes should not count against coverage. The convention excludes `*Proto`, `*OrBuilder`, `*OuterClass`,
`*Grpc` from `classDirectories` (report and gate). Limitation: protobuf message classes with arbitrary names (e.g.
`QueryRequest`) are not caught by these patterns.

**D6: Root aggregate report (`jacocoRootReport`).**
Merges all opted-in modules' exec/source/class dirs into one HTML+XML report, giving a project-wide number.

**D7: Removed redundant hardcoded accessor-hash imports in `code-check-conventions`.**
The `gradle.kotlin.dsl.accessors._<hash>.errorprone/spotbugs/spotbugsPlugins` imports were redundant (the plugins block
provides the accessors) and broke whenever the build-logic plugin set changed. Removing them fixes the latent fragility
that previously blocked using an explicit `jacoco` plugins block.

## Risks / Trade-offs

- [Uniform threshold does not fit data-model vs service coverage] → Mitigation: opt-in convention plus per-module gate
  opt-out for below-baseline modules.
- [Gate requires Docker (integration tests)] → Mitigation: `check` already runs integration, so no new burden.
- [Generated protobuf message classes are not fully excluded] → Mitigation: their coverage still counts in the module
  number; below-baseline modules are exempt from the gate.
- [Coverage measurement variance could flake the gate] → Mitigation: baseline set below the gated modules' measured
  coverage.

## Migration Plan

Build-infrastructure only: no runtime rollout or data migration. Any module that applies the convention gets the
reports and (unless opted out) the gate; rollback is removing the convention from a module.

## Open Questions

None. The follow-up — add tests to the four below-baseline modules (`api-gateway`, `query-service`, `query-client`,
`query-proto`) and re-enable their gates — is a separate change.
