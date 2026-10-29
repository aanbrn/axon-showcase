# Proposal

## Why

The pre-write pass's claims table is seeded by `scripts/claim-scan.py`, and its `absolute` class misses two shapes the
loop keeps recording: the absence forms `no` and `nothing` (only `none` matches today), and the definite singular
(`the one`, `the sole`, `the single`). Four recorded instances slipped through — "nothing is parked for it", "no finding
reduced `AGENTS.md`", "Nothing in the repository reads `.java-version`", and "`git clean -fd` is the one loss vector" —
each needing a claims-table row the scan never surfaced.

## What Changes

- `scripts/claim-scan.py`'s `ABSOLUTE` gains `no`, `nothing` and the definite singular outright, so an absence claim
  surfaces as a candidate.
- `scripts/test-claim-scan.py` gains one control per added form beside the existing absolute case.
- The noise the widened class adds is measured over `AGENTS.md` and recorded: its share of matching lines rises from 16%
  to 22%, a measure of incidence rather than of false positives.
- The implemented `docs/ideas.md` entry is removed, and the unit's process-defect record is written.

## Capabilities

### New Capabilities

none

### Modified Capabilities

none — no requirement covers the scan's candidate kinds: the `agent-skills` corpus names the claims table and its seed
but not the classes the tool prints, and `/seed-claims` tells a reader to run the tool rather than restating its kinds.
The grep that establishes this is `grep -rn "absolute" openspec/specs/`: its six hits are the load-tests specification's
absolute thresholds, and `grep -rn "candidate kind" openspec/specs/` returns nothing.

## Impact

- `scripts/claim-scan.py`, `scripts/test-claim-scan.py`, `docs/ideas.md`, and the unit's process-defect record.
- No build, deployment, or API change; the scan stays a lead list and never fails on a candidate.
