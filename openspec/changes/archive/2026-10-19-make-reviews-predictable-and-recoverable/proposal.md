# Proposal

## Why

A quick-review round has no bound on its duration and no recovery when it stops responding. Its contract calls it a
bounded screen, but the screen's inputs are described rather than fixed, so the pass can keep widening what it reads;
and when an invocation stalls, the loop has no observable protocol — `stopped-responding` is a permitted ledger reason
with no stated decision behind it, so the caller neither knows when to declare it nor what to carry to a replacement.
The stall is not hypothetical: a round of this repository's own loop ran long enough that the owner asked whether it was
still working. The established capability makes the loss worse than it looks: the `subagent` invocation exposes no
timeout and no cancellation, so a stalled reviewer cannot be stopped — the caller can only stop waiting, and whatever
the stalled session had not yet reported is unrecoverable.

## What Changes

- `showcase/quality/agent-skills` (delta) — one `MODIFIED` requirement: the quick screen's inputs become a
  caller-supplied list the report names, the caller's risk focuses widen attention within that list rather than adding
  surfaces, a risk the scope cannot settle escalates to `review-thorough` instead of widening the pass, both reviewers
  gain an explicit stop rule, and a new scenario states the caller-side watchdog: the deadline, the handover recorded
  **before** waiting, the `stopped-responding` record, the replacement, and the verifiable facts the record may keep.
- `.opencode/agent/review-quick.md` — the three layers (the bounded screen over the supplied list, the risk focuses, the
  escalation), the stop rule, and the handover fields it is given.
- `.opencode/agent/review-thorough.md` — the stop rule: the change's artifacts and its own contract's checks, a
  limitation rather than unbounded investigation.
- `AGENTS.md` — the review gate's invocation step carries the watchdog protocol and the handover fields, and the
  session-continuity bullet points at the spec's rule rather than restating it.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the per-change quality-gate subagents requirement, which owns the quick screen, the
  routing decision, the reviewer sessions, and the loop's records.

## Impact

- **Build / tests / services**: no code, build, dependency, deployment, or runtime change. The change edits agent
  definitions and prose, plus a delta spec.
- **Not changed, deliberately**: the mandatory quick-review gate, the finding-evidence rules, the pre-write derivation
  pass, the manual review, the routing decision, the two-pass deep-review budget, and the ledger's schema and its
  continuity check. The delta preserves each; the ledger gains no field, because a duration or token count is not
  reproducible from the repository.
- **Verification**: `openspec validate --all` and `--changes` (which checks that the `MODIFIED` block preserves every
  existing scenario), the scenario and clause preservation diff,
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, and the agent-loader probe the `build` gate runs for
  `.opencode/` changes.
