## Why

The local deployment's `values-local.yaml` carries an `axon-showcase-redis-client: "true"` pod label on the projection
and query services, but there is no redis in the stack — the infra is postgres, kafka, and opensearch only, and no code
or chart references a redis connection. The label is a leftover.

## What Changes

- Remove the `axon-showcase-redis-client: "true"` label from the `projectionService.podLabels` and
  `queryService.podLabels` in `helm/values/axon-showcase/values-local.yaml`.
- The remaining `-client` labels (db-events, kafka, os-views) stay unchanged — they are required by the local target's
  restricted infra NetworkPolicies (`allowExternal: false` on the bitnami charts), and this change is strictly about
  removing the dead redis label.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none (config-only cleanup; no spec-level behavior change — `skip_specs: true`)

## Impact

- `helm/values/axon-showcase/values-local.yaml` — two label lines removed; the local render otherwise unchanged.
- No code, chart, or spec changes; the rendered deployments gain nothing and lose only a label nothing consumes.
