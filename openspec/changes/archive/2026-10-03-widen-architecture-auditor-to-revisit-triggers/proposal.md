# Proposal

## Why

ADR-0003, ADR-0004, and ADR-0011 are explicit deferrals whose entire point is to be revisited when a stated condition is
met — ADR-0003 (adopt Jackson 3 once Axon and the OpenSearch client support it), ADR-0004 (reopen the Spring Boot 4
migration when there is capacity), ADR-0011 (migrate to Axon Framework 5 once its extension/module surface ships 5.x).
Each states its condition in prose inside its Decision, but nothing surfaces it: the ADR template has no `Revisit when:`
field, no check watches the condition, and an ADR's `Status` only records a replacement after the fact — so a deferral
silently becomes permanent until someone remembers to re-check it. A deferral is the one decision class whose whole
purpose is to be revisited, and the repository has no mechanism that reminds anyone.

## What Changes

- `docs/adr/README.md`: add a `Revisit when:` line to the ADR template, documented as required for a deferral (a
  decision that is deliberately not acted on now) and omitted for a settled decision.
- `docs/adr/0003-…`, `0004-…`, `0011-…`: add the `Revisit when:` line to the three deferrals, each naming **its own**
  gate — ADR-0011 an external coordinate gate (`axon-bom` → 5.x), ADR-0003 a third-party-adoption gate (Axon and the
  OpenSearch client resolving Jackson 3), ADR-0004 an internal capacity trigger.
- `.opencode/agent/architecture-auditor.md`: widen the advisory "unexplained design intent" sweep so a deferred ADR
  whose recorded `Revisit when:` condition appears met is reported — holding the condition against the repository where
  it names a checkable fact (an external coordinate, or a consumed artifact's adoption), and **not** reporting a
  condition that names no such fact (a capacity trigger) — as an advisory item naming the ADR, the condition, and the
  signal, not as a defect.
- `.opencode/commands/audit-architecture.md`: reflect the widened sweep in the command's read-list/description.
- `AGENTS.md` and `README.md`: refresh the architecture-auditor descriptions to name the new check.
- `openspec/specs/showcase/quality/agent-skills` delta: extend the "Unexplained design intent is surfaced for
  clarification" requirement to include the deferred-decision condition, and record the capability's `## Purpose`
  refresh for the archive commit.
- Remove the implemented idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Unexplained design intent is surfaced for clarification" requirement gains the
  deferred-decision-class sweep (an ADR's recorded `Revisit when:` condition, held against the repository), and its
  advisory shape.

## Impact

- **Files**: `docs/adr/README.md` and the three deferral ADRs; `.opencode/agent/architecture-auditor.md`;
  `.opencode/commands/audit-architecture.md`; `AGENTS.md`; `README.md`; `docs/ideas.md`; plus the change dir and the
  `agent-skills` delta.
- **Build / tests / services**: no code or build change; the ADR template and the auditor are documentation/tooling.
- **Verification**: the widened auditor's new branch is exercised by a smoke-run (a positive control — a deferral whose
  condition appears met — and a negative control — a deferral whose condition is not met, or a settled ADR — which it
  must not report), and the template/ADR edits are grepped.
