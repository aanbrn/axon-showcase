# Cache infra image verification

## Why

`verifyInfraImageVersions` (part of `check`) resolves each pinned chart's preconfigured `image.tag` live from the
Bitnami chart repository every time it runs. In CI, that costs ~13s per run (18 MB helm client download, 3× `helm repo
add`, `helm repo update`, 3× `helm show values`) — but the gate's inputs are the pinned catalog coordinates, which
rarely change. The result is deterministic for unchanged inputs, yet CI re-pays the full network cost on every run.

## What Changes

- Make `verifyInfraImageVersions` **build-cacheable**: declare its values-file contents as task inputs and a result
  marker file as an output, so Gradle can restore the task from the build cache when the inputs are unchanged.
- **Remove** the hard `dependsOn("helmUpdateRepositories")` dependency and embed the Helm repo setup (`helm repo add` +
  `helm repo update`) inside the task action, so a cache hit skips the entire network chain (client download, repo
  add/update, chart resolution) rather than just the final comparison.
- The gate's behavior is unchanged: it still verifies each pinned chart's preconfigured `image.tag` against the
  `*-image-tag`, fails on drift, and follows release renames/removals. The cache is sound because pinned chart versions
  are immutable — a pinned version's `image.tag` never changes, so a cached result stays valid until a coordinate
  moves.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/infra-image-versions`: the drift-verification requirement gains that the verification is
  cacheable on its inputs (the pinned coordinates and values files), so unchanged inputs do not re-run the Helm
  resolution.

## Impact

- **`VerifyInfraImageVersionsTask.kt`** (build-logic): add `@InputFiles` for the values files, an `@OutputFile` result
  marker, and move the repo setup (add/update) from the `helmUpdateRepositories` dependency into the action.
- **`build.gradle.kts`**: remove `dependsOn("helmUpdateRepositories")` from the `verifyInfraImageVersions` registration
  (the task owns its repo setup now).
- **CI**: unchanged workflow; the Gradle build cache (already shared via `gradle/actions/setup-gradle`) restores the
  cached verification on the common path (unchanged pins), turning ~13s of helm work into ~0s. Cache misses (a
  coordinate or values file changed) still run the full resolution.
- **Behavior**: unchanged gate semantics — pinned chart versions are immutable, so a cached result is valid until a
  coordinate moves.
- **Risk**: the cache must not hide a genuine repo-side drift. Since the chart version is pinned and immutable, the
  cached result reflects the immutable tag; a changed pin invalidates the cache. This preserves the spec's contract.