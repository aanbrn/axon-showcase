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
