# Design: cache infra image verification

## Context

`verifyInfraImageVersions` (root, wired into `check`) extends the helm plugin's `AbstractHelmCommandTask`. It declares
`checks` (chart refs, chart versions, image tags, values dirs) as `@Input` but **no outputs**, and it has a hard
`dependsOn("helmUpdateRepositories")`. In CI the whole chain — client download (18 MB), 3× `helm repo add`,
`helm repo update`, 3× `helm show values` — re-runs every time (~13s), even though the gate's inputs (pinned catalog
coordinates) rarely change. See `proposal.md` — Why.

## Goals / Non-Goals

- **Goal**: unchanged inputs make `check` skip the helm network chain entirely (Gradle build-cache restore).
- **Goal**: preserve the gate's behavior exactly — same checks, same failures, same release-rename/removal handling.
- **Goal**: keep the helm resolution as the source of truth when a coordinate actually changes.
- **Non-Goal**: changing what the gate verifies, the pinned-coordinate model, or the `.editorconfig`/spotless gate.
- **Non-Goal**: eliminating the network cost on the *first* run after a coordinate change (a cache miss legitimately
  re-verifies).

## Decisions

### The gate is cacheable because pinned chart versions are immutable

The task's result is fully determined by its inputs (chart version, image tag, values files) because a pinned Helm
chart version's preconfigured `image.tag` is immutable — chart versions cannot be republished with a different tag.
So once verified for a given pin, the result stays valid until the pin (or a values file) changes. This justifies
declaring the task's real inputs and outputs so Gradle can cache it, without weakening the drift gate.

- *Alternatives considered*: (1) keeping the gate always-live — rejected: re-pays ~13s every CI run for a result that
  is deterministic on unchanged inputs; (2) caching only the helm client tarball (Tier 1/2) — rejected: still runs
  the repo add/update + chart resolution on every run, saving only part of the cost.

### The values files become task inputs

`verifyValuesFiles` already reads each infra release's `values*.yaml` and fails if any pins `image.tag`. But those
files are not declared as task inputs, so a cache would not invalidate when a values file changes. The task SHALL
declare the values files as `@InputFiles` (path-sensitive) so a `image.tag` pin added to a values file invalidates the
cache and fails the gate.

- *Alternatives considered*: treating values files as untracked side data — rejected: would let a newly-added
  `image.tag` pin sneak past a cached pass.

### A result marker is the task output

The task currently declares no outputs, so Gradle treats it as always out-of-date. The task SHALL write a result
marker file (e.g. under `build/verification/`) as its `@OutputFile`; Gradle then restores the task from the build
cache when inputs are unchanged, skipping the action and its helm work.

- *Alternatives considered*: using a `@OutputDirectory` — a single marker file is simpler and sufficient (the task's
  observable result is pass/fail, not a set of files).

### The repo setup moves into the action; the hard dependency is removed

Today `dependsOn("helmUpdateRepositories")` forces the entire network chain to run before the task, even when the task
would be cache-restored. The task SHALL instead perform the repo setup itself (`helm repo add` + `helm repo update`)
inside its action, so a cache hit skips the action (and thus the network) entirely. A cache miss runs the full
resolution.

- *Alternatives considered*: keeping the dependency and relying on the chain's own up-to-date checks — rejected:
  `helmUpdateRepositories` uses `outputs.upToDateWhen()` (TTL), which is not build-cacheable and always executes, so
  the chain would still run on every CI run.

### The base task's inputs remain cache-compatible

`AbstractHelmCommandTask` declares `executable`, `debug`, `extraArgs` as `@Input` (stable strings) and the XDG dirs as
`@Internal` (not snapshotted). The `checks` input derives from the `helm.releases` container, so a release
rename/removal changes `checks` and invalidates the cache — preserving the "follows the configured releases"
requirement.

## Risks / Trade-offs

- [The cache could hide chart-repo drift] → Mitigation: pinned chart versions are immutable, so the cached tag is
  correct until the pin moves; a changed pin (or values file) invalidates the cache and re-verifies.
- [Moving repo setup into the action duplicates plugin logic] → Mitigation: the action uses the same helm client and
  repo configuration the plugin wires up; the duplication is a few `execHelm` calls, and removing the hard dependency
  is the only way a cache hit skips the network.
- [A cache miss is still ~13s] → Mitigation: accepted; a cache miss means a coordinate changed, which is exactly when
  live verification is wanted.

## Migration Plan

1. Update `VerifyInfraImageVersionsTask` — add `@InputFiles` for the values files, an `@OutputFile` result marker,
   and perform `helm repo add` + `helm repo update` inside the action before the `helm show values` resolution.
2. Remove `dependsOn("helmUpdateRepositories")` from the task registration in `build.gradle.kts`.
3. Verify: `verifyInfraImageVersions` passes on a clean run; re-running with unchanged inputs shows
   `UP-TO-DATE`/`FROM-CACHE`; changing a coordinate or a values file re-runs and (for a mismatch) fails.

Rollback: revert the task and registration changes; the hard dependency and always-live behavior are restored.

## Open Questions

None.