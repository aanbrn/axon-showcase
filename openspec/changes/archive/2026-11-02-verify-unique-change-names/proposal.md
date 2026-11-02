# Proposal

## Why

Change names are unique only by convention. The capture after `fix-openspec-config-rules` (#624) found its name collides
with an already-archived change (`2026-09-14-fix-openspec-config-rules`, #218), so `git log --grep` matches both and a
reader looking for "the change that fixed the config rules" finds two. The generated `openspec-propose` skill guards a
collision with an **active** change only, and the archive's date prefix hides its names from that guard.

## What Changes

- `scripts/commit-hygiene.py` gains a `--unique-change-names` mode: it strips one leading `YYYY-MM-DD-` from each
  `openspec/changes/archive/*` directory and fails when two directories carry the same change name, naming the name and
  the directories.
- The one historical collision stays as recorded and is acknowledged in the checker as that exact pair, with its reason;
  a third directory carrying the name fails.
- `build.gradle.kts` registers `verifyUniqueChangeNames`, and the standard `check` depends on it.
- `scripts/test-commit-hygiene.py`'s `UniqueChangeNameTests` carries the controls.
- The parked idea is removed from `docs/ideas.md`.

## Capabilities

### New Capabilities

none

### Modified Capabilities

`showcase/quality/commit-hygiene` gains a requirement that the standard `check` verifies archived change names are
unique, that an entry without the date prefix is not judged, and that the acknowledged historical duplicate is exempt
only as that exact pair.

## Impact

- `scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`, `build.gradle.kts`, `docs/ideas.md`, and the unit's
  process-defect record.
- `showcase/quality/commit-hygiene`'s `## Purpose` enumerates each build check the capability owns, and a delta cannot
  carry one: the archive commit adds this gate to it.
- No runtime, API, or deployment change: the gate reads directory names and fails `check`.
