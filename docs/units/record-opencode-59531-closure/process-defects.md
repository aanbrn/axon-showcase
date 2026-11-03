# record-opencode-59531-closure

### record-opencode-59531-closure#1 — A gate was ticked green before the tree's last formatting pass

Recorded: 2026-10-28 by the unit's session.

Stage: implementation.

Expected: re-run `spotlessApply` and `spotlessCheck` after the last write to a formatted file and only then record the
gate's state, since the tick that claims a gate green is the edit that falsifies it.

Observed: task 3.2 was ticked with a `Done` note stating the pair had succeeded after the change dir's last edit, while
`design.md` and `tasks.md` were edited after that run.

Evidence: review round 1, finding 1 — "`./gradlew spotlessCheck` fails at `:spotlessMarkdownCheck` on
`openspec/changes/record-opencode-59531-closure/design.md` (dismissals-table padding) and `tasks.md`"; the tick and its
note are at `tasks.md` task 3.2.

Consequence: the recorded gate state was false until the pair was re-run; repaired in the same round, with the same
re-run owed again after the unit's last edit. Nothing else was broken.

Knowledge boundary: observed from the round's run and this unit's own later run; no claim about whether other units
carry the same stale tick.

- **Decision** — no-change: the formatting rule already requires `spotlessApply` and `spotlessCheck` after the final
  write and forbids the tick that claims a gate green as the last act.

### record-opencode-59531-closure#2 — The same gate-state claim recurred after its repair

Recorded: 2026-10-28 by the unit's session.

Stage: review.

Expected: after #1, a gate-state note is written only once the sequence that makes it true has finished, so no later
edit or ledger row overtakes it.

Observed: two later notes were each overtaken. Task 3.8 recorded the staging and readiness state before `design.md` was
edited again, and task 3.7 recorded the routing outcome `NOT_NEEDED` before two further rounds made the derivation print
`RECOMMENDED` on its repeated-class signal.

Evidence: review round 6, finding on task 3.8 — `--review-ready` reported "A path is staged and then edited …:
`openspec/changes/record-opencode-59531-closure/design.md`"; review round 7, finding on task 3.7 —
`python3 scripts/review-routing.py --change record-opencode-59531-closure` printed `ROUTING: RECOMMENDED` with
`[repeated-class] 2 rows repeat a prior class`; both rounds' ledger rows carry `claim-unreproducible` in the repeated
column.

Consequence: three further review rounds. Repaired by folding tasks 3.2 and 3.6–3.8 into one final-pass task whose note
precedes the final formatting and readiness runs, with the task text stating why the note cannot claim those two.

Knowledge boundary: recorded from the rounds' reports and the ledger; no cause is assigned here.

- **Decision** — no-change: review-gate §1 already requires a self-record — a Done note or a claims row — to state a
  relative result and the verification as of its own step, never a status its remaining steps move.
