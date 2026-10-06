# Proposal

## Why

The `showcase/deployment/helm-chart` spec's `Network policies` requirement describes several ingress peers as "the same
service" or "same-service pods", but the chart renders them release-wide: each service's NetworkPolicy admits the peer
port from a `podSelector` matching `common.labels.matchLabels` (`app.kubernetes.io/name` + `instance`) merged with the
service's `podLabels`, which default to empty — so every pod of the release is admitted, not only the service's own. The
chart's own server-port template comments say "from other pods in the release". This is a spec-corpus inaccuracy, not a
chart defect: the release-wide peer set is what lets the gateway reach the query service's gRPC port.

## What Changes

- `openspec/specs/showcase/deployment/helm-chart/spec.md` — reword the `Network policies` scenarios
  `Projection and query server ports are restricted`, `Management port is restricted`, and
  `JGroups traffic is limited to the cluster` to name the release as the peer set, matching what the chart renders.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/deployment/helm-chart`: the `Network policies` requirement's peer wording changes from "the same service" to
  the release; no rendered behavior changes.

## Impact

- **Docs/spec only:** `openspec/specs/showcase/deployment/helm-chart/spec.md` (via this change's delta) and the parked
  idea in `docs/ideas.md`. No chart template, values, or code changes.
