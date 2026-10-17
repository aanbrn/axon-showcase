# Design

## Context

See `proposal.md` — Why. The loop's behaviour is owned by `showcase/quality/agent-skills`'s "Quick review runs after
proposal and implementation" scenario; the caller-side discipline lives in `AGENTS.md`'s review-loop clause; the
reviewer's own contract is `.opencode/agent/review-quick.md`. The session decision is spread across `AGENTS.md` (a
permission) and `docs/ideas.md` (the 2026-10-16 "durable home" entry and the 2026-10-15 "rejected levers" entry). The
parked idea prescribes the shape: promote the decision into the capability spec that owns the loop. #604
(`clarify-review-loop-session-owner-2026-10-16`) already made the `AGENTS.md` wording match practice and qualified the
rejected lever to _a persistent session as a substitute for the hand-off_.

## Goals / Non-Goals

**Goals:**

- Make reuse the loop's stated behaviour — an action the caller takes — rather than a permission it may overlook, since
  the `subagent` tool starts a fresh child session unless the caller passes the prior `sessionID`.
- Keep the existing rejection alongside it: reuse is never a substitute for the per-round instance hand-off.

**Non-Goals:**

- No observable: the round record line's fields stay as they are. A session-count field is one of the levers
  `docs/ideas.md` records as contradicted by the record, and a caller-emitted field would be a claim the reviewer cannot
  verify.
- No change to `review-quick`'s contract or to the commands that drive the loop — the spec owns the behaviour.
- No fixed iteration cap, no diff-scoped rounds (the other rejected levers).

## Decisions

**Promote the decision as an `AND` clause on the existing scenario, not a new requirement or scenario.** The parked idea
names this capability and a `MODIFIED` delta, and the behaviour belongs to the loop the scenario already describes,
beside the instance-hand-off clause it qualifies. A new requirement would accrete a permanent corpus entry for a
refinement of an existing one; a new scenario would split one behaviour across two. The delta copies the requirement's
full block (its description and all nine scenarios, in the main spec's order) so no scenario is dropped or renamed.

**State the reuse as the caller's action, with its exception.** The failure being fixed is that a permission in the
always-loaded file does not produce the behaviour — the tool defaults to fresh — so the clause names what the caller
does (`continues the prior round's session — passing its session id`) and the only exception
(`fresh only when that session has expired`). Alternative rejected: keep the permission and rely on the caller — the
status quo, in which the caller may overlook it.

**`AGENTS.md` keeps the imperative and points at the spec.** Per the capture pointer rule, the rationale moves to the
spec (loaded when its capability is worked on) while `AGENTS.md` keeps what a reader must act on. Alternative rejected:
restate the rationale in both, which re-anchors a copy that drifts.

## Risks / Trade-offs

- **A spec clause is not a gate.** Every clause in this scenario is review-verified, not gated; this one is no weaker,
  and it states a concrete caller action a review can ask for.
- **Reuse can anchor the reviewer to its own prior finding.** The existing hand-off and class-exhaustion clauses already
  forbid leaning on the reviewer's memory, and the new clause repeats that reuse never substitutes for the hand-off.
- **A session may expire mid-loop.** The clause permits a fresh session on expiry, so the loop cannot stall on it.

## Claims table

| Claim                                                                                        | Source                                                                                                                                           |
| -------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| The loop is owned by this scenario                                                           | `showcase/quality/agent-skills`'s "Quick review runs after proposal and implementation" (`openspec/specs/showcase/quality/agent-skills/spec.md`) |
| The loop's spec and `review-quick`'s contract never state the reuse                          | grep `reuse`/`session` over that spec and `.opencode/agent/review-quick.md` returns no session-reuse hit                                         |
| The `subagent` tool starts fresh unless the prior `sessionID` is passed                      | the `subagent` tool's own description                                                                                                            |
| `AGENTS.md` recorded the decision as a permission before this change                         | `AGENTS.md`'s review-loop clause at the change's base (`git show HEAD:AGENTS.md`), which #604 wrote and this change reworks                      |
| The parked idea prescribes this shape                                                        | `docs/ideas.md`'s "Give the persistent-reviewer-session rejection a durable home" entry at the change's base (`git show HEAD:docs/ideas.md`)     |
| The reuse is not to substitute for the hand-off                                              | `docs/ideas.md`'s 2026-10-15 "rejected levers" entry, qualified by #604                                                                          |
| The other rejected levers (a session-count field, a fixed iteration cap, diff-scoped rounds) | `docs/ideas.md`'s 2026-10-15 "rejected levers" entry                                                                                             |
