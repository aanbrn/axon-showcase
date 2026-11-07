# Unit: track-agent-session-continuity

Workspace:
`unit=track-agent-session-continuity; path=/Users/afanasyev/Development/axon-showcase; branch=track-agent-session-continuity; writer=ses_e4add21daffe0vW0OISMmtQS3i`

State: active

A change unit: its planning artifacts live under `openspec/changes/track-agent-session-continuity/`.

The owner's authorization (2026-11-07) approved the direction agreed with the technical advisor: one recorded,
reuse-by-default session for the deep review and the unit-bound advisor, replaced only on an evidenced reason with the
owner's approval, with a hand-over written before the invocation; and the continuation of those recorded sessions when a
unit resumes. It also authorized the bounded Plan-stage probe of cross-parent continuation and of the unknown-identifier
behaviour.

## Plan probe — session continuation (2026-11-07)

The owner authorized this bounded probe in the request that began the unit.

- **Same-parent continuation works.** The child `ses_e4ac50a39ffeDw7VGlTmNPWQfZ`, created by this unit's writer session
  `ses_e4add21daffe0vW0OISMmtQS3i`, was continued by that session and returned the marker `ORCHID-7391` it was given
  when created.
- **Cross-parent continuation is refused by the harness.** A new main session `ses_e4ac2e52bffeVpqxOOsnYlG75Y` (created
  with `opencode run`, the resume case) invoked `subagent` with that child's identifier and received
  `{"error":{"type":"tool.execution","message":"Session ses_e4ac50a39ffeDw7VGlTmNPWQfZ is not a child of the current session"}}`.
  No reply was produced, and the call was not retried.
- **An unknown identifier is refused on the same route, not silently created.** The same invocation with
  `ses_00000000000000000000000000` returned
  `Session ses_00000000000000000000000000 is not a child of the current session`. The silent creation `docs/ideas.md`
  records belongs to `opencode run --session`, a different route.
- **Resuming the same main session preserves its children.** Continuing `ses_e4aba0b55ffeWH1uUFdsdpdUZ6` with
  `opencode run --session` kept that identifier — every emitted event carried it — and its child
  `ses_e4ab9c8abffeTZbwD9DbRzPJSH` returned the marker `MAPLE-5522` it was given when created.

Consequence: a unit paused and resumed in a new main session cannot continue its recorded child sessions, while resuming
the same main session keeps them. The design records the forced replacement and offers the surviving route.
