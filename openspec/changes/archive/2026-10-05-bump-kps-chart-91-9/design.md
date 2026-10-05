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

### Decision: bump to the resolved latest (91.9.0)

The tracker named 91.9.0 (the version `helm search repo prometheus-community/kube-prometheus-stack` resolves), so the
pin moves 91.8.1 → 91.9.0.

### Decision: verify with a live install on the local cluster

A chart bump ships a new preconfigured set of images and CRDs, so a green `helm template`/`lint` proves nothing about
the deployed system: the `kps` release is upgraded in place on the local cluster and the deployed signal read — the pods
ready, the Prometheus targets up, and the Grafana datasources wired. A runner-hosted `kind` run stays an option (its
workflow is on the default branch and dispatchable), but the local install is the verification.

- **Alternative — trust `helm lint`/`template`:** renders only; a chart can render and fail to become ready.
- **Alternative — a runner-hosted `kind`:** a second full cluster install for a patch bump; the local in-place upgrade
  reads the same signal and is enough here.

## Risks / Trade-offs

- **A new chart ships newer images/CRDs that fail to become ready** → the live install + smoke (pods, targets,
  datasources) surfaces it.
