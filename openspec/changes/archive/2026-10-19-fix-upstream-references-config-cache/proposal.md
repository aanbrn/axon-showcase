# Proposal

## Why

The upstream-reference report has been broken in CI since the configuration cache landed (#581, 2026-10-15).
`UpstreamReferencesTask`'s action reads `project.rootDir` at execution time, which the configuration cache rejects, so
the scheduled run fails in 36 seconds at its first Gradle step: the task throws "Invocation of 'Task.project' by task
':upstreamReferences' at execution time is unsupported with the configuration cache" and the entry is discarded. The
"Upstream references" issue (#343) has not been refreshed since 2026-10-10. No gate in `check` runs the task, and the
workflow is observational — never a merge gate — so nothing else reported it for a week.

## What Changes

- `build-logic/src/main/kotlin/UpstreamReferencesTask.kt` — the action relativizes each source file against a new
  `rootDirectory` property instead of reading `project.rootDir`, so it captures nothing belonging to the Gradle script.
- `build.gradle.kts` — the `upstreamReferences` registration sets that property from
  `rootProject.layout.projectDirectory`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. `merge-governance`'s "Upstream-reference report runs on a schedule and on demand" already requires the report to
run and to update the issue; the code violated that requirement, so this change restores conformance rather than
changing a contract, and sets `skip_specs: true`. No capability spec covers the configuration cache.

## Impact

- **Build / tests / services**: `build-logic` and the root build script only — no service, dependency, deployment, or
  runtime change. The report's content is unchanged, since both bases are the repository root.
- **Verification**: `./gradlew upstreamReferences` twice (a stored entry, then a reused one), the report's labels
  against the ones the pre-fix code wrote to issue #343, `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, and
  a dispatch of the `upstream-references` workflow.
- **Docs**: the per-unit capture's `AGENTS.md` edit, and the `docs/ideas.md` entry this change parks.
