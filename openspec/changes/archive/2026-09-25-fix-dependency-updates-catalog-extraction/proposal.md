# Proposal: Fix the dependency-updates catalog extraction

## Why

The `dependency-updates` workflow extracts its catalog section with `awk` matching
`The following dependencies have newer versions:` — a header the pinned `gradle-versions-plugin` 0.61.0 **never emits**.
Its section header is `The following dependencies have later <revision> versions:`, and the task's `revision` defaults
to `milestone`, so the report says `have later milestone versions:` and the workflow's section is always empty: 28
stable catalog updates go unreported. Both `milestone` and `release` produce the identical 28 rows (the repo's
`rejectVersionIf` already filters to stable), so this is an extraction/presentation defect, not a candidate-set one.

## What Changes

- `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts` — set `revision = "release"`, so the report's
  actionable section is unambiguously `The following dependencies have later release versions:` (also the plugin's
  getting-started example).
- `.github/workflows/dependency-updates.yml` — extract the emitted `have later release versions:` section instead of the
  non-existent `have newer versions:` one.
- `openspec/specs/showcase/quality/merge-governance` (delta) — the update-report requirement's "No stable updates"
  scenario names the report's real sections.
- `.opencode/commands/dependency-updates.md` — replace the "ignore the milestone section (never stable)" premise with
  the report's release section (the actionable set).
- `docs/ideas.md` — remove the implemented idea.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/merge-governance`: the update-report requirement's "No stable updates" scenario is corrected to the
  report's real section names (the stable-catalog section is `have later release versions:`, not a
  `have newer versions:` header that never exists).

## Impact

- **Build**: one line in `dependency-versions-conventions.gradle.kts` (`revision = "release"`); the report's candidate
  set is unchanged (verified — the same 28 rows).
- **Tests**: none beyond the normal gates.
- **Deployment / CI**: the tracker starts reporting the 28 stable catalog updates it previously dropped; observational,
  never a merge gate.
- **Note**: those 28 rows include the documented spurious `org.apache.logging.log4j:log4j-core [2.17.1 -> 2.26.1]` row —
  a `checkBuildEnvironmentConstraints` floor from `spotbugs-annotations`, not a real update (see ADR-0007 and the
  README/`AGENTS.md` notes). The report cannot distinguish it, so the issue will carry it; suppressing it would change
  the candidate set and is out of scope here.
