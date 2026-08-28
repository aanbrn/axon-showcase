# Run end-to-end tests in CI on a schedule — Design

## Context

See proposal.md — Why. The `merge-governance` change shipped `ci.yml` (PR fast gate + `main` full gate) and the
`main-required-checks` ruleset, but deferred the heavy gates. `ShowcaseApiGatewayE2E` (`showcase-api-gateway`'s
`e2eTest` suite) boots the full pipeline: PostgreSQL, Kafka, OpenSearch, and the four `aanbrn/axon-showcase-*`
service images. The `e2eTest` task already `dependsOn("bootBuildImage", ...)` for all four services
(`showcase-api-gateway/build.gradle.kts:159`), so a single Gradle invocation builds every image and then runs the
suite against them. Image tags resolve to the fixed `project.version` (`0.1.0-SNAPSHOT`, `build.gradle.kts:19`), and
`java-conventions.gradle.kts:64` injects the same value as the `project.version` system property into every test
JVM — so the containers the e2e test references always match the built images with no extra plumbing.

The e2e run needs no credentials: images are built locally and never pushed.

## Goals / Non-Goals

**Goals:**
- Run the full end-to-end suite automatically on a nightly schedule and on demand via `workflow_dispatch`.
- Keep the e2e run entirely separate from the merge gate — never a required check, never triggered by PR/push.
- Reuse the existing `:showcase-api-gateway:e2eTest` task and its `bootBuildImage` dependencies unchanged.
- Reuse the Gradle cache (dependencies, wrapper, build cache) so nightly runs stay fast.

**Non-Goals:**
- Adding e2e to the PR fast gate or the `main` full gate — it is intentionally heavy (builds four images) and
  observational (schedule/manual only).
- Running `dependencySecurityCheck` (Snyk) in this workflow — separate concern, credentialed, deferred to its own
  follow-up.
- Pushing the built images to a registry or changing their tags.
- Cache of the workspace `build/` directories (stale `jacoco` exec data would corrupt coverage reporting; same rule
  as `ci.yml`).

## Decisions

### D1: Dedicated `e2e.yml` workflow, scheduled + manual only

A new `.github/workflows/e2e.yml` with a single `e2e` job, triggered by:

```
on:
  schedule:
    - cron: '30 0 * * *'    # nightly 00:30 UTC
  workflow_dispatch:
```

Neither `pull_request` nor `push` triggers it, so it never couples to or delays the merge gate. A scheduled +
manual-only heavy suite is the standard pattern for expensive, non-blocking verification.

Alternatives rejected:
- **Adding a `push`-to-`main` trigger** — redundant with the existing `main` full gate (which already runs the
  integration tier with real Testcontainers); e2e adds only cross-service image wiring, which a nightly cadence
  covers without burning ~15 min per merge.
- **Adding it to `ci.yml`** — a single workflow with two jobs would make the heavy run share the merge-gate
  workflow's identity and invite coupling; a separate workflow keeps the `build` check name unambiguous.

### D2: One job, one task, no secrets

The `e2e` job runs `./gradlew :showcase-api-gateway:e2eTest` on `ubuntu-latest` (Temurin 21, Docker preinstalled)
after `actions/checkout`, `actions/setup-java`, and `gradle/actions/setup-gradle`. No `env` secrets: the images are
built locally by the task's `bootBuildImage` dependencies and referenced by the fixed `project.version`, which the
test JVM already receives via the convention plugin. The Gradle cache restores the User Home only.

### D3: The e2e run is observational, never a required check

No ruleset change: the `main-required-checks` ruleset continues to require only the `build` check. The e2e workflow
has its own `e2e` job/check name, which is never added to any ruleset, so a failing nightly run reports but never
blocks merging. This matches the merge-governance proposal's intent that heavy gates run post-merge / on a schedule.

## Risks / Trade-offs

- **Nightly failures go unnoticed until someone looks** → the run appears in the Actions tab with a clear `e2e` name
  and failed status; acceptable for an observational gate, and a follow-up could add a notification.
- **~15 min of image building every night** → the cost of running the full suite; mitigated by the Gradle build
  cache (image layers via `bootBuildImage` are not cached, so this is the floor) and by it being schedule/manual
  only.
- **Flaky inter-service timing could produce spurious failures** → the suite already uses `await()` polling and
  health-check waits; a nightly cadence surfaces flakiness early, which is the point.
- **`bootBuildImage` needs network for base images** → `ubuntu-latest` has Docker and network; the same constraints
  as the local `e2eTest` runs already validated.