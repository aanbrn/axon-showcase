# Design

## Context

See `proposal.md` — Why. The `kube-prometheus-stack` chart version is a concrete catalog pin
(`prometheus-community-stack`); the `kps` Helm target installs it (values under `helm/values/kps/`). Unlike the bitnami
infra charts, it carries no `*-image-tag` the `verifyInfraImageVersions` gate cross-checks, so a bump is verified only
by installing it and reading the deployed signal.

## Goals / Non-Goals

**Goals:**

- Move the pin to the resolved latest and verify the chart installs and its components wire up.

**Non-Goals:**

- Any application or source change; only the chart version the `kps` target installs moves.

## Decisions

### Decision: bump to the resolved latest (91.8.1), not the tracker's snapshot (91.8.0)

The update-check issues refresh weekly, so the upstream can publish again between the report and the bump — it did
(91.8.0 → 91.8.1). Confirm the target with the tool's own lookup
(`helm search repo prometheus-community/kube-prometheus-stack`) and bump to the resolved latest.

### Decision: verify with a live install on the local cluster

A chart bump ships a new preconfigured set of images and CRDs, so a green `helm template`/`lint` proves nothing about
the deployed system: install it and read the deployed signal — the pods ready, the Prometheus targets up, and the
Grafana datasources wired — on the local cluster. A runner-hosted `kind` run was considered and deliberately skipped
(the owner's decision); the local install is the verification, and the skip is named in the change's report.

- **Alternative — trust `helm lint`/`template`:** renders only; a chart can render and fail to become ready.
- **Alternative — a runner-hosted `kind`:** needs a workflow on the default branch to be dispatchable; skipped here.

## Risks / Trade-offs

- **A new chart ships newer images/CRDs that fail to become ready** → the live install + smoke (pods, targets,
  datasources) surfaces it.
