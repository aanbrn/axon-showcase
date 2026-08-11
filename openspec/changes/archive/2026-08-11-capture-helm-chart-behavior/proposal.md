## Why

The `helm` module (Helm chart packaging the four showcase services, their migration/initialization jobs, RBAC,
network policies, autoscaling, and observability wiring) exists but has never been captured in an OpenSpec spec. Without
a spec, the deployment contract — what resources the chart creates, the gating conditions, and the configurable
values — is undocumented and changes to it are not reviewable against a stated contract.

## What Changes

- Author the first OpenSpec capability spec that documents the Helm chart's current behavior as requirement statements
  (MUST clauses), derived from the existing chart templates, helpers, and default values.
- No behavioral or code changes. This change only introduces the spec delta so it can be reviewed and later archived
  into `openspec/specs/`.

## Capabilities

### New Capabilities

- `showcase/helm-chart`: Current behavior of the deployment chart — the four service Deployments/Services with probes,
  security contexts, resources and image configuration; the command-service Flyway migration and query-service index
  initialization hook Jobs; autoscaling (HPA/VPA), PDBs, and NetworkPolicies per service; the JGroups clustering wiring
  for command-service and api-gateway; service account and RBAC; ingress/route for the api-gateway; ServiceMonitors and
  the observability environment wiring; and the configurable connection settings for PostgreSQL, OpenSearch, and Kafka.

### Modified Capabilities

- None.

## Impact

- New spec only: `openspec/changes/capture-helm-chart-behavior/specs/showcase/helm-chart/spec.md`.
- No production code, APIs, dependencies, or build configuration are affected.
