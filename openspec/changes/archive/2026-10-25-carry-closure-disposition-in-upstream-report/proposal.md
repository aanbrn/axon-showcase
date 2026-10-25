# Proposal

## Why

The weekly report prints a closed reference as `closed <date>`, so a closure's _kind_ is invisible:
`warpdotdev/warp#6320` reads exactly like a fix closure although it closed as a duplicate of a still-open thread, and
four `anomalyco/opencode` references closed `not_planned` with no fix coming. The 2026-10-25 sweep therefore re-derived
every disposition by hand (`gh api` per reference), and the GitHub API had already returned it.

## What Changes

- `build-logic/src/main/kotlin/UpstreamReferenceState.kt` (new) — parses the API's `state` / `state_reason` /
  `closed_at` triple into the report's state vocabulary.
- `build-logic/src/main/kotlin/UpstreamReferencesTask.kt` — asks the API for the closure reason and renders the state
  through the new object.
- `build-logic/src/test/kotlin/UpstreamReferenceStateTests.kt` (new) — the disposition cases, including the absent
  reason.
- `openspec/changes/carry-closure-disposition-in-upstream-report/specs/showcase/quality/merge-governance/spec.md` (new)
  — `MODIFIED` delta for the report's state vocabulary.
- `README.md` — the update-check section's state enumeration gains the closure disposition.
- `AGENTS.md` and `.opencode/skills/setup-agent-tools/SKILL.md` — record the three opencode references that closed
  `not_planned`, so a later sweep does not re-derive them.
- `docs/ideas.md` — remove the implemented idea.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/merge-governance`: the upstream-reference report's reported state gains the closure's disposition,
  so a duplicate or a declined closure is distinguishable from a completed one.

## Impact

- The report row's state column changes from `closed 2026-10-11` to `closed (not_planned) 2026-10-11`; the workflow's
  filters match the `closed` prefix and its `unresolved` value, so they keep working.
- No service, API, dependency, or deployment change: one `build-logic` object, its task, and one unit test.
- The weekly issue's body now names each closure's disposition.
