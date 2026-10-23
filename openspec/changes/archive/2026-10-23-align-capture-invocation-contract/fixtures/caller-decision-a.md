# Fixture (a) — an authoring-surface diff with no candidate lesson noticed

Synthetic unit (not repository history). Run after the owner reloaded OpenCode, in a fresh session.

## Premise

- The unit edited `.opencode/agent/lesson-capture.md` (one frontmatter sentence) and nothing else.
- Review-round classes: `wording` only; no compliance miss; no problem recorded in a `captured:` rule or
  `docs/ideas.md`.
- No candidate lesson was noticed during the work.

## Observed decision

`DECISION: invoke`, `CONJUNCTION: fails (clause 3)`.

The caller quoted the deciding clause — "a diff touching no authoring surface (`.opencode/`, `AGENTS.md`,
`openspec/specs/`, `openspec/config.yaml`, a check, or a workflow)" — and recorded that "no candidate lesson noticed" is
not a skip basis, citing the failure-handling bullet ("never conclude 'nothing to capture' in its place").

## Invocation

The invocation ran and returned `nothing durable (net 0)`: no proposal passed the promotion gate, and the run's outcome
was recorded as an applied net delta of 0 rather than a skip.

## Note

The difference from the old predicate — which would have skipped for want of a noticed candidate lesson — is a
comparison of the two contracts, not an observation of a past runtime run.
