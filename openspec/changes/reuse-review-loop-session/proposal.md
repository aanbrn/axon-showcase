# Proposal

## Why

`AGENTS.md` states the reviewer's session as the caller's to reuse across rounds, but only as a permission, buried in
the instance-handover clause; the requirement that owns the loop (`showcase/quality/agent-skills`) and `review-quick`'s
contract never mention it, and the `subagent` tool starts a fresh child session unless the caller passes the prior
`sessionID`. So whether the loop reuses one is left to the caller's memory rather than stated as its behaviour: a fresh
round re-explores from scratch and cannot withdraw its own prior finding.

## What Changes

- **`showcase/quality/agent-skills`** — a `MODIFIED` delta adding an `AND` clause to the "Quick review runs after
  proposal and implementation" scenario of the "Per-change quality-gate and analysis subagents are available"
  requirement: the caller continues the same `review-quick` session across the loop's rounds (passing its session id),
  starting fresh only when that session has expired, and the reuse never substitutes for the per-round instance
  hand-off.
- **`AGENTS.md`** — the review-loop clause's reuse sentence becomes an imperative that points at the spec, rather than a
  permission.
- **`docs/ideas.md`** — the "Give the persistent-reviewer-session rejection a durable home" entry this change implements
  is removed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Quick review runs after proposal and implementation" scenario gains the loop's
  session-reuse behaviour — the caller continues the reviewer's session across rounds, starting fresh only when it has
  expired — explicit that it does not replace the per-round instance hand-off.

## Impact

- **Build / tests / services**: spec and docs only; no code, runtime, or deployment change, and no new gate.
- The loop runs the same rounds; a round after the first continues the prior reviewer conversation unless that session
  has expired, in which case it starts fresh.
