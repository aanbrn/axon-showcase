---
description:
  Audit the project-owned agent tooling with the pro-model agents-auditor subagent, including the always-loaded
  instruction budget and the authoring-system gaps that keep producing a defect
---

Run the agent-tooling audit, which reports only — it never edits a file.

**Trigger** — the owner invokes `/audit-agents`, or the scheduled `audit` workflow runs it report-only.

**Procedure**

1. Invoke the `agents-auditor` subagent (`.opencode/agent/agents-auditor.md`) on the repository. Its definition owns
   what it reads, the surfaces it sweeps, its finding classes, its exclusion boundary, and its report contract.
2. Present its report in the contract it returns — the verdict line first, then its findings, advisory, accretion, and
   authoring-system-gap classes, its instruction-budget ledger, and its candidate classes, each as the shared report
   contract requires. Do not re-summarize it.
3. Ask the owner which findings to apply.

**Decision points**

- IF the report carries nothing actionable, or the owner approves nothing, THEN report that and stop — create no unit.
- IF the approved findings differ in delivery route or in scope, THEN split them into separate units, each scoped to its
  own artifacts and diff.
- Do not batch unrelated fixes merely because this report carried them together.
- An advisory observation — including one about a generated or vendored file — is the owner's decision, never an
  automatic fix.
- A reduction candidate is drained by the standing `/apply-audit-reductions` unit, not applied here.

**Applying approved findings**

- Report generation and applying are separate: the applying is a unit of its own, never a continuation of this run.
- Route each finding by its kind. `AGENTS.md`'s "Workflow: Author a spec delta and archive" owns the spec route (a delta
  now, the main spec only at archive); a docs correction is a docs unit; and a command or subagent-definition change
  that alters spec-covered behavior goes through the normal change workflow with that capability's delta — the route
  this audit's definition findings take, never a docs edit.
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
