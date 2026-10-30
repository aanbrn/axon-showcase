# Process defects — generalize-process-defects-2026-10-30

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

Workspace: unit=generalize-process-defects-2026-10-30; path=/Users/afanasyev/Development/axon-showcase;
branch=generalize-process-defects-2026-10-30; writer=ses_e731f42aaffeKQAMVYfJ2KxAYN

### generalize-process-defects-2026-10-30#1 — A lesson-capture report's "exact text" did not match the span it named

- **Recorded** — 2026-10-30, by the writing session (`ses_e731f42aaffeKQAMVYfJ2KxAYN`), during the proposal stage.
- **Stage** — proposal (the `lesson-capture` hand-off).
- **Expected** — a proposal's "exact text" is the text to apply at the anchor it names.
- **Observed** — the P1 proposal said it replaced the sentence beginning "compute what the action needs into a `val`",
  but the quoted text began at "`val`/`fun`, the script's own `this`"; applied as written it would have dropped
  `project`, `gradle`, `rootProject`, and `providers` from the bullet's exclusion list.
- **Evidence** — the report's P1 block (its "replacing" clause against its quoted text), read against the pack's own
  sentence; the applied edit was re-derived from `.opencode/skills/gradle-build/SKILL.md`.
- **Consequence** — one verification read; no artifact carried the dropped list.
- **Knowledge boundary** — observed: the report's text and the pack. Not checked: whether the report's other quoted
  spans match their anchors.

### generalize-process-defects-2026-10-30#2 — An "already covered" citation named a constraint, not the obligation

- **Recorded** — 2026-10-30, by the writing session (`ses_e731f42aaffeKQAMVYfJ2KxAYN`), during the proposal stage.
- **Stage** — proposal (the `lesson-capture` hand-off).
- **Expected** — a report's "already covered" citation names the rule that determines the proposed action.
- **Observed** — the report called `proofread-readme-language#1` "already covered" by `technical-advisor.md:88-93`, but
  that text states the advisor's own workspace limit, not the caller's obligation to start the delegation in the unit's
  tree; the record's disposition moved from already-covered to below the gate.
- **Evidence** — the report's disposition line and `.opencode/agent/technical-advisor.md:88-93`, read against the
  record.
- **Consequence** — one verification read; the disposition changed; no artifact carried the wrong coverage.
- **Knowledge boundary** — observed: the citation and the definition. Not checked: whether the report's other "already
  covered" citations name a rule that determines the action (the consultation checked P2/P3 and the two largest
  recurrences).

### generalize-process-defects-2026-10-30#3 — A proposal carried an unverified retrospective claim

- **Recorded** — 2026-10-30, by the writing session (`ses_e731f42aaffeKQAMVYfJ2KxAYN`), during the proposal stage.
- **Stage** — proposal (the `lesson-capture` hand-off, the P3 draft).
- **Expected** — a proposal's supporting claim is verified against its source or labelled a hypothesis.
- **Observed** — the P3 draft said "The next `/retrospective` did not confirm the class (the reports stop at
  `docs/retrospectives/2026-10-23.md`)"; the date is right, but no retrospective's content was read, so "did not
  confirm" was an unverified inference. The sentence was dropped before the proposal was presented.
- **Evidence** — the report's P3 block and `ls docs/retrospectives/` (the latest is `2026-10-23.md`); the sentence is
  absent from the presented proposal and the pull-request body.
- **Consequence** — one verification read; nothing shipped on the sentence, and the P3 disposition changed on the
  consultation's class-mismatch ground instead.
- **Knowledge boundary** — observed: the report's sentence and the directory listing. Not checked: whether any of the
  three retrospectives discusses the class, which a content read would establish.
