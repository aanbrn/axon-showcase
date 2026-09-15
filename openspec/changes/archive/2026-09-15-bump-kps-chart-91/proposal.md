# Proposal: Bump the kube-prometheus-stack (kps) observability chart to 91.4.0

## Why

The weekly `helmUpdates` check reports the pinned `prometheus-community-stack` coordinate is stale. The open "Helm
updates" issue names `90.0.0 -> 91.2.3`, but the chart repository now publishes `91.4.0` — the same app version
(`v0.94.0`) as `91.2.3`, so this change takes the current latest and clears the report. Unlike the Bitnami infra charts,
the observability charts carry no `*-image-tag` pin and are not suppressed for major bumps, so the bump is actionable.
The catalog requires every Helm chart coordinate to be a concrete version, so this is a plain pin bump.

## What Changes

- Bump `prometheus-community-stack` in `gradle/libs.versions.toml` from `90.0.0` to `91.4.0`.
- Update the `--version 90.0.0` flag on the manual `helm install kps prometheus-community/kube-prometheus-stack` command
  in `AGENTS.md` to `--version 91.4.0`.
- Verify the bump: render the chart at `91.4.0` with `helm/values/kps/values-local.yaml` and smoke-test the local
  deployment (release installs, observability pods ready, Prometheus targets up, Grafana datasources wired).

## Capabilities

### New Capabilities

<!-- none -- a pure dependency bump; `skip_specs: true` is set. -->

### Modified Capabilities

<!-- none -- the `infra-image-versions` requirement (every chart coordinate is a concrete version) is satisfied by
the bumped concrete pin and does not change. -->

## Impact

- **Build**: `gradle/libs.versions.toml` — one version bump (`prometheus-community-stack = "91.4.0"`). No build logic
  changes; the pin feeds `helm.releases`' `kps` entry automatically.
- **Helm**: no chart/values changes expected unless the `91.4.0` chart drops or renames a value the local values use;
  the smoke test confirms.
- **Docs**: `AGENTS.md` — the manual `kps` install command tracks the new pin.
- **Behavior**: the local observability stack deploys at `91.4.0`; the `helmUpdates` report no longer lists the
  coordinate.
