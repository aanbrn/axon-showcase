# Proposal

## Why

Two mechanically-detectable commit-hygiene classes still pass every gate. A merge conflict marker committed in markdown
or docs survives `spotlessCheck` — Prettier rewrites the `>>>>>>>` branch marker into a blockquote and then passes — and
only fails in compiled code, so a conflicted `AGENTS.md`, `README.md`, or `docs/*.md` can be committed unnoticed. A
tracked shell script or the git hook committed without its executable bit is silently skipped by git, a hazard
`AGENTS.md` already records, yet nothing verifies the mode. Both are cheap checks over the staged (hook) and tracked
(build) sets, closing the last two mechanically-detectable commit-hygiene classes.

## What Changes

- `scripts/commit-hygiene.py`: add a conflict-marker check to the `--staged` mode and a `--conflict-markers` build mode
  (scanning staged, then tracked, content for the branch markers `<<<<<<< ` and `>>>>>>> `), and an `--executable-bits`
  build mode verifying every tracked file git runs directly — `scripts/git-hooks/*`, `*.sh`, and `gradlew`, excluding
  the vendored `axon4to5-*` and generated `openspec-*` skill subtrees — is mode `100755`; add unit tests.
- `build.gradle.kts`: register `verifyConflictMarkers` and `verifyExecutableBits` (mirroring
  `verifyTrackedIgnoredFiles`) and add both to the root `check` task.
- `AGENTS.md` and `README.md`: name the new checks where the build gates and the guard are described.
- `openspec/specs/showcase/quality/commit-hygiene/spec.md`: one modified requirement (the guard gains the
  conflict-marker class) and two added requirements (the tracked conflict-marker verification and the executable-bit
  verification).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene`: extends the guard requirement with the conflict-marker class (the `--staged` mode
  gains it), and adds requirements for the tracked conflict-marker verification and the executable-bit verification.

## Impact

- **Build**: the root `check` task gains two dependencies (`verifyConflictMarkers`, `verifyExecutableBits`); no
  application module changes and no new dependency.
- **Tooling**: the commit-hygiene checker gains two modes and tests; the pre-commit hook gains the conflict-marker
  check.
- **Docs**: `AGENTS.md`, `README.md`.
- **Specs**: `showcase/quality/commit-hygiene`.
- **Deployment**: none.
