---
description:
  Audit the project's architecture for recorded-decision drift, unrecorded intent, and a record whose recorded premise
  is due (docs/adr/ + the service/module topology + the spec decomposition) with the pro-model architecture-auditor
  subagent
---

Run the architecture audit to check that the design the project records still matches the design it has.

1. Invoke the `architecture-auditor` subagent (`.opencode/agent/architecture-auditor.md`) on the repository — it reads
   `docs/adr/` in full plus the architectural surface (the service boundaries, the module dependency graph, and the spec
   corpus's capability decomposition); it establishes the authoritative record before asserting a violation (an
   unresolved disagreement is an advisory, not a finding), inspects both sides of a claimed mismatch, and reports a
   dependency edge only where a recorded rule forbids it; it bounds its cohesion/coupling advisory to a concrete
   inconsistency or a meaningful missing decision, works in stages, and classifies each `Revisit when:` condition before
   reporting it. It sweeps the intent-clarification surfaces (dependency `exclude(...)` declarations, the suppressed
   coordinates, held-back coordinates, and npm-suppressed packages in `config/dependency-updates/major-disabled.txt`,
   `config/dependency-updates/hold-back.txt`, and `config/web-ui-updates/major-disabled.txt`, the suppression
   annotations and retained deprecated APIs, the deferrals and band-aids recorded in ADRs or `docs/ideas.md`, an ADR's
   `Revisit when:` condition that appears met, and a parked `docs/ideas.md` entry whose work has already landed — or
   only partly landed, or whose objective a different mechanism achieves).
2. Present its report in the contract it returns (the verdict line first, then the two sections — **findings** and
   **advisory** observations, each presented as the shared report contract requires — do not re-summarize it).
3. Ask the user which findings to apply — an advisory observation, including an intent-clarification item (a question
   for the user to answer), is the user's call and is never "fixed" automatically. Do not edit files without their
   go-ahead.
4. Apply the approved changes, run `./gradlew spotlessApply` for the formatter-wrapped files (`AGENTS.md`, `README.md`,
   `docs/adr/`, the specs/change dirs, and the project-authored `.opencode/` markdown the audit edited), then re-run the
   `review-quick` subagent over the resulting diff before reporting the audit done.
