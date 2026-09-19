# Design — add a README auditor

## Context

`README.md` is the repository's human-facing showcase and onboarding guide. Its markdown formatting is gated by
Spotless, but its _content_ is not: the review convention has repeatedly relied on a human or a review pass to catch a
miscount, a stale tally, or a missing capability, and the two conventions that govern the README's shape (README design
intent, Surface human-visible capabilities) have no enforcement. The parked idea in `docs/ideas.md` scoped a
`readme-auditor` subagent for this, on the same pattern as the three existing auditors. See `proposal.md` in this change
directory.

## Goals / Non-Goals

**Goals**: a `readme-auditor` subagent and `/audit-readme` trigger; three verifiable audit axes; an advisory treatment
of subjective quality; the spec requirement, `AGENTS.md` and README rows, and the idea removal.

**Non-Goals**: fixing the README's content (the auditor reports, the main agent applies what the user approves);
auditing any other document (ADRs, retrospectives, `docs/`, the spec corpus are out); reproducibility of prose taste.

## Decisions

### D1 — A new auditor, not a widening of `agents-auditor`

The auditor-justification rule (`AGENTS.md`) requires naming the drift and which existing auditor cannot see it. The
README's drift is _not_ visible to `agents-auditor`: its scope is `AGENTS.md` and the project-authored `.opencode/`
files, and it does not hold the README's facts against the repository. The parked idea reached the same conclusion on a
second ground — a distinct artifact and audience (humans, not agents) — which is the same test that keeps
`specs-auditor` separate. Widen-vs-add resolves to add.

### D2 — Three axes, and prose taste is advisory

The idea named them: accuracy/consistency, design-intent fidelity, coverage/experience surfacing. The important boundary
is that **subjective quality is advisory, never a finding**: the README is hand-curated by design ("preserve its
intended shape on every edit"), so a redundancy or a phrasing preference is the user's call, not a defect. Making it a
finding would let the auditor relitigate a deliberate edit, which is the failure mode the other auditors' "respect
deliberate choices" rule already guards against.

### D3 — Scope is `README.md` only

The idea is explicit, and it keeps the auditor's evidence small and its output credible. Other docs have their own
owners (`specs-auditor` the corpus, `agents-auditor` the guidance, `architecture-auditor` the ADRs), so widening here
would duplicate them rather than add coverage.

### D4 — The reports use the shared report contract

The `agent-skills` spec's shared report contract already binds the review and lesson-capture agents and the three
auditors; the README auditor joins it, so its verdict line and budgeted items match the others and the main agent's
apply step is uniform.

## Risks / Trade-offs

- **An inaccuracy in the auditor's own README knowledge would be reported as a finding.** Mitigation: the definition
  requires each finding to be verified against the repository (the same instruction the other auditors carry), and the
  smoke-run before archiving is the control.
- **Coverage/experience surfacing is the softest axis** — "every human-visible capability" needs a source of truth the
  auditor can read. The system's own surfaces (the gateway's routes, the saga, the SSE stream, the Helm values) and the
  README's own convention give it one; the smoke-run with a seeded, deliberately-undocumented capability is the positive
  control.
