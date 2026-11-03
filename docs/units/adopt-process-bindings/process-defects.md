# Process defects — adopt-process-bindings

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### adopt-process-bindings#1 — Edited a staged path without re-staging it

- **Recorded** — 2026-11-02, by the unit's agent.
- **Stage** — review.
- **Expected** — the review gate's §5 re-stage rule: after a fix, re-stage the changed paths before the tree is offered
  for review, so the index and the working tree cannot disagree.
- **Observed** — round 1's fixes were applied to `.opencode/agent/experience-analyzer.md`,
  `.opencode/commands/review-thorough.md`, `docs/adr/0026-process-bindings-are-declared.md`,
  `docs/contracts/process-bindings.md`, `docs/ideas.md`, and the change dir's `design.md`, `proposal.md`, and `tasks.md`
  after those paths had been staged, and no re-stage followed.
- **Evidence** — `git status --porcelain` reported `MM` for five paths (`experience-analyzer.md`, `review-thorough.md`,
  `ADR-0026`, `process-bindings.md`, `docs/ideas.md`) and `AM` for three (`design.md`, `proposal.md`, `tasks.md`), with
  the ledger row unstaged. Round 2's report recorded the same event as its finding 7, though its path list omitted
  `docs/ideas.md`. The pre-commit guard would have refused the commit.
- **Consequence** — none shipped: round 2 caught it, and the round-3 fix re-staged every path (`--review-ready` exit 0).
  One review round was spent on an artifact defect rather than a content defect.
- **Knowledge boundary** — observed: the staged-set state and the finding. Assumed: that the guard's refusal is what
  would have surfaced it next. Not checked: whether any earlier unit shipped a stale copy the same way.
