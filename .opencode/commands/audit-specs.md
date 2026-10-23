---
description: Audit the openspec/specs corpus for structure and consistency with the pro-model specs-auditor subagent
---

Run the spec-corpus audit, which reports only — it never edits a file.

**Trigger** — the owner invokes `/audit-specs`, or the scheduled `audit` workflow runs it report-only.

**Procedure**

1. Invoke the `specs-auditor` subagent (`.opencode/agent/specs-auditor.md`) on the repository. Its definition owns the
   corpus it reads, its finding classes, the false-positive classes it verifies a dead reference against, and its report
   contract.
2. Present its findings in the contract it returns — the verdict line first, then findings budgeted per item and grouped
   by severity. Do not re-summarize it.
3. Ask the owner which findings to apply.

**Decision points**

- IF the report carries nothing actionable, or the owner approves nothing, THEN report that and stop — create no unit.
- IF the approved findings differ in delivery route or in scope, THEN split them into separate units, each scoped to its
  own artifacts and diff.
- Do not batch unrelated fixes merely because this report carried them together.

**Applying approved findings**

- Report generation and applying are separate: the applying is a unit of its own.
- A spec edit is itself a change, so every accepted fix routes through an OpenSpec change carrying a delta spec, with
  the main spec synced only at archive — never as a direct main-spec edit. `AGENTS.md`'s "Workflow: Author a spec delta
  and archive" owns the procedure.
- Run the full per-unit review gate over the result. The `review-gate` skill owns its steps, including the
  reviewer-session preflight; this command points at it rather than restating it.

**Verification** — `openspec validate --all` and `openspec validate --changes`, plus `./gradlew spotlessApply`.

**Completion criteria** — the owner has the report and has chosen; or, for an approved selection, every unit is applied
and has completed the review gate with the manual pass requested.

**Repository lifecycle**

- The unit starts on its own branch, created from a freshly fetched `origin/main`.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval.
- The full per-unit review gate runs over the result. `AGENTS.md`'s "Workflow: Change lifecycle" owns the branch,
  uncommitted-work, and approval steps; the `review-gate` skill owns the review steps, including the reviewer-session
  preflight; this command points at them rather than restating them.
