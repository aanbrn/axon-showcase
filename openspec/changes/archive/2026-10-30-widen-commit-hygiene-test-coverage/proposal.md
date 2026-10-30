# Proposal

## Why

The `commit-hygiene` capability ships twelve checks — the pre-commit guard's staged-set refusals, ten build checks, and
the manual-pass readiness check — and the checker exposes each as a mode. Only the guard's classes owe a test: the
capability's coverage requirement is scoped to the guard, so any other check can ship with no test and nothing in the
corpus says otherwise — even though every mode is exercised today.

## What Changes

- The capability's coverage requirement is retitled and widened from the guard's defect classes to every check it ships,
  so a build check added without a test is a corpus gap rather than an unstated one.
- `scripts/test-commit-hygiene.py` gains the mapping the requirement needs a control for: each mode the checker exposes
  names the test class that exercises it, asserted complete against the checker's own mode list, so a mode added without
  a test fails the build.
- The implemented `docs/ideas.md` entry is removed, and the unit's process-defect record is written.

## Capabilities

### New Capabilities

none

### Modified Capabilities

`showcase/quality/commit-hygiene`: its coverage requirement is retitled from "The guard's checks are covered by tests
run in the build" to "Every check the capability ships is covered by tests run in the build", so the delta is a
`REMOVED` + `ADDED` pair — a `MODIFIED` block cannot rename a requirement's header.

## Impact

- `openspec/specs/showcase/quality/commit-hygiene/spec.md` (through the delta and the archive sync),
  `scripts/test-commit-hygiene.py`, `docs/ideas.md`, and the unit's process-defect record.
- No runtime, build, or deployment change: the gate the requirement names already runs in `check`.
