- [contradiction] design.md:64-65 — "panel ids are unique across the dashboard" beside "only top-level panels are
  compared", while the delta's id scenario names any two panels.
- [over-claim] design.md:90-91 — the risk says the new overlap rule controls the 18-panel shift, but the rule compares
  non-row panels only and cannot see a gap.
- [over-claim] design.md:63-65 — the "every non-row panel has a query expression" rule fails a legitimate text panel,
  while its justification is about empty metric panels.
- [missing-task] tasks.md:15-21 — task 1.1 fixes y, width and the shift but not the panels' height, ids, datasource
  reference, or label selector.
- [claim-unreproducible] design.md:133 — "no production source set" contradicts the proposal's own build-logic src/main
  path.
- [over-claim] design.md:126 — "Every base panel is a timeseries" is false for the 7 row panels.
- [incomplete-enumeration] design.md:137-139 — the dismissals name three scan kinds while the scan emits cross-ref
  candidates too.
- [contradiction] unit.md:20-21 — the record says nothing is staged while the unit's files are staged.
- [contradiction] unit.md:18-19 — the checkpoint makes an advisor consultation an unconditional next step, which no rule
  authorizes by default.
- [test-control] tasks.md:34-37 — the manual gate control mutates the dashboard without a snapshot or a verified
  restore.
- [structured-writing] design.md:63-65 — decision 4 compresses three rules and a scope caveat into one sentence.
