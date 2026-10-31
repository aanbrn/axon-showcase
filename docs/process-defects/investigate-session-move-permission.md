# Process defects — investigate-session-move-permission

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### investigate-session-move-permission#1 — a class determination rested on the V1 contract

- **Recorded** — 2026-10-31, the main agent.
- **Stage** — implementation.
- **Expected** — a class determination cites the version-relevant permission contract.
- **Observed** — the record cited `/docs/permissions` (the V1 page) and the V1 schema and concluded a
  configuration-validation defect; the V2 permissions page states "V2 action names are strings, so plugins may define
  more actions", so the open set is intentional.
- **Evidence** — the pre-fix class section said "the defect is that the permission schema accepts a key outside the
  documented set"; the V2 page's Actions section carries the quote.
- **Consequence** — the record and the draft report were rewritten to a documented limitation, the publication was held,
  and no outward artifact was published. The quick loop (rounds 1–3) did not catch it; the pre-ship advisor did.
- **Knowledge boundary** — observed in this unit. The V2 permissions page was not checked until the advisor's
  correction; the V1 page's content was accurate for V1.
