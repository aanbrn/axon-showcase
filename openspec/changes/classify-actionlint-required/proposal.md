# Proposal

## Why

`check` runs `workflowLint`, which invokes `actionlint`, and both `AGENTS.md` and the README name it as a prerequisite
of the default build-and-test path — but `scripts/doctor.sh` declares its probe `optional`, so a machine without it is
diagnosed as fit and then fails the first `check`. The spec's own definition settles which class is right: required
means needed to run and verify the application, optional covers deployment, security scanning, and the agentic workflow.

## What Changes

- `scripts/doctor.sh` declares `actionlint` as a required probe.
- `scripts/test-doctor.sh`'s expected required set gains `actionlint`, so the set-equality control asserts the class
  against the default build-and-test path rather than against itself.
- The `showcase/quality/toolchain-check` delta widens the two scenarios that enumerate the required prerequisites — the
  required-tool scenario and the repo-state requirement's — to name `actionlint`.
- The parked idea is removed from `docs/ideas.md`.

## Capabilities

### New Capabilities

none

### Modified Capabilities

`showcase/quality/toolchain-check` gains `actionlint` in the required set: an absent or unsatisfied `actionlint` fails
the default diagnosis, and the two scenarios that name the required prerequisites list it.

## Impact

- `scripts/doctor.sh`, `scripts/test-doctor.sh`, `docs/ideas.md`, and the unit's process-defect record.
- `README.md` and `AGENTS.md` already state that `actionlint` is needed for the default build-and-test path, so this
  change makes the declaration agree with them rather than editing either.
- No runtime, API, or build change: `check` already runs `workflowLint`, and `testDoctorCharter` already runs in
  `check`.
