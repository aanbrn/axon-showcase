---
description:
  Audit the project-owned agent tooling (AGENTS.md + project-authored .opencode/ files) with the pro-model
  agents-auditor subagent
---

Run the agent-tooling audit to check the project's guidance and agent files for consistency and conciseness.

1. Invoke the `agents-auditor` subagent (`.opencode/agent/agents-auditor.md`) on the repository — it reads `AGENTS.md`
   and the project-authored `.opencode/` files in full, and also reads the generated (OpenSpec instruction files) and
   vendored (`axon4to5-*`) ones to report any that contradict how the repository uses them, and reports the accreted
   meta rules with their origins (the `captured:` marker, or `git blame` / `git log -S`); it never edits an excluded
   file.
2. Present its report in the contract it returns (the verdict line first, then findings budgeted per item and grouped by
   severity, plus separate advisory (third-party inconsistency) and accretion (meta rules with their origins) classes,
   and the merge and removal candidates counted in the verdict line).
3. Ask the user which findings to apply — do not edit files without their go-ahead.
4. Apply the approved findings, run `./gradlew spotlessApply` for the formatter-wrapped files the audit edited
   (`AGENTS.md` and the project-authored `.opencode/` markdown — the audit's `README.md` copy is fixed by the change's
   docs sweep, and its spec-side copy routes to the corpus owner), then re-run the `review-quick` subagent over the
   resulting diff before reporting the audit done.
