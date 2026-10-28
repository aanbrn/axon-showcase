# Tasks

Workspace: unit=exempt-merge-commits-from-assignment;
path=/Users/afanasyev/Development/axon-showcase-exempt-merge-commits-from-assignment;
branch=exempt-merge-commits-from-assignment; writer=ses_e804f4f1bffecIPYa7PieI2YY1

## 1. Implementation

- [x] 1.1 Split the deleted read out of `staged_change_paths` into `deleted_change_paths`, and add
      `staged_change_presence`, which maps each staged change path to whether the index holds it. Done: both helpers are
      in place, and `test_a_change_paths_staged_presence_distinguishes_an_addition_from_a_removal` asserts an added and
      a renamed-to path present, a deleted path and a rename's source absent.
- [x] 1.2 Add `merge_revisions` (every parent in `MERGE_HEAD`, empty when there is none, `GitFailure` outside a
      repository or on an unreadable merge state), `tree_paths`, and `merge_base_paths` (the forks' trees, the evidence
      that a merge removed a path). Done: `MergeStateTests` covers no merge, a clean merge, a conflicted merge, a
      two-parent merge naming both revisions, the forks' union across those parents, the merge base's no-common-ancestor
      answer against its error, an unreadable state, and an unresolvable revision.
- [x] 1.3 Apply the exemption in `find_foreign_change_paths`: keep a foreign path only when the merge does not account
      for it — the index holds it while no merged revision does, or the index lacks it while the merged revision still
      holds it or no fork of it did — and leave every foreign path standing when no merge is in progress. Done: the
      merge tests cover an added archive, an archive move, an archive from either of two merged revisions, an intruder
      staged during the merge, a deletion the merge does not make (both of a path it still holds and of one it never
      held), a squash merge, and a `none` assignment.
- [x] 1.4 Cover the behaviour in `scripts/test-commit-hygiene.py`. Done: 192 tests pass; removing the exemption from a
      scratch copy reddens the three exemption cases (the two refusal cases then report a second failure), reading the
      merge state with `rev-parse` instead of the file reddens both two-parent cases, reducing the predicate to bare
      presence reddens the never-held deletion case only, reading only the first merged revision's fork reddens the
      two-parent deletion case, collapsing `merge_base_of`'s error into "no common ancestor" reddens its own case, and
      the squash and plain-commit refusals stay green.
- [x] 1.5 Write the delta at `specs/showcase/quality/commit-hygiene/spec.md` as a `MODIFIED` block carrying the current
      requirement whole, the exemption in its description, and three added scenarios. Done:
      `openspec validate --changes` passes, and the delta carries all 22 existing scenarios plus the three.
- [x] 1.6 Refresh the guard's descriptions the change falsifies — `AGENTS.md`'s pre-commit bullet, the hook's comment,
      and the checker's module docstring — and record the decision for `README.md`. Done: the three sites state the
      exemption; `README.md` is unchanged because its overview lists the guard's slips, and the exemption narrows when
      the assignment check applies rather than adding or removing a slip.
- [x] 1.7 Record the `showcase/quality/commit-hygiene` `## Purpose` decision for the archive commit. Done: no edit owed
      — the Purpose names the guards and the check names, never the ownership check or its scope.
- [x] 1.8 Reproduce the defect on the base commit and the fix here, recording both message sets. Done: on one
      clean-merge tree the base revision's checker prints
      `Path belongs to change 'other', not the commit's assigned change 'demo'` and exits 1, this tree exits 0, and
      staging `openspec/changes/intruder` on the same tree exits 1 naming only the intruder.

## 2. Verification

- [x] 2.1 Run `python3 scripts/test-commit-hygiene.py` and a mutation control. Done: the suite is green, and the scratch
      copy without the exemption reddens the three merge-exemption cases while the squash and plain-commit refusals stay
      green.
- [x] 2.2 Run the gates: `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `spotlessCheck`,
      `openspec validate --all`, and the 120-character recipe over the changed files. Done: `check` green,
      `spotlessCheck` green, and `openspec validate --all` 27 passed / 0 failed with the pre-existing warnings. The
      recipe finds no over-limit line in the changed sources; its hits are the review ledger's one-line rows,
      `AGENTS.md`'s pack table, one section heading, and one quoted message holding an absolute path — none wrappable.

## 3. Review gate

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`.
- [x] 3.2 Run the quick-review loop to a clean round, appending the ledger rows and running both ledger checks. Done:
      four rounds on one session (`ses_e7f4a9dc1ffenbaG9OGVMsqgpF`, created then continued): 6, 6, 1 and 0 findings;
      round 4 closed with no class, and `--expect-clean` passes. Every round's row is appended verbatim with its session
      fields, and `--expect`, `--expect-clean` and `--session-continuity` all pass.
- [x] 3.3 Derive the deep-review routing with `scripts/review-routing.py` and run the pass if it is `REQUIRED`. Done so
      far: the derivation is `RECOMMENDED` on `repeated-class` (two rows repeated a prior class); the owner chose to run
      the pass. Pass 1 (session `ses_e7eeadcecffeiwKzoCY0GAg5Bh`, initial review): 1 finding — task 3.1 unticked though
      its claims-table output is recorded — fixed by ticking it, and the finding is `ACCEPTED`: what is accepted is the
      absence of an independent second pass over that bookkeeping fix, by the owner's explicit decision of 2026-10-28,
      since the correction is legible in the artifact itself; the defect is closed by the tick, the accepted surface is
      only the missing independent confirmation.
- [x] 3.4 Write `docs/process-defects/exempt-merge-commits-from-assignment.md` as defects are observed, or state none
      was. Done: three records — the probe that read this worktree instead of the scratch repository, the startup
      install's pin rewrite that reached round 1 undecided, and the claims-table clause asserted without running its
      command.
- [x] 3.5 Run the readiness check, stage the unit's new files, and request the owner's manual review pass. Done: the
      branch was refreshed to `eefe2543` (ledger rows merged from both sides), `--review-ready` reports that base
      contained in `HEAD` with the index current and no untracked path unaccounted for, and the unit's files are staged.
      The stage set excludes `.opencode/package.json`: the startup install had rewritten its plugin pin (`1.19.4` →
      `1.19.3`), and it was reverted to `HEAD` as installer state rather than a change of this unit.
