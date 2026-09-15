---
description:
  Audit the project's architecture for recorded-decision drift and unrecorded intent (docs/adr/ + the service/module
  topology + the spec decomposition) with the pro-model architecture-auditor subagent
---

Run the architecture audit to check that the design the project records still matches the design it has.

1. Invoke the `architecture-auditor` subagent (`.opencode/agent/architecture-auditor.md`) on the repository — it reads
   `docs/adr/` in full plus the architectural surface (the service boundaries, the module dependency graph, and the spec
   corpus's capability decomposition), and sweeps the intent-clarification surfaces (dependency `exclude(...)`
   declarations, the major-version-suppressed coordinates, the suppression annotations and retained deprecated APIs, and
   the deferrals and band-aids recorded in ADRs or `docs/ideas.md`).
2. Present its report in the contract it returns (the verdict line first, then the two sections — **findings** and
   **advisory** observations, each budgeted per item).
3. Ask the user which findings to apply — an advisory observation, including an intent-clarification item (a question
   for the user to answer), is the user's call and is never "fixed" automatically. Do not edit files without their
   go-ahead.
4. Apply the approved changes, run `./gradlew spotlessApply` for the formatter-wrapped files (`AGENTS.md`, `README.md`,
   `docs/adr/`, the specs/change dirs, and the project-authored `.opencode/` markdown the audit edited), then re-run the
   `review-quick` subagent over the resulting diff before reporting the audit done.
