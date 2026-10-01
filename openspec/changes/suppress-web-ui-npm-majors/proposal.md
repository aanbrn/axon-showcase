# Proposal: Filter deferred majors out of the web UI's npm update report

## Why

The `npmOutdated` report forwards `npm outdated`'s table whole, so the weekly "Dependency updates" issue lists every
available update — including majors the project cannot take. Today that is `typescript` `6.0.3` → `7.0.2`, deferred
because `typescript-eslint` caps TypeScript at `<6.1.0`; the JVM side solved exactly this with
`config/dependency-updates/major-disabled.txt`, but the web UI has no equivalent, so the row returns every week.

## What Changes

- `config/web-ui-updates/major-disabled.txt` (new) — the web UI's suppression list, one npm package per line, shipped
  with `typescript` and a pointer to its rationale.
- `build-logic/src/main/kotlin/frontend-conventions.gradle.kts` — the `npmOutdated` task filters the report it already
  writes: a package listed there appears only when its newest **same-major** update is available, so a suppressed major
  is dropped while its minor/patch updates stay reported.
- `build-logic/src/main/kotlin/NpmOutdatedRules.kt` (new) + `build-logic/src/test/kotlin/NpmOutdatedRulesTests.kt` (new)
  — the report filter as a pure, unit-tested rule (the repo's pattern for a rule with cases worth pinning).
- `showcase-web-ui/scripts/outdated-report.sh` — writes the raw table to a separate file the filter reads, so the
  rendered report keeps its existing shape.
- The `dependency-management` capability gains the rule: a package may be suppressed from the web UI report, and its
  same-major updates still surface.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Unexplained design intent is surfaced for clarification" requirement enumerates
  the suppression lists the architecture-auditor sweeps, so it gains the web UI's list (`MODIFIED`; the requirement's
  behavior is unchanged).
- `showcase/quality/dependency-management`: the "Web UI dependency update reporting" requirement gains the suppression
  behaviour (a listed package's major-only rows are dropped; its same-major rows are kept), so the report matches the
  JVM report's contract.

## Impact

- **Build**: `build-logic` gains the filter and its tests; the web UI module's report gains the suppression.
- **CI / tooling**: the weekly "Dependency updates" issue stops listing the deferred `typescript` major; the workflow,
  the issue body, and the JVM report are otherwise unchanged.
- **Deployment**: none.
