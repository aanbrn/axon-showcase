# Test the build-logic rule sets and unify the version comparison

## Why

Three of `build-logic`'s rule sets have no tests, and they are the parts that decide what the update checks and the
infra-image gate report. The version comparator is worse than untested: it exists in three copies that disagree, because
only the buildpack copy applies a trailing-length tiebreak at the end of the comparison. `1.2.0` therefore counts as
newer than `1.2` under the buildpack and tooling copies but not under Helm's, so a pin spelled without its trailing zero
— `5.15` against a provider publishing `5.15.0` — reports a phantom update for the same release. Nothing in the build
catches any of this: `ModuleDependencyRules` and the tooling patterns are tested, these are not.

## What Changes

- **`build-logic/src/main/kotlin/Versions.kt`** (new) — one `internal object Versions`: `isNewer` (numeric segments,
  zero-padded, so `1.2` and `1.2.0` are equal), `compare` (numeric, then the longer spelling wins, which is the
  canonical-tag selection the buildpack alias rule needs), `numericParts` (a leading `v` is stripped), `leadingInteger`,
  and `highest` (the maximum of a tag list).
- **`build-logic/src/main/kotlin/ToolingUpdatesTask.kt`** — the task uses `Versions`; `ToolingVersions` is deleted and
  its comparison cases move out of `ToolingUpdatesTaskTests.kt` into `VersionsTests.kt`, while `ToolingJson` (the
  provider patterns and the token) stays and keeps that class's JSON guards.
- **`build-logic/src/main/kotlin/BuildpackUpdatesTask.kt`** — the task keeps only the HTTP query: the Docker Hub `name`
  pattern becomes a named constant that tolerates a space after the colon (Docker Hub answers compact today — verified
  with a live request — so this is hardening, not a defect fix) and the tag selection moves to `Versions.highest`.
- **`build-logic/src/main/kotlin/HelmUpdatesTask.kt`** — the private comparator, `numericParts` and `leadingInteger` are
  deleted, and the same-major filter is extracted into a new `build-logic/src/main/kotlin/HelmUpdateRules.kt`, mirroring
  `ModuleDependencyRules`.
- **`build-logic/src/main/kotlin/InfraImageVersionRules.kt`** (new) — the pure rules behind
  `VerifyInfraImageVersionsTask`: the top-level `image:`/`tag:` parse, the segment count, the floating-reference
  rejection and the truncation match, each returning its message, so the task becomes the adapter that runs Helm and
  walks the values files.
- **`build-logic/src/test/kotlin/VersionsTests.kt`**, **`InfraImageVersionRulesTests.kt`**,
  **`HelmUpdateRulesTests.kt`**, **`BuildpackUpdatesTaskTests.kt`** (new) — the ordering and equivalence cases, the
  top-level parse, the truncation cases, the floating rejection, the same-major filter, and the Docker Hub pattern
  against a real (trimmed) response body.

## Impact

- **Build**: `build-logic` sources are reorganized for testability (`internal` rule objects, tasks as adapters). No
  task's name, inputs, outputs or registration changes, and `check` keeps running `:build-logic:test` as it does now.
- **Tests**: four new test classes in `build-logic/src/test/kotlin/`, all unit tier (`Tests` suffix, `@DisplayName` on
  each class and method per the test conventions).
- **Behavior**: one intended change — a numerically equal version in a different spelling no longer reads as an update.
  The reported tag still prefers the longer spelling, so the documented alias behavior is preserved. The existing live
  reports (`toolingUpdates`, `helmUpdates`, `buildpackUpdates`) are compared before and after as evidence.
- **Deployment**: none.

## New Capabilities

None. `build-logic` is one of the modules that carry no capability (its rule sets are specified through the gates and
checks that use them), and no spec describes these task internals.

## Modified Capabilities

None. The quality capabilities describe the checks' observable behavior, which this change preserves — the comparator
unification removes a phantom update rather than altering what a check is for. The change is therefore `skip_specs`.
