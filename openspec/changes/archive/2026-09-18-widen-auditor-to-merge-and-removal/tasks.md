- [x] 1.1 Add the two standing analyses and their tests to the auditor's conciseness axis in
      `.opencode/agent/agents-auditor.md`, with the `merge` / `remove` labels.
- [x] 1.2 Name both counts in its report contract's verdict line.
- [x] 1.3 Update `.opencode/commands/audit-agents.md`'s output description.
- [x] 1.4 Update the `agents-auditor` bullet in `AGENTS.md`, the two `README.md` table rows, and the README prose.
- [x] 1.5 The delta modifies the tooling-audit requirement: the conciseness paragraph gains the standing analyses and
      their tests, and the audit-produced scenario names the counts, carrying all seven existing scenarios.
- [x] 2.1 `spotlessApply` after the final edit, `spotlessCheck`, and `openspec validate --all`.
- [x] 2.2 `review-quick` until clean before asking for the manual pass.
- [x] 2.3 Smoke-ran `/audit-agents` (seeded: a duplicate pair + a trivia bullet): verdict
      `3 findings (1 merge, 1     removal), 0 advisory, 11 accreted`; both classes reported with their evidence and the
      trivia seed's fact verified wrong — the behaviour works, seeds reverted.
- [ ] 3.1 Record the capability `## Purpose` refresh (its auditor-output enumeration gains the merge and removal counts)
      and apply it in the archive commit.
