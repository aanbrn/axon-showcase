- [x] 1.1 Add the deletion-of-a-duplicate disposition to the auditor's merge sentence in
      `.opencode/agent/agents-auditor.md`.
- [x] 1.2 Carry it into the `AGENTS.md` auditor bullet.
- [x] 1.3 The delta modifies the tooling-audit requirement: the conciseness clause's merge sentence and the
      standing-findings scenario gain the disposition, carrying all eight existing scenarios.
- [x] 2.1 `spotlessApply` after the final edit, `spotlessCheck`, and `openspec validate --all`.
- [x] 2.2 `review-quick` until clean before asking for the manual pass.
- [x] 2.3 Seeded smoke-run before archiving: the audit reported the planted duplicate as "a deletion of the duplicate,
      not a merge", naming the survivor — but classified it only as `redundant` with no `remove` label, so the verdict's
      count missed it. Fixed: the disposition now says it is reported as a `remove`, and the label line labels by
      disposition. Seed reverted.
- [x] 3.1 Capability `## Purpose` needs no edit: its enumeration already names the merge and removal candidates, and a
      deletion of a duplicate is counted within removal. removal candidates) — tick without an edit if the archive shows
      no scope change.
