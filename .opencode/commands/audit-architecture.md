---
description: Audit the project's architecture for recorded-decision drift and unrecorded intent (docs/adr/ + the
  service/module topology + the spec decomposition) with the pro-model architecture-auditor subagent
---

Run the architecture audit to check that the design the project records still matches the design it has.

1. Invoke the `architecture-auditor` subagent (`.opencode/agent/architecture-auditor.md`) on the repository — it reads
   `docs/adr/` in full plus the architectural surface (the service boundaries, the module dependency graph, and the spec
   corpus's capability decomposition), and sweeps the intent-clarification surfaces (dependency `exclude(...)`
   declarations, the major-version-suppressed coordinates, the suppression annotations and retained deprecated APIs, and
   the deferrals and band-aids recorded in ADRs or `docs/ideas.md`).
2. Present its report in the two sections it returns: **findings** (verified drift, each with a location and a suggested
   correction) and **advisory** observations (design judgment, not defects).
3. Ask the user which findings to apply — an advisory observation, including an intent-clarification item (a question
   for the user to answer), is the user's call and is never "fixed" automatically. Do not edit files without their
   go-ahead.
4. Apply the approved changes, run `./gradlew spotlessApply` for the formatter-wrapped files (`AGENTS.md`, `README.md`,
   `docs/adr/`, the specs/change dirs) and a manual 120-character check on the `.opencode/` files the audit edited
   (`node_modules`, the generator-written `opsx-*` commands and `openspec-*` skills, and the vendored skills are out of
   scope; the project-authored `opsx-tool-update.md` stays in scope), then re-run the `review-quick` subagent over the
   resulting diff before reporting the audit done.
