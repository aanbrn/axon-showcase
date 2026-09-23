# Proposal

## Why

The repository has no gate for a tracked file that `.gitignore` excludes: a generated artifact force-added at some point
stays tracked, and nothing in `check` or CI notices — the pre-commit guard's force-staged-artifact check runs only on a
local staged set, and `spotlessCheck`/`verifyCapturedMarkers` cover formatting and marker placement, not tracked-file
hygiene. A committed artifact is exactly the slip the guard exists to stop, so its CI-gated counterpart belongs in
`check`, where it protects every PR rather than only a clone that activated the hook.

## What Changes

- `scripts/commit-hygiene.py`: add a `--tracked-ignored` mode that reports every tracked path
  `git ls-files --cached --ignored --exclude-standard` lists, with unit tests for a force-committed ignored file and a
  clean repository.
- `build.gradle.kts`: register `verifyTrackedIgnoredFiles` (runs the checker's `--tracked-ignored` mode) and add it to
  the root `check` task, so the verification runs in the CI `build` gate.
- `AGENTS.md` and `README.md`: name the new check where the build gates are described, and complete the `AGENTS.md`
  root-check enumerations (the `# Check runs:` comment and the CI paragraph) with the three commit-hygiene tasks
  (`verifyCapturedMarkers`, `testCommitHygiene`, `verifyTrackedIgnoredFiles`).
- `openspec/specs/showcase/quality/commit-hygiene/spec.md`: an added requirement that the build verifies no tracked file
  is ignored.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene`: adds a requirement that the build verifies the repository tracks no ignored file
  (the CI-gated counterpart of the guard's force-staged-artifact check).

## Impact

- **Build**: the root `check` task gains a dependency (`verifyTrackedIgnoredFiles`); no application module changes and
  no new dependency.
- **Tooling**: the commit-hygiene checker gains a mode and tests.
- **Docs**: `AGENTS.md`, `README.md`.
- **Specs**: `showcase/quality/commit-hygiene`.
- **Deployment**: none.
