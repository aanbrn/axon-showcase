# Process defects — state-the-adr-decision-boundary

### state-the-adr-decision-boundary#1 — Edits after the staging step left the index carrying the rejected wording

**Recorded** — 2026-10-30, by the unit's writing session.

**Stage** — review.

**Expected** — the review gate stages the unit's files before the owner's manual pass, and a path staged and then edited
is re-staged, so the index is the tree the review and the commit see (`AGENTS.md`'s `git add <dir>` gotcha; the gate's
§5 step).

**Observed** — the round-4 rewrite of the criterion was applied to the working tree after the previous `git add`, and
the index kept round 3's rejected wording; `git status` reported `MM` on `AGENTS.md`, `docs/adr/README.md`, and
`docs/review-rounds.tsv`.

**Evidence** — round 4's report quoted `MM` for those three paths and `git diff --cached` still carrying "a settled
proposal that rules nothing about the system's behaviour or shape"; the fix was `git add -A -- docs AGENTS.md` followed
by `git status` showing four `M ` lines and no `MM`.

**Consequence** — one review round spent on a staging defect rather than content; the index caught up before the manual
pass, and the content itself needed no change.

**Knowledge boundary** — observed: the stale index, the round-4 report, and the re-stage. Not checked: whether an
earlier round in this unit also reviewed a stale index, which the round reports' file lists would show.

- **Decision** — no-change: the `git add <dir>` staging-snapshot rule and the review gate's §5 re-stage remedy already
  require a path edited after staging to be re-staged.
