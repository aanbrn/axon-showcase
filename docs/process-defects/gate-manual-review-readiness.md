# Process defects — gate-manual-review-readiness

### gate-manual-review-readiness#1 — Pre-change claims were anchored to the working tree

- **Recorded** — 2026-10-28, by the writing session (`ses_e804f4f1bffecIPYa7PieI2YY1`), during the review stage.
- **Stage** — review (the claims table, written during the pre-write derivation pass).
- **Expected** — a claims-table row about the state **before** the change anchors its source to the unit's base
  revision, as the review gate's pre-write step requires, so it keeps reproducing after the change rewrites that state.
- **Observed** — three rows asked the working tree whether the readiness mode existed, whether anything read a merge
  base, and which scripts defaulted `--base`; all three described the pre-change state, and the change itself falsified
  each one the moment it was written.
- **Evidence** — the round-1 `review-quick` report, finding 1 (`claim-unreproducible`): it re-ran the rows' commands and
  found `review-ready` present in `.opencode/skills/review-gate/SKILL.md` and `scripts/commit-hygiene.py`, `merge-base`
  present in `scripts/commit-hygiene.py`, and a third `--base` argument. The report's correction (`git grep … bf0dbd61`)
  reproduced: two commands exit 1 and the third returns exactly `claim-scan.py:153` and `review-routing.py:198`.
- **Consequence** — one review round spent on the table rather than on the change; the rows and the Context sentence
  that shared the defect were re-anchored to the base in the same round. Nothing shipped wrong.
- **Knowledge boundary** — observed for this unit's claims table. Not checked whether other units' tables carry rows
  anchored to `HEAD` or the working tree; the rule is stated, and this record cannot show how often it is missed.

- **Decision** — no-change: review-gate §1 already requires a pre-change claim's source to be anchored to the unit's
  base revision.

### gate-manual-review-readiness#2 — A test fixture inherited the branch its assertion named

- **Recorded** — 2026-10-28, by the writing session (`ses_e804f4f1bffecIPYa7PieI2YY1`), during the review stage.
- **Stage** — review (the implementation's tests).
- **Expected** — a fixture creates every ref its assertion names, so the case exercises the guard it claims rather than
  a neighbouring branch, per the check-authoring rule that a fixture must reach the state the assertion claims.
- **Observed** — `test_a_base_without_a_merge_base_fails` asked the checker for `base="main"` without creating `main`,
  so on a host whose `init.defaultBranch` is `master` the case took the unresolvable-base branch and failed its own
  `assertIn("share no merge base", …)`.
- **Evidence** — the round-2 `review-quick` report, finding 1 (`test-control`), reproduced here: running the suite with
  `init.defaultBranch=master` set for that invocation (`GIT_CONFIG_COUNT=1`, `GIT_CONFIG_KEY_0=init.defaultBranch`,
  `GIT_CONFIG_VALUE_0=master`) failed the case before the fix and passes after it; the repository pins
  `init.defaultBranch` nowhere.
- **Consequence** — the case would have been green on this host and red on a differently configured one, inside `check`.
  The fixture now runs `git checkout -B main` before the orphan branch, and the suite passes under both configurations.
- **Knowledge boundary** — observed for this case. The sibling readiness fixtures obtain `main` from a shared helper
  that creates it; not checked whether other suites inherit refs the same way.

- **Decision** — no-change: check-and-test-authoring's fixture mode already requires the fixture to reach the state the
  assertion claims, every ref it names included.

### gate-manual-review-readiness#3 — The base criterion was derived from a symptom sentence, not from the rule it serves

- **Recorded** — 2026-10-28, by the writing session (`ses_e804f4f1bffecIPYa7PieI2YY1`), during the review stage.
- **Stage** — review (the implementation's base check).
- **Expected** — a check for "the base has not moved" tests the condition the branch-refresh rule names — the unit sits
  on the base — rather than re-deriving readability from the "deletes nothing the unit did not author" sentence in the
  manual-pass step.
- **Observed** — the mode compared the working tree's deletions against the base with its deletions against the merge
  base, treating a line deleted against the base but not against the merge base as the base's advance. A base that
  _removed_ a line the tree still carries produces the opposite diff — an addition — so the mode reported the tree
  ready.
- **Evidence** — the second consultation's counterexample; reproduced in a scratch repository: with the base's advance
  dropping the line `B` the worktree still carries, `git merge-base --is-ancestor main HEAD` exited 1 while
  `git diff --unified=0 --no-renames main --` emitted only a `+B` line and no deletion.
- **Consequence** — the criterion, its diff parser, its rename handling, and six tests were replaced by a `merge-base`
  containment test in the same unit; the change had already passed a clean quick loop and two deep-review passes before
  the state was found, so those rounds verified a check with a hole in it.
- **Knowledge boundary** — observed for this unit's criterion. Not checked whether other checks in the repository derive
  their criterion from a symptom sentence rather than from the condition the owning rule names.

- **Decision** — proposed: applied as the new mode "The predicate is the rule's own condition, not a sentence beside it"
  in [`check-and-test-authoring`](../../.opencode/skills/check-and-test-authoring/SKILL.md).

### gate-manual-review-readiness#4 — A failing git read was mapped onto the benign outcome

- **Recorded** — 2026-10-28, by the writing session (`ses_e804f4f1bffecIPYa7PieI2YY1`), during the review stage.
- **Stage** — review (the implementation's error paths).
- **Expected** — an invocation whose failure is indistinguishable from an empty result is reported as an unestablished
  measurement, never read as "no violations", as `AGENTS.md`'s derivation rule requires.
- **Observed** — the untracked-path read and the deletion diff parsed stdout without checking the command's exit status,
  so a git failure yielding empty stdout produced no offenders and the mode could print its ready line.
- **Evidence** — the second consultation's finding; reproduced here with an unreadable index:
  `git rev-parse --verify --quiet main^{commit}` exited 0 while `git status --porcelain` exited 128, which the pre-fix
  code would have read as an empty record list.
- **Consequence** — the mode now reports `Readiness is not established` with the failing command and exits non-zero, and
  a test corrupts the index and asserts that line. Nothing shipped with the defect.
- **Knowledge boundary** — observed for this mode's reads. The sibling modes in the same script (`--staged`,
  `--tracked-ignored`, `--unique-crons`, …) were not surveyed for the same pattern; only this unit's path was fixed.

- **Decision** — no-change: the rule that a derivation's failure must not map onto the benign outcome already requires
  an unreadable input to be reported as unestablished, never as "no violations".
