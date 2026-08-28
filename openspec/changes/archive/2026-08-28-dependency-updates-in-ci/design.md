# Run the Gradle dependency update report on GitHub — Design

## Context

See proposal.md — Why. The `dependencyUpdates` task (from `io.github.ben-manes.versions`) is configured in
`build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts`: it reports only catalog-owned coordinates,
suppresses major updates for groups in `config/dependency-updates/major-disabled.properties`, and treats
calendar-versioned coordinates by release-train (YYYY.TRAIN) so a service-release bump is minor. It writes the
report to `build/dependencyUpdates/report.txt`, which the `/dependency-updates` and `/gradle-update` commands already
read. The Gradle wrapper section at the end of the report states the current/pinned wrapper version.

The merge-governance change established the observational-workflow pattern (`ci.yml`, `e2e.yml`, `snyk.yml`); this
is the dependency-hygiene half.

## Goals / Non-Goals

**Goals:**
- Run the dependency update report automatically on a schedule and on demand via `workflow_dispatch`.
- Surface the report in a durable, reviewable place: a GitHub issue that is updated in place on each run.
- Use the `GITHUB_TOKEN` (least privilege: `issues: write`) — no PAT, no extra secrets.
- Reuse the existing `dependencyUpdates` task and its report file unchanged.

**Non-Goals:**
- Adding the report to the PR fast gate or the `main` full gate — it is advisory, not a merge quality gate.
- Dependabot or Renovate — they would not respect the deferred-major policy (`major-disabled.properties`) or the
  calendar-versioning heuristic; the custom Gradle task is the source of truth.
- Automatically applying dependency bumps — the report informs, a human applies via the catalog.
- Caching workspace `build/` directories (stale-report/jacoco rule, same as the other workflows).

## Decisions

### D1: Dedicated `dependency-updates.yml` workflow, scheduled + manual only

A new `.github/workflows/dependency-updates.yml` with a single `dependency-updates` job, triggered by:

```
on:
  schedule:
    - cron: '0 2 * * 1'    # weekly, Mondays 02:00 UTC
  workflow_dispatch:
```

Neither `pull_request` nor `push` triggers it, so it never couples to or delays the merge gate. Weekly matches the
`dependencyUpdates` cadence (dependency bumps land at a reviewable rhythm).

Alternatives rejected:
- **Adding a `push`-to-`main` trigger** — advisory report on every merge adds noise with no gate value.
- **Dependabot** — ignores the repo's deferred-major and calendar-versioning policy (see Non-Goals).

### D2: Report surfaced via an in-place-updated GitHub issue

The job runs `./gradlew dependencyUpdates`, then opens (or updates) a stable "Dependency updates" issue. A stable
title lets subsequent runs update the same issue rather than stacking new ones. The body summarizes the available
catalog updates from `build/dependencyUpdates/report.txt` plus the `Gradle CURRENT updates` wrapper section, and
links the report file as context.

Alternatives rejected:
- **Log-only** — the report would vanish in the Actions tab; an issue keeps it reviewable and commentable.
- **A new issue per run** — noise; in-place update is the standard "bot issue" pattern.

### D3: Least-privilege `GITHUB_TOKEN` (`issues: write`)

The workflow grants `contents: read` (checkout + Gradle cache) and `issues: write` (create/update the issue) on the
`GITHUB_TOKEN`. No PAT: the workflow only touches this repo's issues, and `GITHUB_TOKEN` works on scheduled runs with
these explicit permissions (the standard cron-issue pattern). A classic PAT with the `repo` scope would be
account-wide and is unnecessary here.

### D4: Notify the repository owner via an `@mention`

The issue body starts with `cc @<repository_owner>` (resolved from the `github.repository_owner` context, not
hardcoded). GitHub emails users mentioned in an issue, so the owner is notified on creation; because the update step
rewrites the body via `gh issue edit`, a re-mention also fires the notification on subsequent scheduled runs — the
report never silently lands without an email.

## Risks / Trade-offs

- **Issue bot spam if the report is large** → the issue is updated in place (D2), so at most one issue exists; the
  report is advisory, not a notification.
- **Scheduled cadence may lag a fast-breaking update** → the `main` gate and Snyk (weekly) still run; this report is
  the proactive dependency-hygiene signal, not the only one.
- **The issue needs triage** → it is informational; a maintainer applies updates via the catalog when appropriate,
  following the deferred-major policy captured in the dependency-management spec.
- **`issues: write` on `GITHUB_TOKEN`** → scoped to this repo only; least privilege, no account-wide PAT exposure.