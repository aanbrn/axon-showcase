---
description: Audit the openspec/specs corpus for structure and consistency with the pro-model specs-auditor subagent
---

Run the spec-corpus audit to check the specs for structural consistency.

1. Invoke the `specs-auditor` subagent (`.opencode/agent/specs-auditor.md`) on the repository — it reads the
   `openspec/specs/` corpus in full as part of the audit.
2. Present its findings grouped by severity (structural / stale / duplicate / dead reference), each with its location
   and suggested rewrite.
3. Ask the user which findings to apply — do not edit specs without their go-ahead. A spec edit is itself a change, so
   route accepted fixes through the normal change workflow (a delta, synced to the main spec at archive), not a direct
   main-spec edit.
