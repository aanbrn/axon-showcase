# Process defects — adopt-spec-delta-role

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### adopt-spec-delta-role#1 — A text edit duplicated the design file

- **Recorded** — 2026-11-02, by the unit's agent.
- **Stage** — implementation.
- **Expected** — a fix that rebuilds a section of a markdown file replaces that section and leaves the rest of the
  document once.
- **Observed** — the claims-table rewrite computed its end boundary as the next `## ` heading, which did not exist after
  the table, so the slice ran to the end of the file and the earlier content was re-appended: the design carried its
  Context, Decisions, trace, Evidence and Claims table twice.
- **Evidence** — `grep -n '^## '` on the file listed each heading twice before the rewrite and once after; the unit's
  hand-over reported the duplication, and the review round that prompted this record relayed that report.
- **Consequence** — the file was rewritten whole; no content was lost, and the unit spent one edit and one review
  finding on it.
- **Knowledge boundary** — observed: the duplicated headings and the rewrite. Assumed: that no other file in this unit
  was rebuilt the same way, which the single `grep` covered for this file only. Not checked: the same pattern in other
  units' artifacts.
