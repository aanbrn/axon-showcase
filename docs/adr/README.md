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

Revisit when: <the condition that would reopen this decision, and the signal that shows it met>

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
- The `Revisit when:` line is **required for a deferral** — a decision deliberately not acted on now, to be revisited
  when a stated condition is met — and omitted for a settled decision. Name the condition and the signal that shows it
  met (an external coordinate that would publish a newer major, a third-party adoption, or an internal trigger), so the
  `architecture-auditor` can surface it once due.
