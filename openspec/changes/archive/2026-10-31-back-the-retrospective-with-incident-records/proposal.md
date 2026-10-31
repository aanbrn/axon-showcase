## Why

The retrospective's contract tells the analyzer to reuse the units' `docs/process-defects/` records, but the digest it
is handed never carries them: the evidence arrives only when a session happens to read the repository, so a fresh pass
can skip it silently. The same latest report turns a count of rounds that raised a class into a share of compliance
misses, stating the window's top class at the wrong unit.

## What Changes

- `scripts/experience-analysis.sh` gains a window-scoped process-defect index in the derived-facts block: the unique
  record files a commit since the window start touched, each with its `###` incident headings read at the indexed
  revision, the count of files and headings, the revision, the `since`-date overlap, a selected path marked absent
  rather than dropped, and an explicit zero-selection line (never silent).
- `.opencode/agent/experience-analyzer.md` names the index as a supplied input and requires a causal claim that rests on
  a record to have read that record's body and to name it; it also states how to read the classes figure.
- `scripts/review-rounds.py` counts one contribution per canonical class per round, so two labels mapping to one class
  on one row no longer count twice, and its classes line states that unit.
- `scripts/test-review-rounds.py` carries the collapsing case and the unit wording as controls.
- `scripts/test-experience-analysis.sh` (new) drives the real digest entry point over a temporary git repository with a
  stubbed `gh`, discriminating the boundary day, a record two window commits touched, an older record updated in the
  window, an empty selection, a record with no heading, a path deleted in the window, and a non-ASCII path; it runs
  under `sh`, as the build's `shExecutable` task does.
- `build.gradle.kts` registers that test as a `check` verification task, following the existing shell-test pattern.
- `openspec/specs/showcase/quality/agent-skills/spec.md` gains a `MODIFIED` delta for "Experience analysis is available
  to agents": the derived-facts block carries the index, and the classes figure is a round occurrence.
- `docs/retrospectives/2026-10-31.md` corrects the four dependent occurrences to the unit the ledger supports and adds a
  dated correction note; the originally reported totals stay as recorded rather than being recalculated from the grown
  ledger.
- `AGENTS.md`, `README.md`, and `.opencode/commands/retrospective.md` describe the digest with the index included.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the derived-facts block's contents and the classes figure's unit.

## Impact

- Scripts: `scripts/experience-analysis.sh`, `scripts/review-rounds.py`, one new shell test.
- Agent tooling: the `experience-analyzer` definition and the `/retrospective` command.
- Build: one new `Exec` verification task wired into `check`; no dependency or plugin change.
- Specs: a `MODIFIED` delta on `showcase/quality/agent-skills` only; the main spec changes at archive.
- Records and docs: one retrospective's corrected figures, `AGENTS.md`, `README.md`.
- No runtime code, API, schema, image, chart, or deployment change.
