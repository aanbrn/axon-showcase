# Review hand-over

## Round 1 — proposal

- **Unit**: `verify-unique-change-names`; **base** `f0486b6f`; **state**: uncommitted working tree, staged, no commit;
  **tree**: `/Users/afanasyev/Development/axon-showcase-verify-unique-change-names`.
- **Supplied artifacts**: the change dir — `proposal.md`, `design.md` (Context, Goals/Non-Goals, four decisions with
  rejected alternatives, Risks, a ten-row claims table with per-kind dismissals), `tasks.md` (with the `Workspace:`
  record), and the delta spec on `showcase/quality/commit-hygiene` — plus the `docs/ideas.md` removal of this unit's
  idea.
- **Stage**: proposal; no implementation yet.
- **Packs**: `gradle-build`; the `check-and-test-authoring` skill.
- **Already verified**: `openspec validate --all` 27 passed, 0 failed, with no warning on this change's delta;
  `spotlessCheck` clean; `python3 scripts/commit-hygiene.py --ideas-sections` exit 0 after the idea removal;
  `--review-ready` reports the base contained and the index current.
- **The premise the design rests on**: the archive carries exactly one duplicated change name
  (`fix-openspec-config-rules`, archived `2026-09-14` by #218 and `2026-10-18` by #624), so a verification without an
  exemption would fail `check` on the tree that introduces it.
- **Risk focuses**: (1) whether the acknowledged-pair exemption is the right route for the one historical collision, and
  whether its narrow scope is stated so it cannot grow; (2) whether "the change name is the directory name minus one
  `YYYY-MM-DD-` segment, read through `ARCHIVED_CHANGE_RE`" is faithful to how the guard already resolves a name, and
  whether reusing the regex is single-sourcing rather than coupling; (3) whether the delta's scenarios each name a state
  a control can reach; (4) whether the claims table's rows and dismissals are derived rather than asserted.

## Round 5 — proposal, closed clean

- **Result**: no findings; no class repeats a prior round's. Rounds 2–4 raised 11 findings across 8 classes (two
  contradictions, two missing tasks, a test-control, a single-sourcing, a structured-writing, a wording, and two
  claims-table completeness gaps), all applied.
- **Preconditions recorded**: `openspec validate --all` 27 passed, 0 failed with no warning on this delta;
  `spotlessCheck` clean; both ledger checks and `--expect-clean` pass; `--review-ready` reports the base contained and
  the index current.
- **Process defect**: round 1 produced no screen because the invocation's prompt omitted the subject pack the hand-over
  names; recorded as `docs/process-defects/verify-unique-change-names.md#1`, and round 2 continued the same session.

## Round 6 — implementation

- **Unit**: `verify-unique-change-names`; **base** `f0486b6f`; **state**: uncommitted working tree, staged, no commit;
  **tree**: `/Users/afanasyev/Development/axon-showcase-verify-unique-change-names`.
- **Supplied artifacts**: the full diff against the base — `scripts/commit-hygiene.py`,
  `scripts/test-commit-hygiene.py`, `build.gradle.kts`, `docs/ideas.md`, `docs/review-rounds.tsv` — and the change dir
  (proposal, design with its claims table and dismissals, tasks, delta spec, this hand-over), plus
  `docs/process-defects/verify-unique-change-names.md` (outside the screened set).
- **Stage**: implementation. Rounds 1–5 reviewed the proposal; round 5 closed clean.
- **Completed checks**: `python3 scripts/test-commit-hygiene.py` 286 tests OK;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL with `verifyUniqueChangeNames` in the
  graph; `./gradlew verifyUniqueChangeNames` BUILD SUCCESSFUL on the live archive; `openspec validate --all` 27/0 with
  no warning on this delta; `spotlessCheck` clean; the manual 120-character recipe clean over the three changed script
  files.
- **Measured controls**: planting `openspec/changes/archive/2026-11-03-gate-manual-review-readiness` beside the real
  `2026-10-28-gate-manual-review-readiness` failed the task, naming
  `'gate-manual-review-readiness' (2026-10-28-…, 2026-11-03-…)`. Three mutations over `scripts/commit-hygiene.py`:
  `find_duplicate_change_names` returning no duplicate fails 5 of the class's 10 cases; emptying
  `ACKNOWLEDGED_DUPLICATE_CHANGE_NAMES` fails 3; widening the exemption from the exact pair to the name fails exactly
  `test_a_third_directory_carrying_the_acknowledged_name_fails`. Restored, 10 pass.
- **Design change made during implementation**: decision 3 gained the retirement clause — a control reads the live
  archive, so the acknowledgement constant cannot outlive the collision it records.
- **Known limitations**: the gate sees a name only once the change is archived; a live-against-archived collision
  surfaces in the archive commit's own pull request, before that pull request merges.
- **Packs**: `gradle-build`; the `check-and-test-authoring` skill.
- **Risk focuses**: (1) whether the predicate reads exactly what the requirement states (one date segment, prefix-less
  entries unjudged, exact-pair exemption, every directory named); (2) whether routing `change_names`'s archived half
  through `archived_change_names` left the pre-commit guard's behaviour unchanged, its missing-archive tolerance
  included; (3) whether the controls reach the states they claim and none is vacuous; (4) whether the claims table's
  filled rows reproduce from their commands.

## Round 7 — re-review after a base refresh

`origin/main` advanced to `f0486b6f` (`bound-self-learning-loop-consolidation`, #816), which touched
`docs/decisions/2026-11-02-self-learning-loop-boundary.md`,
`docs/process-defects/bound-self-learning-loop-consolidation.md`, `docs/process-reuse.md`, and `docs/review-rounds.tsv`.
The tree was stashed, fast-forwarded, and popped; the one conflict (`docs/review-rounds.tsv`) was resolved keeping both
sides — upstream's three `bound-self-learning-loop-consolidation` rows, then this unit's five. Both units' rounds are
contiguous and their session fields valid.

The claims table is re-anchored to `f0486b6f`; its before-state rows reproduce there — #816 changed no script, no
archive directory (the archive still holds 445, with `fix-openspec-config-rules` the only duplicated name), and no
gate-wiring file.

## Rounds 8–10 — implementation, closed clean

- **Round 8**: clean after round 7's fixes (the emptied-table mutation re-measured to 4, and the docs-refresh pattern
  widened to `verifyUniqueChangeNames`).
- **Deep review** (`REQUIRED`, `requirement-block-added`): pass 1 raised one `should-fix` — the prefix-less fixture
  proved only that two such entries do not collide, so the scenario's "joins no name group" clause was unverified —
  fixed by pairing the prefix-less `demo` with `2026-01-01-demo`. Pass 2 reproduced the regression (a fallback to the
  entry's own name fails exactly that case), caught a stale "three mutations" count in claims row 8, and confirmed the
  fix.
- **Round 9**: one `claim-missing` — the implementation consultation's new Risks bullet (the archive-tree input beside
  the name-only read) had no claims row and the `[absolute]` dismissal still counted three. Fixed by adding row 11 and
  restating the dismissal.
- **Round 10**: clean.
- **Independent consultation** (technical advisor, implementation stage): `Agreement: yes`, accepting archive-time
  detection, the preserved historical collision, prefix-less exclusions, suppression maintenance, and presently
  unmeasured incremental overhead; its one open question — the build input's fingerprint cost — is recorded as the third
  Risk with the cache-correctness reason for the tree input.
