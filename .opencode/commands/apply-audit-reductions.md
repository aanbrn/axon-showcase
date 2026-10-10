---
description:
  Apply an audit report's reduction candidates (merge/removal/route) as merged text, pointers, or deletions, under the
  review gate
---

Apply the reduction candidates an audit report names, so they do not age. This is the standing, owner-gated unit for a
report's reductions; it never runs automatically.

1. Identify the source report: the newest under `docs/audits/` by date, or the one the caller names. When a date carries
   more than one report (a dispatch and a scheduled run, e.g. `2026-10-04.md` and `2026-10-04-scheduled.md`), present
   both and ask which — do not guess an ordering.
2. Read that report's **reduction candidates** — its findings labelled a merge, removal, or route candidate — and the
   parked reduction entries in `docs/ideas.md` they correspond to. The report's other findings go through the normal
   change workflow, and its advisory and accretion classes are out of scope.
3. Present each candidate with its suggested rewrite or target, and ask the user which to apply — do not edit files
   without their go-ahead.
4. Apply the approved ones, following the candidate's own suggested edit and `AGENTS.md`'s capture-bullet pointer/trim
   rule (referenced, not restated here):
   - **merge**: apply the candidate's merged text, or delete the duplicate it flags.
   - **route**: reduce the rule to a pointer at the target the candidate names. Where the candidate instead **moves**
     the rule's content into the spec or ADR that owns it, route that through the normal change workflow (a spec edit
     carries its archive-time sync) — do not edit a spec or ADR inline.
   - **removal**: delete the rule the candidate names, recording what the deletion loses and whether git preserves it.
5. Remove the applied entries from `docs/ideas.md`.
6. Run `./gradlew spotlessApply` for the formatter-wrapped files the unit edited.
7. Run the `review-quick` subagent over the resulting diff, repeating until clean, then ask for the manual review before
   the change ships.

The scheduled `audit` workflow only reports; it never applies. This unit runs only when the owner invokes it.
