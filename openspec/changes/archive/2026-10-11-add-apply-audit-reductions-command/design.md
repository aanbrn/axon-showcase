# Design

## Context

See `proposal.md` — Why. The `agents-auditor` reports reduction candidates (merge, removal, route) whose application is
editorial, and the scheduled `audit` workflow only reports. The on-demand `/audit-agents` command applies the findings
of a **live** audit run (its steps: invoke, present, apply approved, `spotlessApply`, `review-quick`), so it cannot
drain a stored report without re-running and re-deriving the audits. The trim discipline the unit applies is already
recorded — the capture bullet's "pointer, not a condensed copy" rule and its "verify a trim by sweeping every removed
line's distinctive tokens against both the trimmed text and the pointer's target" clause.

## Goals / Non-Goals

**Goals:**

- A standing, owner-gated way to apply a report's reduction candidates, so they stop ageing.
- Reuse the existing trim/pointer discipline and the per-unit review gate rather than inventing a new one.

**Non-Goals:**

- Changing the scheduled `audit` workflow or the `merge-governance` spec: application stays owner-invoked, never
  automatic (the retrospective classed this as process, not a check).
- Re-running the audits: the unit drains a stored report's candidates.
- Applying the report's other findings or its advisory and accretion classes: the unit applies the reduction candidates
  alone, and the scope Decision below states what is out of scope and where each exclusion is routed.

## Decisions

**A dedicated command, not a flag on `/audit-agents`.** A flag would conflate two units with different inputs (a live
run vs a stored report) and different cost (a pro-model audit vs an editorial pass). A separate
`/apply-audit-reductions` command is discoverable (a README row) and leaves the audit flow untouched.

**The source is the newest report, or one the owner names.** Defaulting to the newest `docs/audits/*.md` makes the unit
zero-argument; naming one handles a specific report. A single date can carry two reports (a dispatch and a scheduled
run, e.g. `2026-10-04.md` and `2026-10-04-scheduled.md`), so the unit presents both rather than guessing an ordering.

**Scope is the reduction class.** The unit applies only the findings labelled a merge, removal, or route candidate — the
ones that age in `docs/ideas.md`; the report's other findings go through the ordinary change workflow, and its advisory
and accretion classes are out of scope. The three forms differ: a **merge** applies the candidate's merged text (or
deletes the duplicate the candidate flags), a **route** reduces the rule to a pointer at the named mechanism or moves
its content into the spec or ADR that owns it, and a **removal** deletes a rule that governs no decision. A content-move
edits a spec or ADR, so the unit routes that through the change workflow (a spec edit carries its archive-time sync)
rather than editing the target inline.

**The spec home is a new requirement in `agent-skills`.** The capability's Purpose already enumerates its auditors and
their outputs; the unit that applies a report's reductions is a new agent capability there, so it is an `ADDED`
requirement (a `MODIFIED` block would have to fold it into a requirement whose subject — the auditor's own behavior —
does not cover it). _Alternative rejected:_ putting it in `merge-governance` — that spec owns the scheduled workflow's
reporting, which this change deliberately leaves unchanged.

**The unit is the reuse of the trim rule, not a new rule.** The command's edit step points at the capture bullet's
pointer/trim discipline and the trim-sweep clause, so the reduction semantics stay single-sourced.

**The command carries its own apply/review steps, mirroring `/audit-agents`.** Commands in this repo are standalone
documents rather than composed fragments, so `/apply-audit-reductions` re-expresses the short present → owner-approves →
apply → `spotlessApply` → `review-quick` sequence rather than pointing at `/audit-agents`. The duplication is the
mechanical steps only; the _rule_ the unit applies is single-sourced by reference, and the two commands' inputs differ
(a stored report vs a live run), which is what a reader needs to choose between them.

## Risks / Trade-offs

- **Overlap with `/audit-agents`' apply step.** → They differ in input (stored report vs live run); the command's
  description and the AGENTS.md sentence say so, so a reader picks the right one.
- **The unit still depends on the owner.** → That is the chosen route ("owner-gated"): it removes the ad-hoc churn, not
  the owner's decision. A future change could make the scheduled report PR carry the edits, which the parked idea also
  lists; this change does not foreclose it.
- **A pointer that drops a fact or over-claims.** → The command's step invokes the existing trim-sweep rule and the
  `review-quick` gate, the same protection the manual trim used.

## Migration Plan

None: a new owner-invoked command with no runtime or scheduled effect. The scheduled workflow is unchanged.
