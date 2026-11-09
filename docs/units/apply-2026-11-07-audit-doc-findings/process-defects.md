# Process defects — apply-2026-11-07-audit-doc-findings

### apply-2026-11-07-audit-doc-findings#1 — The claims table took three rounds to cover one rule's assertions

- **Recorded** — 2026-11-09, by the main agent.
- **Stage** — review.
- **Expected** — the pre-write derivation pass derives the claims table from every assertion the edited rule makes, so
  the review's first round finds the table complete.
- **Observed** — the table was seeded from the scan candidates and the rows a round named; each of rounds 1–3 closed one
  instance (`claim-missing`, `claim-missing`, `claim-unreproducible`) and left another assertion unrowed.
- **Evidence** — `docs/units/apply-2026-11-07-audit-doc-findings/findings/round-1.md` through `round-3.md`, and the
  round-3 report's statement that the claim and source cells were authored separately.
- **Later instance** — after the base advance `#873` added an `#1984` fact to the same bullet, round 5 raised the same
  class on the new `#1984` row: its source did not reproduce the state and body the claim stated.
- **Consequence** — two extra quick rounds beyond the first; nothing shipped, and the closing round is clean.
- **Knowledge boundary** — observed the sequence of rounds and their findings; did not check whether the derivation
  pass's own instructions would have caught it, since the failure is in how the table was seeded rather than in the
  script.

### apply-2026-11-07-audit-doc-findings#2 — A tail-split rewrite of the hand-over dropped its later sections

- **Recorded** — 2026-11-09, by the main agent.
- **Stage** — review.
- **Expected** — refreshing the consultation section in `review-handover.md` replaces that section and keeps every later
  `## Round` section.
- **Observed** — a python rewrite split the file at the consultation heading and wrote `head + new section`, so every
  section after the heading (rounds 2–7) was dropped; `--agent-sessions` then failed on the missing `Round 5`–`Round 7`
  headings.
- **Evidence** — `commit-hygiene.py --agent-sessions` named the rows whose slugs no longer resolved; the prior content
  was recovered from an unreachable blob (`fe9cf521692f`) and the section replaced in place.
- **Consequence** — one recovery step; no content was lost, and the records are complete.
- **Knowledge boundary** — observed the dropped sections and the recovery; did not check whether another unit's
  hand-over rewrite used the same tail-split shape.
