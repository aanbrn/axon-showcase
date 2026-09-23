# Proposal

## Why

Four commit-hygiene slips recur and are each caught only by a human or a review pass, though every one is mechanically
detectable from the staged set: a staged file the formatter would rewrite, a generated artifact force-staged, a path
staged and then edited (leaving the index stale), and a misplaced `captured:` provenance marker. The commit discipline
already tells the agent to inspect the staged set; nothing enforces it, so the slip reaches the commit and is caught
later. A guard at commit time blocks the class by construction, and a build check for the marker catches it even when a
local hook is absent or bypassed.

## What Changes

- Add `scripts/commit-hygiene.py` — the checker implementing the four checks, with a `--staged` mode (used by the hook)
  and a `--markers` mode (content-only, used by the build).
- Add `scripts/git-hooks/pre-commit` — the tracked git hook that runs the checker in `--staged` mode.
- Add `scripts/install-git-hooks.sh` — sets `core.hooksPath` to `scripts/git-hooks` for a clone (activation).
- Add `scripts/test-commit-hygiene.py` — Python `unittest` coverage of the checker against fixture repositories and
  fixture `AGENTS.md` content.
- Register `verifyCapturedMarkers` (runs the checker's marker mode) and `testCommitHygiene` (runs the Python unit tests)
  in `build.gradle.kts`, both wired into the root `check` task.
- Document the guard and its activation in `README.md` and `AGENTS.md`.
- Remove the parked idea from `docs/ideas.md` and update its disposition in `docs/retrospectives/2026-09-16.md` and
  `docs/retrospectives/2026-09-19.md` (the change that takes a suggestion records it).

## Capabilities

### New Capabilities

- `showcase/quality/commit-hygiene`: mechanical guards that run at commit time (a tracked pre-commit hook over the
  staged set) and in the build (the `captured:` marker-placement check), covering the commit/artifact hygiene the
  standard quality gates cannot see.

### Modified Capabilities

<!-- None: the CI marker check runs inside the standard `check` task, which merge-governance already describes as
     running the standard check with all static gates; no requirement text changes. -->

## Impact

- **Build**: the root `check` task gains two dependencies (`verifyCapturedMarkers`, `testCommitHygiene`); no application
  module changes and no new third-party dependency. Python 3 is already a documented prerequisite.
- **Tooling**: new `scripts/` entries (a Python checker, its tests, a git hook, and an activation script); a git hook
  that runs locally on `git commit` and is bypassable with `--no-verify`.
- **Docs**: `README.md`, `AGENTS.md`, `docs/ideas.md`, and the two `docs/retrospectives/` disposition tables.
- **APIs / deployment**: none.
