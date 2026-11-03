# Proposal

## Why

A unit's process-defect records are already per unit, but they sit in a shared directory beside the unit's other
artifacts: its record, rounds, findings, and hand-over are under `docs/units/<unit>/`, while its defect history is
`docs/process-defects/<unit>.md`. Reading one unit's history means knowing two conventions and visiting two directories,
and the parked idea reopens exactly on the owner's decision that one physical dossier is itself the required outcome.

## What Changes

- All 111 records move to `docs/units/<unit>/process-defects.md`, the unit name taken from the file's current basename,
  so a date or suffix inside a unit's name stays part of that name.
- The capture workflow names the new path; the bindings' `process-defect-records` role value and its consumer trace
  follow.
- `scripts/review-routing.py`'s record-artifact class and `scripts/experience-analysis.sh`'s git-log filters, with their
  tests, recognize the new path.
- `AGENTS.md`, the pull-request template, the README, ADR-0028 (a dated amendment), `openspec/config.yaml`, and the
  `agent-skills` requirements that name the directory are retargeted.
- The parked idea is removed.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/agent-skills`: the process-defect record lives at `docs/units/<unit>/process-defects.md`, and the
  routing's record-artifact class includes it there.

## Impact

- Data: 111 file moves into `docs/units/<unit>/`, and one directory retired.
- Scripts: `scripts/review-routing.py`, `scripts/experience-analysis.sh`, and their two test suites.
- Docs: `AGENTS.md`, `.github/PULL_REQUEST_TEMPLATE.md`, `README.md`, `docs/contracts/process-bindings.md`, ADR-0028 (a
  dated `## Amendment` pointing at the Decision's role bullet), `openspec/config.yaml`, and `docs/ideas.md` (the parked
  entry removed and its two citations retargeted).
- No runtime, API, or deployment change.
