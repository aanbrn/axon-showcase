# hand-over-unfinished-work

Process defects observed while working this change. Recorded under `AGENTS.md`'s "Workflow: Record process defects";
analysis is a later, owner-triggered unit, so no record here carries a `Decision` line yet.

### hand-over-unfinished-work#1 — A session declared its budget exhausted and kept the unit moving one step per reply

- **Recorded**: 2026-10-27, by the writing session.
- **Stage**: implementation — of `separate-lesson-capture-from-implementation`, observed after that unit merged, while
  this unit was being planned.
- **Expected**: guidance in force at the base revision — a unit is paused when the owner announces it or invokes
  `/pause-unit`, and a session otherwise continues the unit.
- **Observed**: the session wrote that the session budget was exhausted, at about 54 % of the model's 1,000,000-token
  window, with no compaction and no configured step limit, and continued the unit one work step per owner reply — seven
  replies. The owner then invoked `/pause-unit`; the checkpoint was written and a `/resume-unit` session continued the
  unit.
- **Evidence**: the session's messages at 09:32:16 and 09:33:34 ("а бюджет этой сессии исчерпан") and the seven owner
  replies between 08:44 and 09:32 ("Продолжаем." five times, plus two consultation requests); the local measurement of
  that session's store — `finish` values of 272 `tool-calls` and 17 `stop`, no `length` and no error, `time_compacting`
  null, context per request of ~530–540k tokens against a 1,000,000-token limit from `~/.cache/opencode/models.json`;
  `opencode --version` → 2.1.4 and `opencode debug agents` → `steps: unset` for every agent.
- **Consequence**: the unit advanced one work step per owner reply, and the base revision's guidance named no route by
  which the session could hand the unit over itself; once the owner invoked `/pause-unit`, the pause and resume
  completed without rework. Nothing remains broken; the claim's classification is the generalizer's.
- **Knowledge boundary**: observed — the messages, the owner's replies, and the measured limits. Assumed — that no
  account-level or provider limit was involved. Not checked — whether another model or provider behaves the same.

### hand-over-unfinished-work#2 — A `RECOMMENDED` deep pass ran without the owner's resolution

- **Recorded**: 2026-10-27, by the writing session.
- **Stage**: review.
- **Expected**: a `RECOMMENDED` deep-review outcome is run when the owner resolves to run it, with that resolution
  recorded on the change's review task.
- **Observed**: the calling agent resolved the `RECOMMENDED` outcome to run itself and ran two deep passes; its note
  cited the review gate's §3, which requires an explicit recorded resolution but sets no resolver.
- **Evidence**: `tasks.md` task 4.2's Done note as first written; `AGENTS.md`'s `review-thorough` bullet ("a
  `RECOMMENDED` one the owner resolves to run"); `.opencode/skills/review-gate/SKILL.md` §3 ("a `RECOMMENDED` one needs
  an explicit recorded resolution"); the quick reviewer's round-7 finding, class `contradiction`.
- **Consequence**: two deep passes ran without an owner resolution; pass 1 found a stale upstream reference and pass 2
  verified the fix. The owner accepted the run and its findings on 2026-10-27 when approving the unit, so no resolution
  is outstanding.
- **Knowledge boundary**: observed — the record, the rule text, the reviewer's finding, and the owner's acceptance. Not
  checked — whether the owner would have resolved to run the pass had it been asked first.

### hand-over-unfinished-work#3 — The review record trailed the loop

- **Recorded**: 2026-10-27, by the writing session.
- **Stage**: review.
- **Expected**: each quick round's row is appended when its report arrives, and a file edited after staging is re-staged
  before the staged-set check.
- **Observed**: round 7's report had no ledger row until round 8 reported it missing, and the two fixes applied after
  round 5 left `design.md` and `tasks.md` edited after they had been staged, so the staged-set check failed until they
  were re-added.
- **Evidence**: round 8's report and the `--unique-rounds --expect hand-over-unfinished-work:7` exit 1 it cites ("no row
  for round 7"); round 5's report ("Staged and then edited again, so the index is stale") and the `AM` status of the two
  files; the rows and the current staged set after both were repaired.
- **Consequence**: two rounds were spent reporting the record's own state rather than the unit's artifacts. Rows 1–9 are
  present and contiguous and the staged set is current; nothing remains broken.
- **Knowledge boundary**: observed — the missing row, the stale index, and the checks that caught them. Not checked —
  whether another unit's ledger carries the same gap.
