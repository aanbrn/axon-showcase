# Process defects — clarify-projection-failure-scope

### clarify-projection-failure-scope#1 — A hand-over stated a command's output that did not reproduce

- **Recorded** — 2026-10-27, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the review stage.
- **Stage** — review (the round 1 hand-over).
- **Expected** — a statement in the reviewer hand-over about what a command prints is that command's output at the state
  it describes; `AGENTS.md`'s "A verdict echo is not a check" names the class.
- **Observed** — the hand-over told the reviewer "`git diff --stat origin/main` shows only the two files above" (the ADR
  and the process-defect record). The command listed only the tracked ADR: the record was untracked, so it was outside
  the diff.
- **Evidence** — round 1's report states in its Limitations that the hand-over's `git diff --stat origin/main` line does
  not reproduce as written: the command listed only the ADR (18 insertions), and the untracked record appeared only
  under `git status --porcelain -uall`. The current tracked diff also carries the unit's ledger rows, which did not
  exist at the hand-over.
- **Consequence** — the reviewer spent part of its bounded pass checking the hand-over's input claim rather than the
  artifact.
- **Knowledge boundary** — observed for this unit's round 1. Not checked whether other units' hand-overs carry
  statements about command output that were not run, so this is not evidence of a recurrence.

- **Decision** — no-change: the verdict-echo rule and the evidence rule already require a statement about a command's
  output to be that command's output at the state it describes.

### clarify-projection-failure-scope#2 — The unit's first edit ran without the required `Packs:` line

- **Recorded** — 2026-10-27, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the review stage.
- **Stage** — implementation.
- **Expected** — `AGENTS.md` requires one `Packs:` line — every pack loaded in full, or `none` with its reason — before
  the first edit of a task.
- **Observed** — the ADR edit and the process-defect file were written without that line. The classification existed
  only in the reviewer hand-over, which named `jvm-development` for the artifact's runtime claims.
- **Evidence** — the event sequence: branch creation, the pre-write record, both file writes, then the reviewer
  hand-over's `Packs` line; no `Packs:` line precedes the first write.
- **Consequence** — no artifact is affected, and the unit's classification is recoverable from the hand-over; the rule's
  record-keeping purpose was not met for the unit's own turn.
- **Knowledge boundary** — observed for this unit. Not checked whether other docs units omitted the line, so this is not
  evidence of a pattern.

- **Decision** — no-change: `AGENTS.md` already requires one `Packs:` line before the first edit of a task.
