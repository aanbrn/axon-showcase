- [coherence] openspec/changes/track-agent-session-continuity/design.md:146 — D5 still says every registered invocation
  writes its section before it is awaited, which the delta's pre-mechanism exception falsifies, and task 4.5 states the
  wrong sentence for that section
- [test-control] openspec/changes/track-agent-session-continuity/specs/showcase/quality/commit-hygiene/spec.md:271-278 —
  a created row's requested identifier, a mismatch row's returned identifier, and a stalled/failed row's returned
  identifier are unconstrained and untested
- [wording] openspec/changes/track-agent-session-continuity/specs/showcase/quality/agent-skills/spec.md:216 — "fresh
  screen" is undefined and the sentence cites the recovery cases where the requirement states the rule
