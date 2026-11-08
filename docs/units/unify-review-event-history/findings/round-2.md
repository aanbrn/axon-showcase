- [contradiction] specs/showcase/quality/commit-hygiene/spec.md:316-320 — the quick scenario passes a row "with the
  round as its pass", while the format makes the pass the invocation and the round a separate field; the design and
  proposal carry the old model
- [contradiction] specs/showcase/quality/commit-hygiene/spec.md:69-72 — the historical scenario still fails any
  six-field row dated after 2026-10-18, which is a post-retirement row, and the paragraph over-ranges a unit first
  rounded on that day
- [contradiction] specs/showcase/quality/agent-skills/spec.md:133-136 — the stalled replacement is described as one row
  naming the stalled identifier, while the register requires two rows and a fresh stall has no session field for it
- [coherence] design.md:120-124 — the digest's join assumes one quick row per round, while a stalled round now carries
  two; the resolved row's selection is unstated and untested
- [contradiction] specs/showcase/quality/commit-hygiene/spec.md:211-214 — the general slug clause and its scenario still
  cover quick rows, whose heading is `Round <N>`
- [incomplete-enumeration] design.md:149 — the amended round-continuity clause still says the mode governs only
  nine-field units, though it now judges every unit's width
- [claim-unreproducible] design.md:153 — the amended hand-over before-clause quotes `commit-hygiene`'s backticked form,
  not the `agent-skills` hand-over requirement's own text
- [test-control] specs/showcase/quality/commit-hygiene/spec.md:337-340 — a fourteen-field row on or after the boundary
  and a pre-boundary fourteen are unruled and untested
- [test-control] specs/showcase/quality/commit-hygiene/spec.md:289-299 — a non-quick row's round is unruled and untested
- [structured-writing] openspec/changes/unify-review-event-history/tasks.md:30-33 — task 1.9 packs four independent
  edits
- [wording] openspec/changes/unify-review-event-history/proposal.md:22 — "per-unit widths" names the wrong record's
  widths
