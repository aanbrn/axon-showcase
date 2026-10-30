# Proposal

## Why

Removing a dated section's last entry from `docs/ideas.md` deletes the entry but not its heading: the previous change
(`b4b331f8`, #758) removed the "first-run resource passport" it implemented and left an empty `## 2026-10-29` section on
`main`. The file's header groups ideas under dated sections and no gate reads that shape, so the residue shipped and the
owner found it afterwards. Any implementing change or `/ideas cleanup` pass can reach the same state.

## What Changes

- `docs/ideas.md`'s header states the rule: removing a section's last entry removes the heading.
- `scripts/commit-hygiene.py` gains an `--ideas-sections` mode that fails on a dated `## YYYY-MM-DD` section in
  `docs/ideas.md` carrying no `- ` entry, naming its line and heading; a file with no dated section passes.
- `.opencode/commands/ideas.md` states the same where a `drop` disposition is chosen, pointing at the header rule and
  the gate.
- `build.gradle.kts` registers `verifyIdeasSections`, and the standard `check` depends on it.
- `scripts/test-commit-hygiene.py`'s `IdeaSectionTests` carries the controls; the class is their census.

## Capabilities

### New Capabilities

none

### Modified Capabilities

`showcase/quality/commit-hygiene` gains a requirement that the standard `check` verifies every dated ideas section
carries an entry. Each gate that capability already owns has its own requirement (tracked-file hygiene, conflict
markers, executable bits, cron collisions, large files, table rows), so a new gate adds one rather than folding into
another.

## Impact

- `scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`, `build.gradle.kts`, `docs/ideas.md`,
  `.opencode/commands/ideas.md` (a pointer so a `drop` that empties a section removes its heading), and the unit's
  process-defect record.
- `showcase/quality/commit-hygiene`'s `## Purpose` enumerates each build check the capability owns, and a delta cannot
  carry one: the archive commit adds the ideas-section gate to it.
- No runtime, API, or deployment change: the gate reads one tracked file and fails `check`.
