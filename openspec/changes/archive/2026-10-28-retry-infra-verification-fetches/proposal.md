# Proposal

## Why

The drift verification retries its repository add and update, but each chart's `helm show values` fetch is single-shot;
a transient failure there failed the Docker-free `check` during `retry-chart-dependency-fetch` while the same task
passed alone minutes later. It was the only read in this task's action without a bounded retry.

## What Changes

- `build-logic/src/main/kotlin/VerifyInfraImageVersionsTask.kt`: each `helm show values` call runs inside `BoundedRetry`
  (three attempts, a two-second pause, a warning per retry, the last failure propagating); the task's inputs and cache
  key are unchanged.
- `openspec/specs/showcase/quality/infra-image-versions/spec.md` (delta): the retry clause and its scenario cover each
  chart's values fetch as well as the repository add and update.
- `.opencode/skills/deployment-operations/SKILL.md`: the install-path retry note records that `verifyInfraImageVersions`
  also retries each chart's values fetch.
- `docs/ideas.md`: the 2026-10-28 entry parking this retry is removed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/infra-image-versions`: the bounded retry of the chart-repository reads covers the values fetch.

## Impact

- `build-logic`: one task action; no new input, no dependency, no change to the cache key.
- The gate's success path is unchanged; a transient values failure that clears within the retry budget is recovered, a
  persistent one still fails the build; a failing values fetch adds up to two pauses, per failing check.
- `.opencode/skills/deployment-operations/SKILL.md`: the retry note records that `verifyInfraImageVersions` also retries
  each chart's values fetch.
- One `docs/ideas.md` entry removed.
