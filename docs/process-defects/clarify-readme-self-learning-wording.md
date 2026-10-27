# clarify-readme-self-learning-wording

### clarify-readme-self-learning-wording#1 — The unit's record was written only after a review required it

- **Recorded**: 2026-10-27, by the writing session (`ses_e84c9b955ffeY6V3z6ZN1VELva`).
- **Stage**: review.
- **Expected**: the unit's process-defect record exists — stating what the unit observed, or that it observed none —
  before the quick review's closing round and the manual-pass request, as `AGENTS.md`'s "Workflow: Record process
  defects" requires.
- **Observed**: the unit changed the README and ran its gates without creating `docs/process-defects/<unit>.md`; the
  record appeared only after the first quick-review round reported its absence.
- **Evidence**: `docs/review-rounds.tsv`'s round 1 for this unit carries `missing-task`, and that round's report named
  the absent file as the finding.
- **Consequence**: one review round raised it; the file was created in the same turn, and what it must say was itself
  corrected in round 2, when the first version stated no defect had been observed. Nothing remains broken.
- **Knowledge boundary**: observed this unit's round 1 and its class. Did not check whether any earlier unit wrote its
  record only after a review found it missing, so this is not evidence of a recurrence.
- **Decision** — no-change: `review-gate` §5 has carried the process-defect-record precondition since #709; a compliance
  miss, so no rule change is owed.

### clarify-readme-self-learning-wording#2 — A branch command ran against the wrong branch in the settlement step

- **Recorded**: 2026-10-27, by the writing session (`ses_e84c9b955ffeY6V3z6ZN1VELva`).
- **Stage**: ship.
- **Expected**: the settlement after the pull request merged — confirming the unit completed, removing the claim the
  commit guard reads, and returning the primary to a clean `main` that still tracks `origin/main` — leaves the tree's
  branch state as the procedure defines it, and any command the settlement issues acts on the branch it names.
- **Observed**: the unit branch had already been deleted, and `git branch --unset-upstream` — issued with no branch
  name, after `git switch main` — unset `main`'s upstream instead of a branch that no longer existed; the next
  `git status -sb` showed `## main` with no tracking reference.
- **Evidence**: the command exited 0, and the status line read `## main`;
  `git branch --set-upstream-to=origin/main main` restored it, after which the line reads `## main...origin/main` and
  `git rev-parse HEAD origin/main` returns the same commit for both.
- **Consequence**: `main` carried no upstream for the interval between the two commands; no work was lost and nothing
  else changed. Repaired in the same turn.
- **Knowledge boundary**: observed the unset and its restoration. Did not check whether any other branch-scoped git
  command issued during a settlement names its branch. No documented settle step performs an upstream operation, so the
  command itself had no step to follow.
- **Decision** — proposed: applied as the explicit `<branch>` operand in the "Fork every new branch" remedy
  (`AGENTS.md`); the PR link replaces this one before merge.
