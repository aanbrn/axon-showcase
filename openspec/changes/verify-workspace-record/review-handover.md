# Review hand-over

## Round 1 — proposal

- **Unit**: `verify-workspace-record`; **base** `ca4cd1b1e436be6b76889f27feb1fc8aa4368a6e`; **state**: uncommitted
  working tree, staged, no commit; **tree**: `/Users/afanasyev/Development/axon-showcase-verify-workspace-record`.
- **Supplied artifacts**: the change dir — `proposal.md`, `design.md` (Context, Goals/Non-Goals, six decisions with
  rejected alternatives, Risks, an eleven-row claims table, per-kind dismissals), `tasks.md` (with the `Workspace:`
  record), and the delta spec — plus the `docs/ideas.md` removal of this unit's idea.
- **Stage**: proposal; no implementation yet.
- **Packs**: `gradle-build`; the `check-and-test-authoring` skill.
- **Already verified**: `openspec validate --all` 27 passed, 0 failed with no warning on this delta; `spotlessCheck`
  clean; `python3 scripts/commit-hygiene.py --ideas-sections` exit 0 after the removal; `--review-ready` reports the
  base contained and the index current.
- **The premise the design rests on**: most of the archive predates the rule (57 of 446 directories carry the record),
  so the verification's subject is the live change directory; and CI checks out a detached head under a runner path, so
  `path` and `branch` can only be judged by the commit guard.
- **Risk focuses**: (1) whether the live-only scope and the check/guard split are the right boundary, or one side is
  over- or under-scoped; (2) whether the record parser's "one logical line from the marker to the first blank line"
  covers the shapes actually recorded, and whether anchoring at the line's start keeps task prose out; (3) whether every
  delta scenario names a state a control can reach; (4) whether the claims rows and dismissals are derived rather than
  asserted.

## Rounds 2–7 — proposal, closed clean

- **Round 1** raised nine findings: the record grammar matched two shapes while the archive records three (plus a
  trailing period on some writers), no task registered the `MODE_TEST_COVERAGE` entry, the `AGENTS.md` refusal
  enumeration and Workspace bullet were outside the docs task, the detached-head state had no scenario, two load-bearing
  premises had no claims row, the archive count measured the token rather than the record, one cited archive date was
  wrong, row 2's source did not reproduce in the unit's tree, and an unplanned `package-lock.json` edit rode the diff.
- **Rounds 2–6** closed those and the ones they exposed: a five-cell claims row, a skip clause that contradicted the
  capability's failed-read requirement, "the rest predate the rule" falsified by the design's own evidence, the archive
  split re-derived as landing dates against `7e0e564f`, and a scope sentence that denied the defects it stated.
- **Round 7** is clean. The claims table's before-state rows reproduce at `ff21066a`; rows 7–11 stay
  `pending implementation`.

## Rounds 8–15 — proposal revision, closed clean

- **Rounds 8–9** closed the first archive revision (the archive model in Goals/Non-Goal/scenarios, a mangled scenario
  header, the landing-date assumption, the proposal's stale scope summary, the dismissals, the control order, a residual
  scenario collision, and a backwards landing rule).
- **Rounds 10–15** followed the advisor's second and third consultations: the boundary became the fixed landing day
  `2026-11-02` with a name-keyed acknowledgement for a pre-introduction archive at or after it (never a moved constant),
  and the tasks gained the acknowledgement's definition, its control, and the decimal renumbering. Round 15 is clean.

## Round 16 — implementation

- **Supplied artifacts**: the full diff against `a2e87fd6` — `scripts/commit-hygiene.py`,
  `scripts/test-commit-hygiene.py`, `build.gradle.kts`, `AGENTS.md`, `docs/ideas.md`, `docs/review-rounds.tsv` — and the
  change dir, plus `docs/process-defects/verify-workspace-record.md` (outside the screened set).
- **What shipped**: `parse_workspace_record` (three recorded forms, an optional leading backtick, one trailing period, a
  closed four-field set), `workspace_record_paths` (live directories, plus archives dated at or after the boundary,
  minus the acknowledgement), `find_invalid_workspace_records`/`check_workspace_records`, and
  `check_staged_workspace_records` (the same judgment over the index, with the merged-content exemption and the
  tree/branch agreement, the branch comparison skipped only on a detached head); `--workspace-records` in the parser,
  `MODE_FLAGS`, dispatch and the docstring; `verifyWorkspaceRecords` in `check`; `WorkspaceRecordTests` (22 cases).
- **Completed checks**: `python3 scripts/test-commit-hygiene.py` 308 tests OK;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `./gradlew verifyWorkspaceRecords` BUILD
  SUCCESSFUL on the live tree; `openspec validate --all` 27/0 with no warning on this delta; `spotlessCheck`,
  `verifyTableRows` and `verifyCapturedMarkers` clean.
- **Measured controls**: deleting this unit's own record failed `verifyWorkspaceRecords`, naming the file and
  `no Workspace: record`. Five mutations over `scripts/commit-hygiene.py`: the missing-record branch neutered → 1
  failing case; the field validation skipped → 3 malformed-field subcases; the `unit` comparison neutered → 1; the
  boundary made exclusive → 1; the guard's `unit` branch removed → 1; restored, 22 pass.
- **Known limitations**: the writer is shape-checked only; a unit with no change directory keeps its record outside the
  gate; the archive half judges only directories dated at or after `2026-11-02`.
