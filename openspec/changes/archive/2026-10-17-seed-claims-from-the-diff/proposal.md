# Proposal

## Why

The pre-write derivation pass's weakest step is its claims table: the rows are author-seeded, so a fact the author never
noticed stating cannot become a row, and the review loop then finds it one round at a time. The record makes that the
largest grouped cluster — the ten claim classes amount to 175 of the 542 class occurrences in `docs/review-rounds.tsv`
(32%), against 54 for the largest single class — so the pass spends rounds on what a scan of its own diff could have
listed.

## What Changes

- **`scripts/claim-scan.py`** — a tool that scans an authored diff's added lines for the claim shapes the loop keeps
  catching and prints one candidate per line — its location, kind, and text — for the author to resolve into a
  claims-table row or record as a dismissal.
- **`scripts/test-claim-scan.py`** — a case per shape over unified-diff fixtures, plus the known-positive control.
- **`build.gradle.kts`** — a `testClaimScan` `Exec` task in `check`, mirroring `testCommitHygiene`.
- **`.opencode/commands/seed-claims.md`** — the trigger command, since a `scripts/` tool an agent drives gets one.
- **`.opencode/agent/review-quick.md`** — the reviewer's claims-table check gains the scan candidates: one that is
  neither a row nor a listed dismissal is a finding.
- **`showcase/quality/agent-skills`** — a `MODIFIED` delta: the pre-write pass's claims-table clause gains the scan,
  whose candidates the author resolves into rows or records as dismissals (with the reason), the reviewer's check, and
  the loop's session clause admits a fresh session when the reviewer's definition changed mid-loop.
- **`AGENTS.md`** — the pre-write bullet gains the scan's actionable form (pointing at the spec for the behaviour), the
  `check` members list gains `testClaimScan`, the agent-driven-script enumeration gains `claim-scan.py` ↔
  `/seed-claims`, the Python prerequisite names the tool, and the review-loop clause's session sentence admits the
  definition-change restart.
- **`README.md`** — the Slash Commands table gains `/seed-claims`, and the Python prerequisite row names the tool.
- **`docs/ideas.md`** — the entry this change implements is removed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the pre-write derivation pass's claims-table clause gains the mechanical diff scan,
  whose candidates the author resolves into rows or records as dismissals.

## Impact

- **Build / tests / services**: one new `scripts/` tool plus its test task in `check`; no service, runtime, or
  deployment change.
- The pre-write pass gains an authoring aid; the review loop's claim-shaped rounds should drop.
