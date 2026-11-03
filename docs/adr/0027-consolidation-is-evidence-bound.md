# ADR-0027: A consolidation is admitted by evidence, not by its benefit

Date: 2026-11-02

Status: Accepted

Revisit when **any** of the following is met — each alone reopens this decision for reconsideration; reopening is not
admission and authorizes no consolidation:

- **A review record shows an otherwise justified consolidation excluded by one clause of this criterion** — that clause
  may then be too strict (internal trigger: the unit's `docs/units/<unit>/rounds.tsv` rows and its archived
  `design.md`).
- **ADR-0025's `Revisit when:` clauses are met** — this criterion's scope then has to be reconciled with that ADR's,
  which may revise one of the clauses; the mechanism's own decision still governs its admission (internal trigger: that
  ADR's own list).
- **A consolidation shipped without a named adopting component is later shown necessary** — the named-adopter clause may
  then be narrowed, never the evidence clause (internal trigger: that unit's archived `design.md` and its
  `docs/units/<unit>/rounds.tsv` rows).

## Context

ADR-0025 decided what belongs to the process core and deferred four mechanisms behind their own signals. It did not say
how a consolidation that is not on that list is admitted. In practice the decision was the owner's approval, and the two
consolidation units that closed under it carried their evidence informally: the bindings unit a renaming counterfactual,
the adoption unit a 45-file sweep with its controls and a delta whose scenarios carry the preserved obligations.

No rule states a general admission condition, so a proposal could claim a reusability benefit and leave its consumer
unstated. The constraints that do exist are ADR-0025's deferrals for four named mechanisms, the premise check (does the
mechanism already exist, is its rationale recorded, and does its consumer exist), the accretion bound (the size of the
always-loaded guidance), the auditor rule (justify a new auditor by a distinct artifact or widen an existing one), the
merge rule (merge the repetition, not the evidence), the audit's consolidation check (establish what a merge would
lose), and the review gate (the unit's quality). The premise check asks for a consumer, and the merge rule and the
audit's consolidation check ask what a merge costs, but none requires that consumer to adopt the result in the same
change, and none records the adaptation the consolidation removes.

The gap is deliberately narrow. It is not a permission. ADR-0025 records a standing consolidation pause the owner
accepted on 2026-10-23 and lifted, by explicit instruction, for one unit and the first consolidation; unrelated parked
moves remain paused. This decision adds an admissibility condition and neither lifts that pause nor substitutes for it:
a consolidation still needs the owner's approval, and any move the pause covers needs the pause resolved. It is not a
promise of saving either: the repository measures transfer cost in pilots and a counterfactual in one unit, not a
recurring figure.

## Decision

A reuse-driven consolidation of the process is admissible only when its proposal and its change carry all of the
following. Admissibility is not approval: the owner's approval remains the precondition for every unit.

- **A named adopting component.** The proposal identifies a concrete coupling in a component named by path, and the
  owner it consolidates toward.
- **Adoption in the same change.** That component adopts the consolidated owner in that change; a declaration nobody
  adopts is not a consolidation.
- **Recorded evidence.** The change records a reproducible transfer, a counterfactual, or an adoption record with the
  adaptation it removes and the obligations it preserves. A sweep showing the literal gone plus a review showing the
  obligations intact is such a record.
- **The artifacts it moves.** The proposal names the artifacts and dependencies the consolidation adds, removes, or
  leaves unchanged.
- **Default to deferred.** Without that evidence the consolidation stays deferred, recorded where the idea or the ADR
  holds it, with the component it would serve and the evidence it would need.

What this does not change: the four classes, the ownership model, and the deferrals' signals in ADR-0025 and ADR-0026.

## Consequences

- The parked heading-role sweep stays parked. It would need the criterion's whole set — a named component, its adoption
  in the same change, the removed heading recorded with the obligations preserved, and the artifacts it moves accounted
  for — and any move the recorded pause covers needs that pause resolved.
- A proposal that cannot name an adopting component is deferred. Improving the guidance that already exists is one
  option the deferral leaves open; it is not the criterion's conclusion.
- The criterion overlaps the premise check, the accretion bound, the auditor rule, the merge rule, and the audit's
  consolidation check. It cites them and adds what none of them states outright — same-change adoption by the consumer,
  and the adaptation the consolidation removes.
- The evidence bar is the change's, not a second gate: the review gate still decides the unit's quality, and the
  `architecture-auditor` holds this ADR's `Revisit when:` clauses against the repository.

## Amendment — 2026-11-02

One Consequences statement is already superseded, and this amendment records it without reopening the Decision.

- "The parked heading-role sweep stays parked" describes the state when this decision was written. The owner lifted the
  pause for that unit the same day, and `adopt-heading-roles` is the first change admitted under this criterion: it
  names thirteen commands by path, adopts the roles in that change, and records what stops being component-specific with
  the obligations the review preserved. The sentence remains accurate for any consolidation not admitted this way.

## Amendment — 2026-11-03

One Context statement is qualified, and this amendment records it without reopening the Decision.

- The Context says the pause was "lifted … by explicit instruction, for one unit and the first consolidation" and that
  "unrelated parked moves remain paused". Its scope is now stated by ADR-0025's 2026-11-03 amendment, which this
  decision does not restate. This decision's criterion continues to govern every consolidation, adoption included.
