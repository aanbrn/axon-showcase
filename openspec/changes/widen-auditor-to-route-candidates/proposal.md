# Proposal

## Why

The tooling audit's **removal candidates** cover only a rule that governs no decision. But the capture-side promotion
gate already keeps a rule that _is_ enforced elsewhere out of `AGENTS.md`, routing it to a check, a spec, or an ADR —
and the audit never re-checks that. So a rule that predates the gate, or whose subject a later change made enforceable,
sits in the always-loaded file duplicating a mechanism that already catches it, and the growth bound has no lever for
it. The natural but unsafe extension — treating "easy to find and fix on review" as a removal criterion — must be
excluded: the rule is often what makes the reviewer look, and review is not a gate here.

## What Changes

- **Route candidates are a standing analysis.** The audit reports rules whose subject is enforced by a named, in-place
  deterministic mechanism — a build or CI gate, a lint or test, a CLI validation, or a deterministic workflow step such
  as the archive-time spec sync — as candidates to reduce the rule to a pointer at that mechanism (or move its content
  to the spec or ADR that owns it), naming the mechanism and what would be lost. A candidate, not an action; the owner
  decides.
- **The verdict line names the third count**, alongside merge and removal, so a run that found a route candidate is
  distinguishable from one that found none.
- **Review-detectability is stated as _not_ a criterion.** A rule whose only detector is a review — a human's or a
  review subagent's — is not a removal or route candidate: review is not a gate, and the rule may itself be what makes
  the violation visible to the reviewer.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the agent-tooling audit's conciseness analysis gains the route-candidate analysis and
  its deterministic-mechanism test, and bounds out review-detectability.

## Impact

- `.opencode/agent/agents-auditor.md` — the conciseness axis gains the route analysis and the review bound; the finding
  labels and the report-contract verdict line name `route`.
- `.opencode/commands/audit-agents.md` — the output description names the third count.
- `AGENTS.md` — the agents-auditor bullet's finding-class parenthetical names route candidates.
- `README.md` — the agent-table row, the `/audit-agents` row, and the auditor prose name route candidates.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — the tooling-audit requirement is extended (a delta); the
  `## Purpose` is refreshed in the archive commit.
- No code, build, runtime, or deployment change.
