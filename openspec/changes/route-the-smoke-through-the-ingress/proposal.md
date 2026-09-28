# Proposal: Route the smoke through the chart's ingress

## Why

The smoke's job is that the chart installs and serves, but it never opens the front door: the `ci` values leave the
application's Ingress off and the smoke reaches the gateway by port-forward, so a wrong `ingressClassName`, host, path,
or backend would ship unnoticed. The ingress is also the documented access path — the same `http://axon-showcase-api`
the dated records use — so the smoke should traverse it. The route also drops the port-forward, a hop whose only
observed error came at teardown and caused no failures; the 37 `503`s of the second dispatch were gateway responses at
the `baseline` plateau's start, a profile the smoke no longer runs, and are diagnosed and fixed separately.

## What Changes

- A kind cluster configuration for the workflow: the host's 80 and 443 mapped into the node, and the node labelled
  `ingress-ready=true` so an ingress controller can bind them.
- An `ingress-nginx` Helm release, pinned in the version catalog and selected only by the `ci` target — the local target
  excludes its tag, since a local cluster already has its own controller (Traefik on colima, ingress-nginx on
  kind/minikube) — with the kind-specific host-port and node-selector settings in its `values-ci.yaml`.
- The `ci` values enable the gateway's ingress with the hostname the local deployment uses, so the smoke points at
  `http://axon-showcase-api` — the same target the records use — resolved by a runner hosts entry, and the port-forward
  step is deleted.
- `merge-governance`'s smoke requirement is revised: the smoke profile runs against the deployment through its ingress.
- `AGENTS.md` and `README.md` follow.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/merge-governance` — the smoke reaches the gateway through the chart's ingress rather than a
  port-forward.

## Impact

- **Build**: `build.gradle.kts` (a release + the local target's tag expression),
  `build-logic/src/main/kotlin/helm-conventions.gradle.kts` (its chart repository), `gradle/libs.versions.toml` (the
  ingress chart's coordinate), a kind config file, the `ci` and ingress values, the workflow; the smoke's requirement;
  docs.
- **Tests**: verified by dispatching the smoke after it lands — the smoke must pass through the ingress, which is the
  path a user takes.
- **Deployment**: none — the release is `ci`-only and the local path is untouched.
