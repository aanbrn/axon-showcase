# Proposal

## Why

The seven weekly observational workflows (the four update checks, the upstream-reference report, the dependency security
scan, and the repository audits) run on Sunday 19:00–21:00 UTC — Monday 02:00–04:00 in the repository owner's time
(UTC+7). The owner reads their reports at the start of the working week, and a GitHub `schedule` delay (a documented
best-effort trigger, delayed under load — a recent run slipped ~3 hours) pushes the _latest_ of them (the upstream-
reference report, due 04:00) past that read time. Moving every workflow earlier widens the window a delay must exceed
before a report misses its reader, and placing `audit` first gives the report the owner most wants the most slack.

## What Changes

- Move the seven weekly workflows’ `cron` schedules earlier, into a single Saturday-17:00–19:00 UTC block (Sunday
  00:00–02:00 owner time), spaced 14–24 minutes apart on minutes off the `:00`/`:10`/`:20`/`:30`/`:40`/`:50` boundaries
  (GitHub’s documented high-load slots):
  - `audit` first (most slack — the report most wanted on time), then `dependency-security`, `dependency-updates`,
    `helm-updates`, `buildpack-updates`, `tooling-updates`, `upstream-references`.
- Keep every workflow weekly on the same weekday (`* * 6`) and off the top-of-hour boundary; the set stays
  collision-free for `verifyUniqueCronSchedules`.
- Record the new block in the `AGENTS.md` convention that states the scheduling rule (its "owner’s night" guidance names
  no day or time today, so the change makes the block explicit rather than extending an existing statement).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. The `merge-governance` requirements describe each workflow’s _cadence_ (“runs on a schedule and on demand”), not
its clock time; the times live in the workflow files and the `AGENTS.md` scheduling convention. No requirement’s
scenario outcome changes, so this change carries no spec delta (`skip_specs`).

## Impact

Seven `.github/workflows/*.yml` files (their `cron` blocks only) and one `AGENTS.md` convention sentence. No runtime,
image, or service change. `verifyUniqueCronSchedules` and `workflowLint` gate the edit; the change is verified by the
workflows’ **first real fire** on the new schedule (a dispatch runs the workflow body, not the scheduler, so it cannot
verify a `cron`).
