# Design

## Context

The seven weekly workflows are independent `schedule`-triggered, `workflow_dispatch`-enabled jobs. Their times live in
their own `cron` blocks; the `merge-governance` spec describes their cadence only, and `AGENTS.md` carries the rule
("Schedule each workflow's runtime into the repo owner's night (UTC+7)"). GitHub's `schedule` trigger is best-effort and
is documented to slip under load, so a report's arrival is bounded below by its due time plus an unknown delay. The
owner reads the reports at the start of the working week (Monday morning, UTC+7 = Monday 00:00+ UTC).

## Goals / Non-Goals

**Goals:**

- Move the seven weekly runs earlier so a delay has more room before a report misses the owner's Monday read.
- Give the `audit` report — the one most wanted on time — the most slack, by scheduling it first.
- Keep the set collision-free and off the documented high-load top-of-hour slots.

**Non-Goals:**

- Guaranteeing report arrival. A `schedule` cannot be made punctual by any `cron` value; a delay exceeding the window
  still misses the deadline. A dispatch-fallback (a watchdog that dispatches a workflow whose artifact is absent) is the
  only mechanism that turns "likely" into "present-or-retriggered", and is deliberately out of scope here (parked).
- Changing any workflow's cadence (all stay weekly), trigger set (all stay `workflow_dispatch`-enabled), jobs, or
  permissions. Only the `cron` values and the one `AGENTS.md` sentence move.
- Touching the daily `e2e` and `deployment-smoke` runs, whose nightly cadence the owner did not ask to move.

## Decisions

- **One earlier block, not per-workflow tuning.** All seven move into a single Saturday-17:00–19:00 UTC window (Sunday
  00:00–02:00 owner), spaced 14–24 minutes apart. A uniform shift is simple to review and reason about; per-workflow
  offsets would buy nothing, since the delay is a property of GitHub, not of a workflow.
- **`audit` first.** Ordering by _slack_ rather than by _freshness_: the earliest slot absorbs the most delay. The
  alternative — `audit` last so its report is newest — optimizes the wrong variable against a multi-hour delay, since
  the newest report is worthless if it has not arrived.
- **Off the `:00`/`:10`/`:20`/`:30`/`:40`/`:50` minutes.** GitHub's high-load periods are documented to cluster near the
  top of the hour; a minute outside that set reduces avoidable delay. Chosen minutes: `13, 27, 43, 7, 23, 37, 53`.
- **Saturday-UTC (`* * 6`).** Sunday 00:00–02:00 in UTC+7 is Saturday 17:00–19:00 UTC, so the weekday field flips. The
  _owner-visible_ day (Sunday) is what the window is defined by; the cron field is derived from it.
- **`skip_specs`.** No requirement's scenario outcome changes — cadence is spec'd, clock time is not — so there is no
  delta spec. The `AGENTS.md` convention is the durable record of the time and is updated in this change.

## Risks / Trade-offs

- **The schedule still cannot guarantee arrival.** Accepted; the change widens the margin. Measured against the owner's
  Monday 05:00 read, the `audit` report now tolerates a delay of ~28h47m (Sat 17:13 UTC due → Mon 05:00 owner) where it
  previously tolerated ~1h20m (Mon 03:40 owner due → Mon 05:00 owner) — a ~27h improvement in the slack the report must
  outlast before missing its reader. A delay longer than the new margin still slips it; the fallback is parked.
- **Cron verification is deferred to the first real fire.** A `workflow_dispatch` runs the workflow body, not the
  scheduler, so it cannot prove the `cron`. The schedule is verified only when its first real fire occurs (the next
  Saturday-UTC window). This is inherent to a schedule-only edit, not a gap in the change.
- **The window lands just past the owner's midnight.** The earliest slots fire at Sunday 00:00–00:xx owner time — the
  owner's small hours just past midnight, not the "night" this block's predecessor implied — and the owner accepted this
  placement in exchange for the added delay margin.
