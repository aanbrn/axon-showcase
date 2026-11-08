# Process defects — generalize-process-defects-2026-11-08

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### generalize-process-defects-2026-11-08#1 — A stateless reviewer was invoked with a placeholder prompt

- **Recorded** — 2026-11-08, the agent, when the invocation returned.
- **Stage** — proposal review.
- **Expected** — the consultation the owner authorized goes to the `technical-advisor` with the focused hand-off its
  command declares.
- **Observed** — the delegation was issued to `review-thorough` with a placeholder prompt instead, so a reviewer session
  was created that had no change to review.
- **Evidence** — the subagent's reply: it received no review task, read no files, and ran no command, asking instead for
  a hand-over.
- **Consequence** — one subagent session was spent and no review ran; the intended consultation was then issued to the
  right agent, and no artifact was edited by the stray session.
- **Knowledge boundary** — observed from the stray reply and the next invocation. Did not check whether the stray
  session consumed metered quota beyond its single reply.

### generalize-process-defects-2026-11-08#2 — A failed edit script's exit status was not read, and the commit proceeded

- **Recorded** — 2026-11-08, the agent, while delivering the unit.
- **Stage** — ship.
- **Expected** — the script that adds the pull request's link to the two `proposed` `Decision` lines lands both edits,
  and the commit is gated on a content check rather than on the edit command's exit status, as `AGENTS.md`'s
  edit-verification gotcha requires.
- **Observed** — the script applied the first replacement and raised `AssertionError` on the second, because that line
  had been reflowed by the formatter so the anchor no longer matched; the surrounding shell chain went on to
  `git add -A docs/units && git commit && git push`, and the commit carried only one of the two links.
- **Evidence** — the script's traceback naming the `link-generalization-decision-records` anchor; the
  `grep -c "pull/866"` run in the same command printing `0` for that file and `1` for the other; commit `4e8743b5` on PR
  #866; the follow-up commit `3d322f51` adding the missing link before the merge.
- **Consequence** — the first push carried one unlinked `proposed` line for a few minutes: the same symptom
  `generalize-process-defects-2026-11-07#3` records, with a different cause — there the lines merged unlinked, here an
  unread edit failure landed against the tracked link step. The step's own `grep` caught it, and the merge carries both
  links. No other artifact was affected.
- **Knowledge boundary** — observed the traceback, the two `grep` counts, and the two commits. Did not check whether any
  earlier script in this unit failed the same way and had its failure read; the unit's other batches were separate
  invocations whose results were inspected.
