# Proposal

## Why

The review gate requires the main agent to continue a reviewer's session across `review-quick` rounds, but the
requirement is a decision bullet rather than a step the agent runs immediately before a repeat invocation, so it is easy
to miss. The record cannot show whether it was followed: `docs/review-rounds.tsv` records each round's result and not
the session it ran in, so a loop that started a fresh session every round is indistinguishable from one that continued,
and nothing fails when a session was restarted for no reason. The omission surfaces only as the loop's re-derivation
cost, which is the caller's account rather than a fact the repository evidences.

## What Changes

- `AGENTS.md` — the review-gate procedure gains an explicit **reviewer-session preflight** immediately before each
  `review-quick` invocation, and the session decision bullet becomes a pointer to the rule the capability spec owns
  rather than a second statement of it.
- `.opencode/agent/review-quick.md` — the report contract names the fields the caller supplies when it writes the ledger
  row, and forbids inventing a session identifier.
- `docs/review-rounds.tsv` — the header declares three new fields (the reviewer's session identifier, its transition,
  and a replacement's reason), a boundary comment marks the first row that carries them, and this change's own rows
  carry them.
- `scripts/commit-hygiene.py` — a `--session-continuity` mode validates the ledger's session fields against the
  continuity rule.
- `scripts/test-commit-hygiene.py` — the focused tests for that mode: a known-bad case per rejected shape, and a
  known-good case per accepted behaviour.
- `build.gradle.kts` — the mode runs as the `verifyReviewSessionContinuity` task, a member of `check`.
- `docs/ideas.md` — the parked idea this change implements is removed, and the parked test-coverage list gains the new
  verification, since that list enumerates the checks whose spec requirement carries no coverage clause.
- `README.md` — its check list and its command list name the new verification.

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/agent-skills` — the quick-review scenario's session-continuity clause becomes a preflight that names
  the action and verifies the continuation, a sibling clause requires the returned identifier to be compared with the
  saved one, and the record-line clause gains the session fields.
- `showcase/quality/commit-hygiene` — a new requirement verifies the ledger's reviewer-session continuity.

## Impact

The change touches no production code. It changes the review-round record's contract, the checker that validates it, the
`check` task's membership, and the three documents that enumerate those members — so a ledger row written without its
session fields fails the pull-request gate rather than passing silently. The three new fields are appended, so the
digest's figures and the routing tool, which read the first six by index, are unaffected; the digest's echoed per-row
lines do carry the new fields.
