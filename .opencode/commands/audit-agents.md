---
description: Audit the project-owned agent tooling (AGENTS.md + project-authored .opencode/ files) with the pro-model
  agents-auditor subagent
---

Run the agent-tooling audit to check the project's guidance and agent files for consistency and conciseness.

1. Invoke the `agents-auditor` subagent (`.opencode/agent/agents-auditor.md`) on the repository — it reads `AGENTS.md`
   and the project-authored `.opencode/` files in full, skipping generated (OpenSpec instruction files) and vendored
   (`axon4to5-*`) ones.
2. Present its findings grouped by severity (contradiction / stale / dead reference / redundant / structural), each with
   its location and suggested rewrite.
3. Ask the user which findings to apply — do not edit files without their go-ahead.
4. Apply the approved findings, run `./gradlew spotlessApply` for the formatter-wrapped files (`AGENTS.md`, `README.md`,
   the specs/change dirs) and a manual 120-character check for `.opencode/*.md` (outside Spotless), then re-run the
   `review-quick` subagent over the resulting diff before reporting the audit done.
