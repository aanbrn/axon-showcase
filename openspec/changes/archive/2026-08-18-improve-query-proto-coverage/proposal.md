# Proposal: Improve query-proto coverage

## Why

`showcase-query-proto` reported 26% coverage, far below the 0.80 gate baseline, so its coverage gate was disabled. The
low number was not a lack of tests — the hand-written `QueryMessageRequestMapper` is well covered by its component
test — but the generated Protobuf classes (`QueryProto`, `QueryRequest`, and their nested builders) being counted as
0%-covered. Excluding generated code and re-enabling the gate makes the module's coverage reflect its real logic.

## What Changes

- Extend the `code-coverage-conventions` plugin so a module can add its own generated-class excludes via an extra
  property (`coverage.generatedClassExcludes`), merged with the default protobuf patterns.
- Set `showcase-query-proto`'s generated-class excludes to `**/QueryProto*.class` and `**/QueryRequest*.class` (catching
  the generated outer class, message, and nested builders).
- Re-enable the module's coverage gate (remove `extra["coverage.gate.enabled"] = false`).
- Verify the module reports ~100% and its gate passes at the 0.80 baseline.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — test/build-infrastructure only; no spec-level behavior change, so `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- `build-logic/src/main/kotlin/code-coverage-conventions.gradle.kts` — module-extensible generated-class excludes.
- `showcase-query-proto/build.gradle.kts` — the generated-class excludes; the gate opt-out removed.
- No application code or runtime behavior changes.
