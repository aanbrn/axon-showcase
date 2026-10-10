# Proposal

## Why

The Helm chart ships a custom Axon Showcase Grafana dashboard, but no spec names it: `showcase/deployment/helm-chart`'s
"Extra deployments and dashboards" requirement speaks only of "a bundled Grafana dashboard", so a spec reader cannot
learn that a custom dashboard exists or what it is for. Its existence and purpose live only in the README, which the
spec corpus does not own.

## What Changes

- `openspec/specs/showcase/deployment/helm-chart/spec.md`: the "Extra deployments and dashboards" requirement gains an
  outcome-level clause naming the bundled dashboard as the Axon Showcase dashboard (a custom observability view of the
  application), and its provisioning scenario names that dashboard.
- No panel inventory, section list, or panel count is added — the dashboard's contents stay in the README and in
  `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`.
- Docs: verify `AGENTS.md`, `README.md`, `docs/adr/`, and `docs/ideas.md`; none is expected to change, since the README
  already describes the dashboard.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/deployment/helm-chart`: the "Extra deployments and dashboards" requirement now names the bundled dashboard
  as the Axon Showcase dashboard.

## Impact

- Spec-only: no code, build, or deployment behavior changes. The dashboard JSON and its provisioning ConfigMap are
  unchanged.
- `openspec validate --all` covers the delta; the archive syncs it into the main `helm-chart` spec.
