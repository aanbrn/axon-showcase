# Design — prefer-claims-literal-results

## Context

The retrospective's S2 (`docs/retrospectives/2026-10-31.md`) and the advisor's narrow scope: a preference for an
explicit-literal Result where the complete assertion and the supported command fit the mode. The spec's quick-review
requirement names the claims table as the `review-gate` skill's and does not state the row's result form; the mode's
eligibility requires an explicit literal-only Result, so a prose Result is advisory (6 of 44 historical rows are
executable).

## Goals / Non-Goals

**Goals**

- An authoring preference: a row whose result is an output states the literal the command prints.
- The explanation stays in the Claim cell, so the Result stays literal-only.
- No weakening: the literal represents the full claim.

**Non-Goals**

- A new table column or a required format.
- A change to the mode.
- Invalidating a prose Result (it stays valid and advisory).

## Decisions

### D1 — the preference is conditional

A row whose result is an output states the expected literal or literal list **where the complete assertion and the
supported command fit the mode**. Alternatives: a required format (rejected — a prose result can be legitimate); a new
column (rejected — it rewrites every table).

### D2 — the explanation stays outside the result cell

The mode's eligibility is literal-only; a mixed cell is advisory. The explanation belongs in the Claim cell.
Alternatives: allow prose in the Result (rejected — the mode cannot compare it).

### D3 — no weakening

The literal must represent the full claim; a checked row is not a goal at the cost of the evidence. Alternatives: a
partial literal (rejected — it passes a narrower claim).

### D4 — the location

`review-gate` §1, beside the `--verify` pointer, with `captured: retrospective-2026-10-31`.

### D5 — `skip_specs`

The spec does not state the row's result form; the preference is a procedure detail, so no delta is owed.

## Risks / Trade-offs

- **The preference may read as a mandate** — the conditional wording and the prose escape state otherwise.
- **The coverage may not rise** — the verification reads the checked/skipped counts by skip reason and whether the
  literals represent the full claim; a higher checked fraction alone is not the goal.

## Claims table

Base: `52f001e58d71b881cc1125f0362b736e9656766b` (`git merge-base HEAD origin/main`).

| Claim                                                                                 | Source                                                                                                        | Result                                                       |
| ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------ |
| The retrospective reports `claim-unreproducible` at 275 (20%) and the pilot's 6-of-44 | `grep -n '275 (20%' docs/retrospectives/2026-10-31.md`; `grep -n '6 of 44' docs/retrospectives/2026-10-31.md` | 14, 49                                                       |
| The spec names the claims table as the `review-gate` skill's                          | `grep -n 'the claims table' openspec/specs/showcase/quality/agent-skills/spec.md`                             | 1814, 1837                                                   |
| The `--verify` bullet is at `review-gate` §1                                          | `grep -n 'claim-scan.py --verify' .opencode/skills/review-gate/SKILL.md`                                      | 84                                                           |
| No capability spec states the claims-table row's result form                          | `grep -rn -i 'result form' openspec/specs/`; `grep -rn 'claims table' openspec/specs/`                        | no `result form` hit; `claims table` hits at 930, 1814, 1837 |
