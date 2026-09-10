## Context

The local deployment's infra values (`helm/values/axon-showcase-kafka/`, `...-db-events/`, `...-os-views/`) restrict the
bitnami NetworkPolicies (`allowExternal: false`), which is why the app's `values-local.yaml` carries the
`<release>-client: "true"` labels (db-events, kafka, os-views) — those are the labels the bitnami netpols admit. The
`axon-showcase-redis-client` label on projection and query is not among them: there is no redis release, no redis
connection config, and nothing in code or specs references it. It is dead weight from an earlier design.

This change deliberately does **not** touch the live `-client` labels. They exist because the local target's bitnami
netpols are restricted; they are local-target values, not chart concerns, and moving them into the chart would couple
the reusable chart to bitnami conventions and the local target's release names.

## Goals / Non-Goals

**Goals:**

- Remove the dead `axon-showcase-redis-client` label from `values-local.yaml`.

**Non-Goals:**

- Moving any `-client` labels into the chart (they belong in the local values, co-located with the netpol restrictions
  that require them).
- Changing the kafka/db-events/os-views labels or the bitnami netpol configuration.

## Decisions

**D1: Remove only the redis label; leave the live `-client` labels in `values-local.yaml`.** The redis label has no
consumer (no redis in compose/helm/code/specs). The live labels are required by the local target's restricted bitnami
netpols and must stay where the netpol restriction lives. Alternatives considered — moving all client labels into the
chart as defaults/hardcoded template labels — were rejected: the labels are a bitnami-netpol × local-target artifact
(their value derives from the local release names, and they only matter because the local values set
`allowExternal: false`), so hardcoding them into the reusable chart couples it to conventions and release names it does
not own.

## Risks / Trade-offs

- [A future redis addition would need to re-add the label] → trivial and expected; this change only removes what is dead
  today.
- [The change is config-only] → verified by rendering the local chart: pods carry the same live `-client` labels, minus
  the redis label.

## Migration Plan

1. Remove the two redis label lines from `values-local.yaml`.
2. Render the local chart and confirm projection/query pods carry only their live `-client` labels.
3. Rollback: re-add the two lines.

## Open Questions

None.
