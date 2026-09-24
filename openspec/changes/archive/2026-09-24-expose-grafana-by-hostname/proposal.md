# Proposal: Expose the local Grafana by hostname

## Why

In the local Helm deployment, Grafana is reachable only with
`kubectl port-forward -n monitoring svc/kps-grafana 3000:80` — a command that must be re-run per session and after every
pod restart, and that cannot be bookmarked or shared. The application's own gateway and web UI are already reachable by
hostname through `setup-hosts.sh` (`axon-showcase-api`, `axon-showcase-ui`), so the observability front door is the one
access path still stuck behind a port-forward. A visitor following the README to see the dashboards and traces hits the
awkward path first.

## What Changes

- `helm/values/kps/values-local.yaml` — enable `grafana.ingress` for the local target with the hostname
  `axon-showcase-grafana`, omitting `ingressClassName` (the app ingress's local values do the same, relying on the
  cluster's default IngressClass).
- `setup-hosts.sh` — add `axon-showcase-grafana` to the managed `HOSTNAMES` list and its header/usage comment.
- `README.md` — replace the Grafana port-forward recipe with the hostname access path (run `./setup-hosts.sh setup`,
  then open `http://axon-showcase-grafana`), and add the hostname to the "Access the Deployed System" enumeration.
- `AGENTS.md` — update the "surface human-visible capabilities" example that names the observability access path as a
  port-forward, and the local-values sentence that enumerates the managed ingress hostnames.
- `docs/ideas.md` — remove the implemented "Grafana ingress + hostname instead of port-forward" idea.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — `skip_specs: true` is set in `.openspec.yaml`; no capability spec covers the local observability release's
ingress or the `setup-hosts.sh` hostname list — the app-chart Ingress requirements in `showcase/deployment/helm-chart`
and the README coverage mention in `showcase/quality/agent-skills` are unaffected).

## Impact

- **Build**: none — only a Helm values file, a shell script, and documentation change; no Gradle, Java, or web source.
- **Tests**: none — no JVM/web test exercises the local ingress values or `setup-hosts.sh`.
- **Deployment**: the local `kps` release renders one additional Ingress in the `monitoring` namespace. Verify live with
  `./gradlew helmInstallKpsToLocal` (or `helmInstallToLocal`) and `./setup-hosts.sh setup`, then confirm
  `http://axon-showcase-grafana` loads Grafana. Exposing Prometheus or Tempo stays out of scope — Grafana remains the
  front door for both.
