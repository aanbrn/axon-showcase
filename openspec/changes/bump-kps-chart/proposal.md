# Proposal: Bump the kube-prometheus-stack (kps) observability chart

## Why

The Helm update check reports `prometheus-community-stack` (the kube-prometheus-stack observability chart, deployed as
the `kps` release) at `88.6.2`, with `90.0.0` available. Unlike the Bitnami infra charts, the observability charts
carry no `*-image-tag` pin and are not suppressed for major bumps — a major version carries no test-surface image-tag
drift to coordinate, so it surfaces as actionable. The version catalog requires every Helm chart coordinate to be a
concrete version, so this is a plain pin bump (`88.6.2` → `90.0.0`).

## What Changes

- Bump `prometheus-community-stack` in `gradle/libs.versions.toml` from `88.6.2` to `90.0.0`.
- Verify the deployment after the bump: install the `kps` release at the new version (and the full local stack) and
  smoke-test that the observability wiring still works — Prometheus pods ready, scrape targets up (the services'
  ServiceMonitors, including the web-UI metrics), and the Grafana datasources (Prometheus, Tempo) wired. The
  `values-local.yaml` kps-related settings (monitoring namespace, `release: kps` serviceMonitor labels, dashboard
  annotations) must still match what the new chart expects.

## Capabilities

### Modified Capabilities

- `showcase/quality/infra-image-versions` — the catalog's `prometheus-community-stack` concrete version coordinate
  moves to `90.0.0`, so the "Every Helm chart coordinate is a concrete version" and "All surfaces resolve from the
  catalog" scenarios reflect the new pin.

## Impact

- **Build**: `gradle/libs.versions.toml` — one version bump (`prometheus-community-stack = "90.0.0"`). No build logic
  changes (the pin feeds `helm.releases`' `kps` entry automatically).
- **Helm**: no chart/values changes expected unless the `90.0.0` chart drops or renames a value the local values use;
  the smoke test confirms.
- **Docs**: `AGENTS.md` / `README.md` — update the pinned `kps` version in the Kubernetes Deployment instructions if
  they quote `88.6.2`.
- **Behavior**: the local observability stack deploys at `90.0.0`; Prometheus/Grafana/Tempo wiring is verified by the
  smoke test.