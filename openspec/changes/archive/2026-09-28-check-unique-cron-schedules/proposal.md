# Proposal

## Why

Nothing mechanically prevents two GitHub Actions workflows from sharing a `cron` schedule. GitHub accepts a duplicate,
but the two jobs collide on the scheduler's queue and undo a deliberate stagger: the
`schedule-jobs-into-the-owners-night` unit found `dependency-updates` and `helm-updates` already sharing `0 2 * * 1` at
HEAD and produced two more fresh pairs while re-staggering nine crons. The only guard today is a prose convention no
gate reads, so the next re-stagger (or a new scheduled workflow) can reintroduce the collision silently. A one-line scan
over `.github/workflows/` would have caught all three instances.

## What Changes

- `scripts/commit-hygiene.py`: add a `--unique-crons` mode that scans `.github/workflows/` and fails when two `cron`
  schedules collide (shared minute and hour with overlapping day-of-week fields — a field of `7` treated as `0`, since
  both name Sunday — or an identical cron string when a restricted day-of-month/month is involved), naming the workflow
  file or files carrying each colliding pair and the shared cron. Stdlib only.
- `scripts/test-commit-hygiene.py`: add cases for an exact duplicate, two schedules in one workflow file, a daily/weekly
  pair sharing a minute, weekly schedules on different days at the same time, a `7`-vs-`0` Sunday, a restricted-day pair
  (identical and different), a malformed cron, a commented-out `cron`, an inline trailing comment, a clean set, and a
  `.yaml` workflow (51 tests).
- `build.gradle.kts`: register `verifyUniqueCronSchedules` beside `verifyConflictMarkers`/`verifyExecutableBits` and add
  it to the root `check` task.
- `AGENTS.md` and `README.md`: name `verifyUniqueCronSchedules` where the commit-hygiene `check` tasks are enumerated.
- `docs/ideas.md`: remove the parked unique-cron idea (this change implements it) and add the new check to the parked
  test-coverage idea's list of build checks.
- `openspec/specs/showcase/quality/commit-hygiene/spec.md`: add a requirement that the build verifies no two workflow
  schedules collide.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene`: adds a requirement that the standard `check` verifies no two workflow `cron`
  schedules collide. (Widening the existing test-coverage requirement to the checker's build checks is a separate,
  parked idea and is not part of this change.)

## Impact

- **Build**: the root `check` task gains a dependency (`verifyUniqueCronSchedules`); no application module changes and
  no new dependency.
- **Tooling**: the commit-hygiene checker gains a mode and tests, still stdlib-only and Python 3.9 compatible.
- **Docs**: `AGENTS.md`, `README.md`, `docs/ideas.md`.
- **Specs**: `showcase/quality/commit-hygiene`.
- **Deployment**: none.
