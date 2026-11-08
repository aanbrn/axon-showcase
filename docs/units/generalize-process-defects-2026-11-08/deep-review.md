# Deep review — generalize-process-defects-2026-11-08

## Routing decision

- **Decided** — 2026-11-08, before any deep-review session, from the completed implementation and the existing review
  evidence.
- **Outcome** — `RECOMMENDED`, awaiting the owner's explicit run-or-skip resolution.
- **Floor and signals** — `python3 scripts/review-routing.py --unit generalize-process-defects-2026-11-08` reports
  `RECOMMENDED` on `[repeated-class] 2 rows repeat a prior class` (rounds 2 and 3 repeated round 1's
  `claim-unreproducible`, and round 5 is clean).
- **Why not lower** — the change edits `AGENTS.md`, the always-loaded guidance every task reads, and the analysis's own
  admission was that P1's overlap with the verdict-echo rule is a judgment call; a deep pass would test that overlap and
  the applied text against the corpus.
- **Why not `REQUIRED`** — the change is a localized semantic one: a single clause merged into an existing guidance
  bullet, one parked entry, and decision lines, with the gates and the specs untouched. That is a claim about the
  change's semantics and evidence, not an exemption for its file type — documentation-only status is not a bypass, as
  the routing requirement states.
- **An advisor's reading of the signal** — `advisor-routing-1` observes that the repeated-class floor is historical (it
  counts rows without discounting later clean ones), that the recurrence concerned the claims-table derivation which the
  loop then repaired at its root, and that P1's overlap with the verdict-echo rule is not a contradiction: the two
  obligations can hold at once, and P1 requires reading the findings, not fixing every advisory.

## Passes

None yet: the `RECOMMENDED` outcome needs the owner's run-or-skip decision first.

## Owner's resolution

- **Resolved** — 2026-11-08: the owner chose to **skip** the `RECOMMENDED` deep review, on `advisor-routing-1`'s
  recommendation and the reasoning above.
- **What that records** — no deep-review session is created, no hand-over is prepared, and no expensive-model invocation
  is spent; the manual quick review stands as the review of record for this unit.
- **Limits of the skip** — the revised claims-dismissals block was not independently reproduced by the advisor, since
  the saved pull-request body was not supplied to it; the quick loop's clean rounds are the evidence that it converged.
