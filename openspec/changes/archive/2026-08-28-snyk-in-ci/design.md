# Run the Snyk dependency security scan on GitHub — Design

## Context

See proposal.md — Why. The `dependencySecurityCheck` task runs `snyk test --all-sub-projects --policy-path=.snyk`
(`build-logic/src/main/kotlin/dependency-security-conventions.gradle.kts`). The root `.snyk` policy (21
version-pinned ignores, quarterly expiry 2026-11-28) makes the scan green locally. The Snyk CLI authenticates via the
`SNYK_TOKEN` environment variable. The scan is credentialed (needs the token) and lightweight (no containers, no
image builds) — unlike `e2e.yml` which is heavy but credential-free.

The merge-governance and e2e-in-ci changes kept the heavy/credentialed gates off the PR path as follow-ups; this is
the Snyk half of that deferral.

## Goals / Non-Goals

**Goals:**
- Run the dependency security scan automatically on a schedule and on demand via `workflow_dispatch`.
- Keep the snyk run entirely separate from the merge gate — never a required check, never triggered by PR/push.
- Reuse the existing `dependencySecurityCheck` task and the root `.snyk` policy unchanged.
- Authenticate via the `SNYK_TOKEN` secret only.

**Non-Goals:**
- Adding snyk to the PR fast gate or the `main` full gate — it is credentialed and the merge gate already surfaces
  dependency issues via other means; kept observational.
- Consolidating snyk into `e2e.yml` or `ci.yml` — separate workflow keeps each gate's check name and failure mode
  unambiguous.
- Changing the `.snyk` policy or the Gradle task — the workflow only invokes them.
- Caching workspace `build/` directories (stale `jacoco` exec data rule, same as the other workflows).

## Decisions

### D1: Dedicated `snyk.yml` workflow, scheduled + manual only

A new `.github/workflows/snyk.yml` with a single `snyk` job, triggered by:

```
on:
  schedule:
    - cron: '0 1 * * 1'    # weekly, Mondays 01:00 UTC
  workflow_dispatch:
```

Neither `pull_request` nor `push` triggers it, so it never couples to or delays the merge gate. A weekly cadence
balances freshness against the credential/observation nature of the scan; `workflow_dispatch` covers on-demand runs
(e.g. before a release).

Alternatives rejected:
- **Adding a `push`-to-`main` trigger** — redundant with the full `main` gate and makes a credentialed job run on
  every merge; a weekly cadence is sufficient for a policy-based dependency scan.
- **Adding it to `ci.yml` or `e2e.yml`** — the `e2e` workflow is deliberately credential-free; a separate `snyk`
  workflow keeps the `SNYK_TOKEN` secret scoped to exactly one job.

### D2: One job, one task, `SNYK_TOKEN` secret, CLI installed via `snyk/actions/setup`

The `snyk` job runs `./gradlew dependencySecurityCheck` on `ubuntu-latest` (Temurin 21) after `actions/checkout`,
`actions/setup-java`, `gradle/actions/setup-gradle`, and `snyk/actions/setup`. The Gradle task's
`snykExecutableOnPath()` guard fails without the CLI on PATH, so `snyk/actions/setup` installs it (pinned via
`snyk-version` to the same major as the local CLI, `1.x`). The `SNYK_TOKEN` secret is exported as an environment
variable so the Snyk CLI authenticates. The Gradle cache restores the User Home only.

### D3: The snyk run is observational, never a required check

No ruleset change: `main-required-checks` continues to require only the `build` check. The snyk workflow has its own
job/check name, never added to any ruleset, so a failing scan reports but never blocks merging. This matches the
merge-governance proposal's intent that credentialed gates run on a schedule.

## Risks / Trade-offs

- **`SNYK_TOKEN` must be set manually** → the workflow fails with an auth error until the repo owner adds the secret;
  documented in the tasks (step to set the secret) and this design.
- **Weekly cadence may miss a fast-breaking dependency** → the merge gate and `dependency-updates` still run; the
  weekly scan is a policy-drift backstop, not the primary dependency signal.
- **Failing scan is only visible on the Actions tab** → acceptable for an observational gate; the scan is green today
  and its quarterly-expiry ignores re-surface findings proactively.
- **Rate limit** → the policy-suppressed scan reports zero identified vulnerabilities, so per the "what counts as a
  test" policy it consumes no monthly quota (200 Open Source tests/billing period on the free org).