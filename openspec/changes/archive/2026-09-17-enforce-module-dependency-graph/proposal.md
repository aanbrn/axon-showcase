## Why

The architecture has a **decide** layer (`docs/adr/`), a **describe** layer (the capability specs), and a **review**
layer (the `architecture-auditor` subagent) — but no **enforce** layer: nothing fails the build when a
`build.gradle.kts` adds a dependency the structure does not sanction. An audit already caught `showcase-query-api` →
`showcase-command-api` and the correction was deferred, with no artifact recording whether that direction is allowed
(issue #262).

## What Changes

- A `build-logic` task validates the **module dependency graph** — the declared `project(...)` edges across all
  subprojects — against the structure's forbidden edges, failing `check` and naming the offending edge.
- The forbidden edges are the ones the repo's own documents record: no module depends on a service application; an
  `-extension` module stays a leaf; a contract module (`-api`, `projection-model`, `query-proto`) gains no consumer-side
  dependency.
- The rule logic is a **pure, unit-tested object**, so the validation is verifiable without running Gradle — and it
  gives `build-logic` its first tests, wired into the root `check` so they actually run.
- An **ADR** records the sanctioned graph, which is the decision the audit asked for, including the `query-api` →
  `command-api` direction.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/code-quality`: a new requirement that the build enforces the module dependency graph, naming the
  offending edge and failing `check`.

## Impact

- `build-logic/src/main/kotlin/` — the task and its pure rule logic; `build-logic/src/test/kotlin/` — its first tests,
  with `build-logic/build.gradle.kts` gaining the test dependency and its `check` wired into the root's, since
  `build-logic` is an included build whose tasks the root `check` does not otherwise reach.
- `build.gradle.kts` — the root gathers each subproject's declared project edges into the task's inputs and wires the
  task into `check`.
- `docs/adr/` — a new ADR recording the sanctioned directions and the `query-api` → `command-api` decision.
- `openspec/specs/showcase/quality/code-quality/spec.md` — synced at archive.
- No runtime, service, or deployment change: the rules lock in properties that are already true.
