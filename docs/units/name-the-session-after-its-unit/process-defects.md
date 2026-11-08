# Process defects — name-the-session-after-its-unit

### name-the-session-after-its-unit#1 — A findings entry with a multi-token location failed the instance shape

- **Recorded** — 2026-11-08, the implementing agent.
- **Stage** — review.
- **Expected** — each finding-instances entry matches `- [class] <location> — <claim>`, with the location a single
  token, as the previous unit's files do (`path` or `path:line`).
- **Observed** — three round files were written with a location phrase such as "delta `spec.md:12-14` vs
  `tasks.md:8-12`", and the checker read those entries as lines outside the instance shape.
- **Evidence** — `python3 scripts/commit-hygiene.py --finding-instances` printed "round 1 carries 3 line(s) outside the
  instance shape, first '- [single-sourcing] delta `spec.md:12-14` vs `tasks.md:8-12` — …'".
- **Consequence** — rounds 1 to 3's files were rewritten with a single-token location and the remaining fields in the
  claim; the ledger rows and the findings' substance were unaffected.
- **Knowledge boundary** — observed the shape failure and the repair. Did not check whether the skill's guidance states
  the one-token rule; the checker's `(\S+)` location group is what enforces it.

### name-the-session-after-its-unit#2 — A section heading was deleted again by an edit anchored across it

- **Recorded** — 2026-11-08, the implementing agent.
- **Stage** — review.
- **Expected** — an edit that inserts a new section after the last one leaves every existing heading in place.
- **Observed** — the edit's `oldString` ended with the `## Round 4 — proposal quick review after the round-3 fix`
  heading and its `newString` omitted it, so the new `advisor-proposal-1` section replaced that heading and the round-4
  bullets fell under it. This is the class the previous unit recorded as `gate-review-handover-section#1`.
- **Evidence** — `grep -n '^## '` after the edit listed four headings with no `## Round 4`, and the round-4 bullets sat
  directly under `## \`advisor-proposal-1\``.
- **Consequence** — repaired by moving the advisor block below the round-4 bullets and restoring the heading; the ledger
  rows, the finding files, and the change artifacts were unaffected.
- **Knowledge boundary** — observed the deletion and its repair. Did not change the edit tool's anchoring behaviour.
