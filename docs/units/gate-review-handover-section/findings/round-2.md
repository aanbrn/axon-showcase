- [claim-unreproducible] docs/units/gate-review-handover-section/review-handover.md:103 — the round-2 hand-over says the
  unit's files are staged after the fixes, but `--review-ready` exits 1 naming six staged-then-edited paths whose index
  copies still hold the round-1 heading and the old completed-checks line; re-stage as the fix pass's last act and make
  the claim reproduce.
- [contradiction] openspec/changes/gate-review-handover-section/proposal.md:40 — the Impact says a copied or
  later-written heading still passes, while the delta says copied content beneath a matching current-round heading
  passes and a heading naming another round fails; reword the proposal to the delta's rule.
- [incomplete-enumeration] openspec/changes/gate-review-handover-section/specs/showcase/quality/commit-hygiene/spec.md:7
  — task 1.2 requires `Round 3x` not to match round 3, but the delta excludes only a digit run continuing past the token
  and accepts a suffix "separated from it", so `## Round 3x` still satisfies "begins with `Round <N>`"; state one rule:
  the text begins with `Round` and the exact token, followed by the end of the text or whitespace.
- [claim-unreproducible] openspec/changes/gate-review-handover-section/design.md:113 — the row makes a corpus-wide claim
  about quick-round headings but sources one file; source it with the corpus grep over the hand-over files.
- [test-control] openspec/changes/gate-review-handover-section/tasks.md:14 — two shapes the delta rejects have no task
  case: the unit `.` (only `..` is listed) and a backticked `Round <N>` heading (the delta forbids backticks); add both.
