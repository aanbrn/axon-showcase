- [contradiction] openspec/changes/pin-wrapper-distribution-checksum/design.md:154 — the consequence said a bump leaves
  the four files as found, omitting the incomplete-restore outcome the tool now reports.
- [contradiction] openspec/changes/pin-wrapper-distribution-checksum/design.md:84 — a killed process was listed among
  the cases that "say the restore is incomplete", though it writes no report.
- [over-claim] scripts/update-gradle-wrapper.py:23 — the docstring and the command said the report states an outcome for
  a run that stopped before touching anything, while those paths printed only the error.
- [contradiction] scripts/test-update-gradle-wrapper.py:303 — the interruption case was named for keeping the snapshot
  while its assertion required the snapshot to be gone.
- [single-sourcing] openspec/changes/pin-wrapper-distribution-checksum/design.md:129 — a second cold-cache sentence
  repeated the acceptance claim two paragraphs after the caveat-bearing statement.
- [test-control] openspec/changes/pin-wrapper-distribution-checksum/design.md:86 — the claim that the snapshot path is
  printed only after the capture had no case.
