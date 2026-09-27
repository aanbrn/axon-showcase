# Proposal: Route the smoke through the chart's ingress

> **Parked, 2026-09-28 — the premise below was wrong.** The smoke's failing assertion was not the port-forward: the
> review of this change reproduced run 36354957471 and every one of its 37 `PollShowcase` KOs is a gateway
> `503 Service Unavailable` in a nine-second window at the `baseline` plateau's start, while the port-forward's only
> errors came two minutes later at teardown and caused no failures. I misdiagnosed the failing hop (a log slice hid the
> 503s); the route below is still the faithful way for the smoke to reach the deployment, but it would not have fixed
> the failure. The 503s are diagnosed and fixed in a separate change; this branch stays unmerged until the ingress route
> is reconsidered on its own merits.

## Why

The smoke's second dispatch reached the load step — the cluster, the images, the trimmed `ci` install, and the `smoke`
profile all succeeded — and then failed: `kubectl port-forward` broke under the sustained `baseline` plateau, logging
`portforward.go:404 "Unhandled Error" … read: connection reset by peer`, which KO'd 37 polls and failed the assertions.
The fragile hop is the harness, not the deployment: the chart ships an ingress, a user reaches the deployment through
it, and the smoke should exercise that path rather than a userspace proxy that resets under ten long-lived SSE
connections.

## What Changes

- A kind cluster configuration for the workflow: the host's 80 and 443 mapped into the node, and the node labelled
  `ingress-ready=true` so an ingress controller can bind them.
- An `ingress-nginx` Helm release, pinned in the version catalog and selected only by the `ci` target — the local target
  excludes its tag, since a local cluster already has its own controller (Traefik on colima, ingress-nginx on
  kind/minikube) — with the kind-specific host-port and node-selector settings in its `values-ci.yaml`.
- The `ci` values enable the gateway's ingress with the hostname the local deployment uses, so the load profiles point
  at `http://axon-showcase-api` — the same target the records use — resolved by a runner hosts entry, and the
  port-forward step is deleted.
- `merge-governance`'s smoke requirement is revised: the profiles run against the deployment through its ingress.
- `AGENTS.md` and `README.md` follow.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/merge-governance` — the smoke reaches the gateway through the chart's ingress rather than a
  port-forward.

## Impact

- **Build**: `build.gradle.kts` (a release + the local target's tag expression), `gradle/libs.versions.toml` (the
  ingress chart's coordinate), a kind config file, the `ci` and ingress values, the workflow; the smoke's requirement;
  docs.
- **Tests**: verified by dispatching the smoke after it lands — the profiles must pass through the ingress, which is the
  path a user takes.
- **Deployment**: none — the release is `ci`-only and the local path is untouched.
