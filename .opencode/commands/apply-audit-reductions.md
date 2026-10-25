---
description:
  Apply an audit report's reduction candidates (merge/removal/route/extraction) — merged text, pointers, and deletions
  inline, a content-move routed to the change workflow — under the full review gate
---

Apply the reduction candidates an audit report names, so they do not age. This is the standing, owner-gated unit for a
report's reductions; it never runs automatically, and the scheduled `audit` workflow only reports.

**Trigger** — the owner invokes `/apply-audit-reductions`, naming a report or accepting the newest one.

**Prerequisites** — none beyond the repository; the report is under `docs/audits/`.

**Procedure**

1. Identify the source report: the newest under `docs/audits/` by date, or the one the owner names. IF a date carries
   more than one report (a dispatch and a scheduled run, e.g. `2026-10-04.md` and `2026-10-04-scheduled.md`), THEN
   present both and ask which — do not guess an ordering.
2. Read that report's **reduction candidates** — its findings labelled a merge, removal, route, or extraction candidate
   — and the parked reduction entries in `docs/ideas.md` they correspond to. The report's other findings go through the
   normal change workflow, and its advisory, accretion, and authoring-system-gap classes are out of scope.
3. Present each candidate with its suggested rewrite or target, and ask the owner which to apply.
4. Apply the approved ones, following the candidate's own suggested edit and `AGENTS.md`'s capture-bullet pointer/trim
   rule (referenced, not restated here):
   - **merge**: apply the candidate's merged text, or delete the duplicate it flags.
   - **route**: reduce the rule to a pointer at the target the candidate names. Where the candidate instead **moves**
     the rule's content into the spec or ADR that owns it, route that through the normal change workflow (a spec edit
     carries its archive-time sync) — do not edit a spec or ADR inline.
   - **removal**: delete the rule the candidate names, recording what the deletion loses and whether git preserves it.
   - **extraction**: route the move through the normal change workflow rather than editing the destination inline, since
     it creates or changes a destination the change owns; the change follows `AGENTS.md`'s "Workflow: Extract an
     `AGENTS.md` section" procedure.
5. Remove the applied entries from `docs/ideas.md`.

**Decision points**

- IF the report carries no reduction candidate, or the owner approves none, THEN report that and stop — create no unit.
- IF the approved candidates differ in delivery route or in scope, THEN split them into separate units, each scoped to
  its own artifacts and diff.
- An **extraction opportunity** is not a candidate: never present or apply one, since it is a design recommendation the
  owner carries out in a dedicated change.
- A candidate whose suggested edit is not reproducible against the artifact it names is reported as a claim to verify,
  not applied on the report's word.

**Failure handling** — a candidate whose target no longer exists is reported rather than forced; the parked entry it
corresponds to is left in place.

**Verification** — `./gradlew spotlessApply` for the formatter-wrapped files the unit edited, then the review gate's own
checks.

**Completion criteria** — the owner has the candidates and has chosen; or, for an approved selection, every unit is
applied and has completed the review gate with the manual pass requested.

**Repository lifecycle**

- A new unit starts on its own branch, created from a freshly fetched `origin/main`; a unit continuing an existing
  branch or pull request works on that branch.
- The unit's working tree — the primary checkout when no other unit holds it, otherwise the sibling worktree
  `../axon-showcase-<unit>` — is established and recorded before the first write, with an ownership it cannot establish
  treated as occupied, per `AGENTS.md`'s "Workflow: Change lifecycle" **Workspace** rule.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval.
- The full per-unit review gate runs over the result — not the formatter and a quick review alone. `AGENTS.md`'s
  "Workflow: Change lifecycle" owns the branch, uncommitted-work, and approval steps; the `review-gate` skill owns the
  review steps, including the reviewer-session preflight; this command points at them rather than restating them.
