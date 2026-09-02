## Why

The weekly `helmUpdates` check reports that the pinned `prometheus-community-stack` chart coordinate is stale: the
catalog pins `77.14.0`, but the latest in the `prometheus-community` repository is `88.6.2` (app version `v0.85.0` →
`v0.93.1`). The `kube-prometheus-stack` chart is the kps observability release's source; the coordinate is a concrete
pin (per the `infra-image-versions` requirement), and bumping it to the current release keeps the deployment on a
maintained chart. Unlike the bitnami infra charts (whose major bumps are suppressed in
`config/helm-updates/major-disabled.properties` because a new chart ships a new preconfigured `image.tag` that must be
coordinated with the test-surface `*-image-tag` pins), the observability charts carry no `*-image-tag` — so a major
bump is an actionable, uncoordinated update.

## What Changes

- Bump `prometheus-community-stack` from `77.14.0` to `88.6.2` in `gradle/libs.versions.toml`.
- Update the `--version 77.14.0` flags on the manual `helm install kps ...` commands in `AGENTS.md` and `README.md`
  to `--version 88.6.2`, matching the catalog pin.
- No values changes: the chart renders cleanly at `88.6.2` with the existing `helm/values/kps/values-local.yaml`
  (verified via `helm template`), and all key overrides (Grafana Tempo datasource, resource requests/limits,
  alertmanager disabled) land correctly in the rendered output.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
<!-- none -- a pure dependency bump; `skip_specs: true` is set. The `infra-image-versions` requirement (every chart
coordinate is a concrete version) is satisfied by the bumped concrete pin and does not change. -->

## Impact

- `gradle/libs.versions.toml` — the `prometheus-community-stack` coordinate becomes `88.6.2`.
- `AGENTS.md`, `README.md` — the manual `helm install kps ... --version` flags track the new pin.
- `helm/values/kps/values-local.yaml` — unchanged (renders cleanly at `88.6.2`).
- No build-gate impact: the kps chart is not part of the `verifyInfraImageVersions` drift check (that gate covers only
  the bitnami infra charts), and `helmUpdates` will no longer report the coordinate once bumped.