---
description:
  Audit the project's architecture for recorded-decision drift, unrecorded intent, and a deferral whose recorded
  condition is due (docs/adr/, the service/module topology, and the spec decomposition) with the pro-model
  architecture-auditor subagent
---

Run the architecture audit, which reports only — it never edits a file.

**Trigger** — the owner invokes `/audit-architecture`, or the scheduled `audit` workflow runs it report-only.

**Procedure**

1. Invoke the `architecture-auditor` subagent (`.opencode/agent/architecture-auditor.md`) on the repository. Its
   definition owns the surfaces it sweeps, how it establishes the authoritative record, how it classifies an ADR's
   recorded `Revisit when:` condition, and its report contract.
2. Present its report in the contract it returns — the verdict line first, then its findings and advisory sections, each
   item as the shared report contract requires. Do not re-summarize it.
3. Ask the owner which findings to apply.

**Decision points**

- IF the report carries nothing actionable, or the owner approves nothing, THEN report that and stop — create no unit.
- IF the approved findings differ in delivery route or in scope, THEN split them into separate units, each scoped to its
  own artifacts and diff.
- Do not batch unrelated fixes merely because this report carried them together.
- An advisory observation, an intent-clarification item included, is a question for the owner: it is the owner's call
  and is never "fixed" automatically.
- A reduction candidate is drained by the standing `/apply-audit-reductions` unit, not applied here.

**Applying approved findings**

- Report generation and applying are separate: the applying is a unit of its own.
- Route each finding by its kind — a docs correction (an ADR, `AGENTS.md`, `README.md`) is a docs unit; a code change,
  or a command or subagent-definition change altering spec-covered behavior, goes through the normal change workflow
  with that capability's delta; a spec change follows `AGENTS.md`'s "Workflow: Author a spec delta and archive". Never
  edit a main spec outside archive.
- Run the full per-unit review gate over the result. The `review-gate` skill owns its steps, including the
  reviewer-session preflight; this command points at it rather than restating it.

**Verification** — `./gradlew spotlessApply` for the formatter-wrapped files the unit edited.

**Completion criteria** — the owner has the report and has chosen; or, for an approved selection, every unit is applied
and has completed the review gate with the manual pass requested.

**Repository lifecycle**

- A new unit starts on its own branch, created from a freshly fetched `origin/main`; a unit continuing an existing
  branch or pull request works on that branch.
- The unit's working tree — the primary checkout when no other unit holds it, otherwise the sibling worktree
  `../axon-showcase-<unit>` — is established and recorded before the first write, with an ownership it cannot establish
  treated as occupied, per `AGENTS.md`'s "Workflow: Change lifecycle" **Workspace** rule.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval.
- The full per-unit review gate runs over the result. `AGENTS.md`'s "Workflow: Change lifecycle" owns the branch,
  uncommitted-work, and approval steps; the `review-gate` skill owns the review steps, including the reviewer-session
  preflight; this command points at them rather than restating them.
