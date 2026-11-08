# Process defects — gate-review-handover-section

### gate-review-handover-section#1 — A section heading was deleted by an edit anchored past it

- **Recorded** — 2026-11-08, the implementing agent.
- **Stage** — review.
- **Expected** — an edit that appends a new hand-over section after an existing one leaves every existing heading in
  place, and the edit's anchor stays inside the unit being changed.
- **Observed** — the edit's `oldString` began at the end of the `Round 2` section and ran through the
  `## Round 3 — proposal quick review after the round-2 fixes` heading, and its `newString` omitted that heading, so the
  new `advisor-scope-3` section landed above the round-3 bullets and the heading was deleted.
- **Evidence** — `grep -n '^## ' docs/units/gate-review-handover-section/review-handover.md` after the edit listed five
  headings with no `## Round 3`, and the round-3 bullets sat directly under `## \`advisor-scope-3\``.
- **Consequence** — the record's structure was wrong until a script moved the `advisor-scope-3` block below the round-3
  bullets and restored the heading; the review ledger, the finding instances, and the change artifacts were unaffected.
- **Knowledge boundary** — observed the dropped heading and its repair. Did not check whether any gate would have caught
  it; the pre-commit guard and `--review-ready` inspect the staged set, not a file's heading structure.
- **Decision** — no-change: covered by `AGENTS.md`'s programmatic-edit rule (anchor on the text being changed) and by
  the span-edit rule.
