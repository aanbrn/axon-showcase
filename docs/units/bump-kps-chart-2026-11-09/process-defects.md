# Process defects

### bump-kps-chart-2026-11-09#1 — an inline code span authored across a source line break

- `Recorded` — 2026-11-09, the unit's own session.
- `Stage` — review (the retargeted plan's proposal round, round 13).
- `Expected` — a command written as an inline code span stays on one source line, so the formatter's reflow moves the
  whole span and its content keeps its single spaces.
- `Observed` — the task-1.1 rewrite split `` `helm search repo prometheus-community/kube-prometheus-stack --versions` ``
  across a source line break; Prettier joined the lines and kept the continuation line's indentation, so the span came
  out as `helm search repo     prometheus-community/…`, a whitespace run neither `spotlessCheck` nor the 120-character
  check can see.
- `Evidence` — the review's finding: the inline code span at `tasks.md:11` reads with five spaces between `repo` and
  `prometheus-community`; the correction was to put the span on its own source line.
- `Consequence` — one extra review round. Repaired in the same round; no other artifact carried the run.
- `Knowledge boundary` — observed: the round's report and the file's text. Assumed: the run came from the split, not
  from another tool. Not checked: whether an earlier edit in this unit produced the same shape.

### bump-kps-chart-2026-11-09#2 — a reviewer invocation delegated without its hand-over and packs

- `Recorded` — 2026-11-09, the unit's own session.
- `Stage` — review.
- `Expected` — before a `review-quick` delegation, write the invocation's hand-over section at
  `docs/units/<unit>/review-handover.md` and name the applicable subject packs in the prompt.
- `Observed` — the first screen of the upstream comment draft was delegated with an inline prompt that named neither a
  hand-over section nor a pack; the reviewer stopped with a missing-input report and screened nothing.
- `Evidence` — the report's own line ("Missing input — the screen stopped before judging the artifact") and its note
  that the hand-over "names no pack at all".
- `Consequence` — one wasted invocation; no artifact was judged. Repaired: the hand-over section `upstream-8953-comment`
  was written and the screen re-dispatched on the same session with `deployment-operations` and `gradle-build`.
- `Knowledge boundary` — observed: the report and the prompt. Assumed: the reviewer would have screened had the packs
  been named. Not checked: whether the first invocation's session could have been continued — it was used for the
  re-dispatch.

### bump-kps-chart-2026-11-09#3 — the session register's event sequence skipped a number

- `Recorded` — 2026-11-09, the unit's own session.
- `Stage` — review.
- `Expected` — each agent-session row's event field is the unit's next number, contiguous from 1.
- `Observed` — the round-17 row was written with event `20` while round 16 held `18`, leaving `19` unused; rounds 18 and
  19 then carried `21` and `22`.
- `Evidence` — the register check's own lines: "the event '20' is not the unit's next, 19" (and the two following rows).
- `Consequence` — `--agent-sessions` failed until the three rows were renumbered before the first commit; the file was
  still uncommitted, so the append-only rule was not broken and no history was rewritten.
- `Knowledge boundary` — observed: the file's rows and the checker's output. Assumed: the slip came from counting the
  upstream-comment screens as rows, which they are not. Not checked: whether an earlier unit hit the same shape.
