---
description:
  Audit the repository's human-facing README for accuracy against the repository, fidelity to its documented design
  intent, grammatical correctness of its prose, and coverage of the human-visible capabilities the system offers, with
  the pro-model readme-auditor subagent
---

Run the README audit, which reports only — it never edits a file.

**Trigger** — the owner invokes `/audit-readme`. It is deliberately absent from the scheduled `audit` workflow.

**Procedure**

1. Invoke the `readme-auditor` subagent (`.opencode/agent/readme-auditor.md`) on the repository. Its definition owns the
   four axes it audits, the evidence each finding rests on, and its report contract.
2. Present its report in the contract it returns — the verdict line first, then its findings grouped by axis and its
   advisory section, each item as the shared report contract requires. Do not re-summarize it.
3. Ask the owner which findings to apply.

**Decision points**

- IF the report carries nothing actionable, or the owner approves nothing, THEN report that and stop — create no unit.
- IF the approved findings differ in delivery route or in scope, THEN split them into separate units, each scoped to its
  own artifacts and diff.
- An advisory item — a wording preference, a structure, or an unclear intent, for the owner's judgment — is never
  "fixed" automatically. A provable language error is a finding, not an advisory item, and its suggested rewrite is
  applied only through the unit process the definition's boundary describes.
- A finding the README's own convention records as deliberate (its documented section order, the OpenSpec-flow diagram's
  asymmetry) is not a defect.
- A correction that changes the README's _design intent_ rather than its accuracy needs the owner's decision, since the
  README convention owns that intent.

**Applying approved findings**

- Report generation and applying are separate: the applying is a unit of its own.
- `README.md` alone is a docs unit under `AGENTS.md`'s "Workflow: Change lifecycle". A finding that also needs a spec or
  code change splits into its own unit by that route, and never rides the docs unit.
- Run the full per-unit review gate over the result. The `review-gate` skill owns its steps, including the
  reviewer-session preflight; this command points at it rather than restating it.

**Verification** — `./gradlew spotlessApply` for the formatter-wrapped markdown the unit edited.

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
