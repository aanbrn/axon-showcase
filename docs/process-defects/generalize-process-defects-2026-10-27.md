# Process defects — generalize-process-defects-2026-10-27

### generalize-process-defects-2026-10-27#1 — Proposals cleared the promotion gate without the evidence it requires

- **Recorded** — 2026-10-27, main agent session, proposal stage of the generalization unit.
- **Stage** — proposal.
- **Expected** — a proposal that reaches the owner clears the `agent-skills` promotion gate: it may not add an action
  already determined at another site, needs adequate independent evidence, and may not widen a rule past the incident it
  cites.
- **Observed** — the `lesson-capture` analysis handed over three proposals the gate's contract does not support. The
  `AGENTS.md` branch-remedy proposal carried a causal explanation the record contradicts ("No documented settle step
  performs an upstream operation") and added narrative beside a clause correction; the `review-gate` §1 proposal forbade
  a line range generally, though the incident establishes only a wrongly selected range and a wrong revision, and §1
  already requires reproduction, base anchoring, and re-derivation after edits; and the §2 recommendation retained an
  existing round-state synchronization duty rather than removing the duplicate round inventory the consultation
  identified.
- **Evidence** — an independent consultation (`technical-advisor`, session `ses_e8372c65cffeYKp36504nNT4hB`) read the
  records and guidance and confirmed all three as problems: the incident text at
  `docs/process-defects/clarify-readme-self-learning-wording.md:34–36`; §1's existing obligations at
  `.opencode/skills/review-gate/SKILL.md:47–77`; the promotion contract at
  `openspec/specs/showcase/quality/agent-skills/spec.md:2614–2631`; the incident at
  `docs/process-defects/extend-owner-reply-language-scope.md:9–20`; and the clean-round heading the archived review
  required,
  `openspec/changes/archive/2026-10-27-separate-lesson-capture-from-implementation/review-handover.md:289–303`.
- **Consequence** — the reduced scope was applied instead: the branch remedy became
  `git branch --unset-upstream <branch>` with its provenance marker, the `RECOMMENDED` resolution became one
  owner-decision rule carrying the ask-and-stop and the recorded answer, §2 was split into "before the invocation" and
  "when the report arrives" with the round-state duplication removed, the §1 proposal was dropped, the claims-execution
  design was parked in `docs/ideas.md`, and the table-split incident was linked to the existing parked table-integrity
  entry. No artifact shipped the over-scoped text. Applied net `AGENTS.md` delta 0 (one clause correction and one marker
  on an existing line); the `review-gate` edits net +3 lines.
- **Knowledge boundary** — observed for this unit's analysis. Not established: whether the promotion-gate wording or its
  application produced the over-reach, and whether the same shape recurs in a later generalization.

### generalize-process-defects-2026-10-27#2 — The unit's review-round rows were not appended as each report arrived

- **Recorded** — 2026-10-27, main agent session, review stage.
- **Stage** — review.
- **Expected** — the review gate's §2 "When the report arrives" step, which this same unit wrote, appends each round's
  row to `docs/review-rounds.tsv` before applying a fix, then runs both ledger checks.
- **Observed** — rounds 1 and 2 were reviewed and fixed with no row appended, and only round 3's review found the gap
  (`--unique-rounds --expect …:1` exited 1 with "no row for round 1"). The three rows were backfilled when it was found.
- **Evidence** — `python3 scripts/commit-hygiene.py --unique-rounds --expect generalize-process-defects-2026-10-27:1`
  printed "Unit 'generalize-process-defects-2026-10-27' has no row for round 1, the round whose report arrived" and
  exited 1; `grep "generalize-process-defects-2026-10-27" docs/review-rounds.tsv` returned nothing.
- **Consequence** — rows 1–3 were appended with the reviewer session `ses_e83542e5fffeVesiFpWcjMEiUd` (created, then
  continued twice), and both ledger checks then passed. The unit shipped no artifact with an untracked round.
- **Knowledge boundary** — observed for this unit. Not established: whether the new §2 step's placement prevents the
  miss in a later unit, or whether the caller simply skipped a step it had just written.
