# Design

## Context

See `proposal.md` — Why. The current state that shapes the approach:

- Grafana is deployed by the `kps` release into the `monitoring` namespace, configured by
  `helm/values/kps/values-local.yaml` (the release resolves its values via `valuesDir("helm/values/$name")` in
  `build.gradle.kts`). Grafana is reachable today only through
  `kubectl port-forward -n monitoring svc/kps-grafana 3000:80`.
- The app's gateway and web UI are exposed by the `axon-showcase` chart's own Ingresses; `setup-hosts.sh` manages the
  `/etc/hosts` entries for their hostnames by resolving the ingress-controller LoadBalancer address generically
  (`kubectl get svc -A`, filtered by ingress component), so the detection is already namespace-agnostic.
- The app's local values (`helm/values/axon-showcase/values-local.yaml`) enable their Ingresses **without**
  `ingressClassName`, relying on the cluster's default IngressClass — the repo deliberately avoids hard-coding a
  machine-specific class (`helm.local.kubeContext` is the same principle for the kube context).
- The pinned `kube-prometheus-stack` chart (`91.4.1`) exposes `grafana.ingress` (`enabled`, `ingressClassName`,
  `annotations`, `labels`, `hosts`, `path`, `tls`), verified via `helm show values`.

## Goals / Non-Goals

**Goals:**

- Make the local Grafana reachable by a stable hostname (`axon-showcase-grafana`), symmetric with the app's
  `axon-showcase-api`/`axon-showcase-ui`, so the port-forward is no longer required.
- Keep the change to the local target and to existing mechanisms (the kps values file, `setup-hosts.sh`, the docs).

**Non-Goals:**

- Exposing Prometheus or Tempo — Grafana is the front door for both (see Decisions).
- TLS, authentication changes, or a non-local target.
- Changing Grafana's in-cluster datasource wiring or `setup-hosts.sh`'s LoadBalancer-address detection.

## Decisions

- **Expose Grafana only.** Alternatives considered: all three components, and Grafana + Tempo. Rejected because Grafana
  already fronts metrics and traces (the chart defaults the Prometheus datasource; `values-local.yaml` adds the Tempo
  one), so a Prometheus or Tempo hostname duplicates an existing path — Prometheus's expression browser/API is an
  operational debugging surface rather than a demo surface, and Tempo has no standalone UI (its traces are read in
  Grafana Explore). Each extra hostname is also more exposed surface and another line to keep in sync in
  `setup-hosts.sh` and the README. The agent's trace-fetch workflow keeps its `svc/tempo` port-forward (out of scope).
- **Configure the ingress in `helm/values/kps/values-local.yaml`.** Grafana belongs to the `kps` release, not the app
  chart, and this file is already the local home for the release's tuning (datasources, resources). Alternative: add it
  to the app chart — rejected, the chart does not own the `kps` release's resources.
- **Omit `ingressClassName`, relying on the cluster default.** Mirrors the app ingress's local values. Alternative:
  hard-code `traefik`/`nginx` — rejected as machine-specific, the same reason the release uses `helm.local.kubeContext`
  rather than a hard-coded context.
- **Reuse `setup-hosts.sh`; add only the hostname.** Its detection already scans all namespaces, so the ingress
  controller's single LoadBalancer address serves the `monitoring` Ingress too — no detection change is needed.
  Alternative: a separate helper or a `Host:`-header workaround — rejected, it duplicates the existing mechanism.
- **Hostname `axon-showcase-grafana`.** Follows the app's `axon-showcase-*` naming.

## Risks / Trade-offs

- No gate renders or reaches the Grafana Ingress → verify two ways: render it locally with
  `helm template kps prometheus-community/kube-prometheus-stack --version 91.4.1 -f helm/values/kps/values-local.yaml`
  and assert the Ingress/rule, and (live, owner-run) install kps and load `http://axon-showcase-grafana` after
  `./setup-hosts.sh setup`.
- `setup-hosts.sh` exits when it cannot resolve exactly one ingress LoadBalancer → pre-existing behavior, unchanged by
  this change; the new hostname inherits it.
- Stale copies of the port-forward recipe → sweep `README.md` and `AGENTS.md` for `port-forward`/`kps-grafana` in the
  change (the `svc/tempo 3200` trace-fetch note is intentionally left).
- Exposing Grafana widens the local surface (it has an admin login) → local cluster only; the README already documents
  the default login and the password source.

## Migration Plan

- Apply: update the values file, `setup-hosts.sh`, and the docs; reinstall/upgrade the `kps` release
  (`./gradlew helmInstallKpsToLocal`) so the Ingress is created; run `./setup-hosts.sh setup`.
- Rollback: revert the values file and reinstall `kps` (the Ingress disappears); `./setup-hosts.sh remove` clears the
  `/etc/hosts` entries.
