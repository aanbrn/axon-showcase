## Why

The `verifyInfraImageVersions` drift gate compares the official `*-image-tag` against the Bitnami chart's preconfigured
`image.tag` by stripping trailing `.0` segments from **both** sides. That normalization is only meaningful for the chart
side (Bitnami always writes the full app version, e.g. `17.6.0`); applied to the official tag it erases the tag's own
precision and blesses a bare-major tag like `17` as if it meant `17.0.0` — but `postgres:17` is a floating tag that
currently resolves to `17.11`. The comparison granularity should follow the granularity the official tag actually
declares, and a floating bare-major tag must be rejected outright.

## What Changes

- **Replace zero-strip-both comparison with granularity-truncation**: the chart app version is truncated to the official
  tag's numeric segment count, then compared exactly against the official tag. The official tag is never mutated.
- **Reject bare-major official tags**: an official `*-image-tag` with fewer than two numeric segments (e.g. `17`) fails
  the gate with a clear message, because such tags are floating and cannot be a single source of truth.
- **Fix the documented equivalence claim**: the spec and `AGENTS.md` wording that treats `17` ≡ `17.0.0` as an
  equivalent zero-padding is removed; the correct model is "the chart must match at the granularity the official tag
  declares".

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `showcase/quality/infra-image-versions`: the "Drift between surfaces is caught by the build" requirement changes —
  the comparison no longer strips the official tag, and a bare-major official tag is rejected as floating.

## Impact

- `build-logic/src/main/kotlin/VerifyInfraImageVersionsTask.kt` — comparison logic and new bare-major guard.
- `openspec/specs/showcase/quality/infra-image-versions/spec.md` — requirement and scenario wording.
- `AGENTS.md` — refresh the infra image versions note to match the corrected semantics.
- Current pins (`17.6`, `3.9.0`, `3.2.0`) remain valid and unchanged; postgres is pinned at minor granularity because
  Docker Hub publishes no three-segment postgres tags.