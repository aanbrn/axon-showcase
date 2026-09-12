---
description: Audit AGENTS.md for consistency and conciseness with the pro-model agents-auditor subagent
---

Run the AGENTS.md audit to check the agent's persistent guidance for consistency and conciseness.

1. Invoke the `agents-auditor` subagent (`.opencode/agent/agents-auditor.md`) on the repository — it reads `AGENTS.md`
   in full as part of the audit.
2. Present its findings grouped by severity (contradiction / stale / dead reference / redundant / structural), each with
   its location and suggested rewrite.
3. Ask the user which findings to apply — do not edit `AGENTS.md` without their go-ahead.
4. Apply the approved findings, run `./gradlew spotlessApply` (markdown is formatter-wrapped), and re-run the
   `review-quick` subagent over the resulting diff before reporting the audit done.
