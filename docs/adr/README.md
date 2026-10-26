# Architecture Decision Records

This directory records the project's architecture decisions as lightweight, numbered ADRs using the Nygard format. They
capture the **context, decision, and consequences** behind cross-cutting choices — the _why_ that specs and code do not
express. OpenSpec specs capture what the system does; ADRs capture why it is shaped that way.

## Naming

Numbered files: `NNNN-kebab-case-title.md`. Never renumber; a superseding ADR points at the one it replaces.

## Template

```markdown
# ADR-NNNN: Title

Date: YYYY-MM-DD

Status: Proposed | Accepted | Superseded by ADR-NNNN

Revisit when: <whether any one clause reopens the decision or only all of them together, then one clause per line — each
naming its artifact at its current coordinate, labelling its type (external coordinate vs internal trigger), and stating
the signal that shows the clause met>

## Context

The situation that prompted the decision — the problem, constraints, and forces in play.

## Decision

What was decided, and the alternatives considered and rejected.

## Consequences

What becomes easier or harder, what this enables and what it forecloses. Update this section if the trade-offs change.
```

## Writing a new ADR

- Copy the template, pick the next `NNNN`, and fill it in.
- One decision per ADR. Keep it short; the decision and its rationale matter, not ceremony.
- Record the decision at the time it is made — do not leave decisions implicit in commit history.
- The `Revisit when:` field is **required for a deferral** — a decision deliberately not acted on now, to be revisited
  when a stated condition is met — and omitted for a settled decision. Give each clause its own line, naming its
  artifact at its current coordinate and labelling its type (external coordinate vs internal trigger — a newer major
  release and a third-party adoption are the external kind), and stating the signal that shows the clause met; then
  state the clauses' relation — a conjunction fires only when every clause is met, a disjunction when any one is.
- A compound gate states that relation because the `architecture-auditor` reads it literally: an unstated relation makes
  it guess, and a clause bundling two events delays the reopen when either alone would have satisfied it.
