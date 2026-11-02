# Proposal

## Why

`AGENTS.md` requires a unit's `Workspace:` record before its first write, and `pause-resume-unit` reads it to confirm
the tree and to hand the writer role over — but nothing verifies that it exists. An archived change's `tasks.md` carried
no such line at all, and another unit recorded one only when a review round asked for it. The record is the unit's own
evidence of which tree, branch, and session it belonged to, so a missing or wrong record is silent to every gate.

## What Changes

- `scripts/commit-hygiene.py` gains a `--workspace-records` mode: it reads each live `openspec/changes/<name>/tasks.md`
  and each archive dated on or after the gate's introduction, and fails when the `Workspace:` record is absent,
  malformed, or names a unit other than the directory's own.
- The pre-commit guard applies the same judgment to a staged `tasks.md` — live or archived — and additionally requires
  the record's `path` and `branch` to name the tree and branch being committed, which `check` cannot assert because CI
  checks out a detached head in a runner directory.
- `build.gradle.kts` registers `verifyWorkspaceRecords`, and the standard `check` depends on it.
- `scripts/test-commit-hygiene.py`'s `WorkspaceRecordTests` carries the controls.
- The parked idea is removed from `docs/ideas.md`.

## Capabilities

### New Capabilities

none

### Modified Capabilities

`showcase/quality/commit-hygiene` gains a requirement that a change unit's `tasks.md` records its workspace: the
standard `check` verifies every live change directory and every archive dated on or after the gate's introduction, and
the pre-commit guard refuses a staged one that fails the same judgment.

## Impact

- `scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`, `build.gradle.kts`, `docs/ideas.md`, and the unit's
  process-defect record.
- The archive's history is not re-judged: 56 of its change directories carry a record because the rule is newer than
  most of them. An archive dated on or after the gate's introduction is judged, so a unit whose only push carries the
  archive commit is still covered.
- No runtime, API, or deployment change: the verification reads one file per live change directory and per archive dated
  on or after the gate's introduction, and fails `check`.
