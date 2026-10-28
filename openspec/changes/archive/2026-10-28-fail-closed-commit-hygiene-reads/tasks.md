# Tasks

Workspace: unit=fail-closed-commit-hygiene-reads;
path=/Users/afanasyev/Development/axon-showcase-fail-closed-commit-hygiene-reads;
branch=fail-closed-commit-hygiene-reads; writer=ses_e804f4f1bffecIPYa7PieI2YY1

## 1. Implementation

- [x] 1.1 Route every read whose result decides an offender list through `git_output`: `staged_paths`,
      `find_staged_then_edited`, `find_tracked_ignored`, `unstaged_spec_paths`, `find_non_executable_scripts`,
      `tracked_paths`, `claim_file`, the branch read in `resolve_assignment`, `rename_sources`, and the deleted-path
      read in `staged_change_paths`. Verify: `grep -n "git(repo" scripts/commit-hygiene.py` shows only the helper
      itself, the three exit-1 answer branches, and the branch read.
- [x] 1.2 Keep the answer-by-exit branches explicit: `find_conflict_markers` treats `git grep`'s exit 1 as no match and
      raises on a higher code, and `current_branch` answers `symbolic-ref`'s exit 1 (a detached HEAD) while raising on
      every other non-zero exit. Verify: a clean repository still passes `--conflict-markers`, a fatal grep read raises,
      and an unreadable HEAD fails the check.
- [x] 1.3 Catch `CheckFailure` once in `main` — around `dispatch` — print `The check could not run: <failure>` and
      return 1, so every mode fails closed with the same shape. Verify: each mode's failing case exits 1 with that line.
- [x] 1.4 Cover the rule in `scripts/test-commit-hygiene.py`: a failing read for `--staged`, `--tracked-ignored`,
      `--executable-bits`, `--large-files`, and the assignment read; a no-match grep still passing; and the helpers'
      answer-vs-failure split. Verify: `python3 scripts/test-commit-hygiene.py` passes, and restoring one unguarded read
      turns its case red.
- [x] 1.5 Write the delta at `specs/showcase/quality/commit-hygiene/spec.md` and validate it. Verify:
      `openspec validate --changes` passes with no warning.
- [x] 1.6 Decide the docs refresh and record the outcome: whether `AGENTS.md`, `README.md`, or an ADR states how the
      checker reports a failed read. Verify: the decision names each file and its reason.

- [x] 1.7 Record the `showcase/quality/commit-hygiene` `## Purpose` decision for the archive commit: whether the general
      rule widens the clause the Purpose states for the readiness mode, and apply the edit there if it does. Done: no
      edit owed — the Purpose already names the size limit and the fail-closed boundary, and this change adds a
      reporting property to the guards it describes rather than contradicting its text.

- [x] 1.8 Close `--large-files`' file-measurement gap: a tracked path absent from the worktree is measured from its
      index blob, any other failure to measure raises `FileFailure`, and `CheckFailure` gives the one catch both failure
      kinds share. Verify: an absent file over the limit is reported, a self-referential symlink fails the mode, and
      restoring the skip reddens both cases.

## 2. Verification

- [x] 2.1 Run `python3 scripts/test-commit-hygiene.py` and a mutation control: un-guard one read in a scratch copy and
      confirm its case reddens.
- [x] 2.2 Run the gates: `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `spotlessCheck`, and the
      120-character recipe over the changed files.
- [x] 2.3 Reproduce the defect on the base commit and its fix here: a corrupt index makes `--staged` and
      `--tracked-ignored` exit 1 naming the command, where the base commit reports a clean set.

## 3. Review gate

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`.
- [x] 3.2 Run the quick-review loop to a clean round, appending the ledger rows and running both ledger checks.
- [x] 3.3 Derive the deep-review routing with `scripts/review-routing.py` and run the pass if it is `REQUIRED`. Done:
      `REQUIRED`, signal `requirement-block-added`. Pass 1: one nitpick (the claims table's attribution and count) —
      FIXED_AND_VERIFIED. Pass 2 verified the corrected row and that no other artifact moved; its one residue, an
      off-by-one in the row's own exclusion count, is fixed and checked by rerunning the derivation (18 lines, 5
      excluded, 13 reads). Pass 3, a third full pass spent by a recorded decision, verified the branch-read narrowing
      the owner's third consultation ordered: nothing remained — the exit-0/1/≥2 split probed against git, the
      emitted-name and unreadable-HEAD controls, and the delta's three HEAD scenarios. Pass 4, a fourth full pass spent
      by a recorded decision, verified the `stat()` closure the owner ordered after the quick loop's round 6: one
      should-fix (task 1.8 sat under the review-gate group while numbered for implementation, which `openspec validate`
      warned on) — FIXED_AND_VERIFIED; the file-measurement split, its three controls, and the swept wording all
      checked.
- [x] 3.4 Write `docs/process-defects/fail-closed-commit-hygiene-reads.md` as defects are observed, or state none was.
- [x] 3.5 Run the readiness check, stage the unit's new files, and request the owner's manual review pass. Done:
      `--review-ready` passes at the boundary; the request names the ready line it printed.

## 4. Rework after the third consultation

- [x] 4.1 Narrow the branch read: `current_branch` answers only `symbolic-ref`'s exit 1 (a detached HEAD) and raises on
      every other non-zero exit, so an unreadable HEAD fails the check rather than reading as "no branch". Verify: an
      unborn HEAD names its branch, a detached one answers empty, an unreadable HEAD raises, and the existing assignment
      message speaks of a detached HEAD.
- [x] 4.2 Correct the accepted-limitation claim about file reads: name `find_oversized_files`'s skipped `stat()` as an
      unfixed gap rather than asserting the direct reads already report a missing file.
- [x] 4.3 Restate the delta's answer list and its HEAD scenarios (detached answers, unborn names its branch, an
      unreadable HEAD fails), and keep the requirement's description under the validate warning's length. Verify:
      `openspec validate --changes` passes with no warning.
- [x] 4.4 Record both defects in `docs/process-defects/fail-closed-commit-hygiene-reads.md` (records `#3`, `#4`).
- [x] 4.5 Re-run the gates: `python3 scripts/test-commit-hygiene.py` (170 tests), the read mutation battery including
      the branch read, `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `spotlessCheck`, and the 120-character
      recipe.
