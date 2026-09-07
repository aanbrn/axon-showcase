# Proposal: Ship the web UI as a dedicated deployable unit

## Why

The `showcase-web-ui` module is currently build-only: `npmBuild` produces `build/dist`, but no Docker image is built
and nothing serves it in deployment — the Docker/Helm stack has no UI, and the pipeline can only be demonstrated via
the Vite dev server (`viteDev`) or preview. The UI is a standalone deployable (decided: not served by the API
gateway), so shipping it means building a dedicated static image, adding it to docker-compose, and adding a Helm
Deployment/Service with its values and the CORS origin it needs.

## What Changes

- Build a dedicated web-UI image: a static nginx image serving the `npmBuild` output (`build/dist`), produced by the
  Paketo buildpack pipeline via the **`pack` CLI** (the canonical CNB tool for static sites — Spring's
  `bootBuildImage` is JVM/Spring-specific and doesn't fit a frontend module). Following the repo's
  convention/mechanism split, `frontend-conventions` registers a **generic** `dockerBuildImage` task (the `pack`
  build mechanism over `build/dist` with the Paketo NGINX buildpack), and `showcase-web-ui/build.gradle.kts` sets its
  `imageName`. The image is built with patched run images, an SBOM, and no committed Dockerfile. The compose
  build-first tasks gain a dependency on the generic task.
- Add the web UI to `docker-compose.yml`: a `web-ui` service publishing the static site on a host port (e.g. `8084`),
  built from the `aanbrn/axon-showcase-web-ui:${PROJECT_VERSION}` image, with the gateway's CORS allow-list updated
  to include the UI's origin.
- Add a `web-ui` Deployment + Service to the Helm chart, mirroring the existing service structure but with an nginx
  static container (port 80) and its own `webUi` values section (image, replicaCount, port, resources, `ingress` +
  `route` blocks, networkPolicy), plus the api-gateway's external-exposure and network templates (Ingress, HTTPRoute,
  NetworkPolicy). The gateway CORS origin is updated to the UI's in-cluster address, and the local deployment exposes
  the UI alongside the gateway's existing ingress. The local gateway ingress hostname is renamed `axon-showcase` →
  `axon-showcase-api` (with the UI as `axon-showcase-ui`) so the two public surfaces are unambiguous, and a
  `setup-hosts.sh` helper documents direct hostname access instead of the `Host:`-header curl workaround.
- Add server-side nginx observability: enable `BP_NGINX_STUB_STATUS_PORT` on the image and add a `webUi`
  ServiceMonitor (mirroring the api-gateway's) so nginx server metrics appear in the existing Prometheus/Grafana
  stack. Client-side (RUM: web vitals, JS errors, trace propagation) is out of scope — a separate UI change.
- No behavioral change to the UI's features — the same bundle is served; the one app change is that the API base URL
  is read at runtime from `window.__API_BASE_URL__`, which the container renders from the `SHOWCASE_API_BASE_URL`
  env var at start (`start.sh` → `config.js`), falling back to the build-time `VITE_API_BASE_URL`. This keeps
  configuration a plain runtime env var (with a baked `BPE_DEFAULT_SHOWCASE_API_BASE_URL` default), matching the JVM
  services — no ConfigMap/mount, no per-target rebuild.

## Capabilities

### New Capabilities

- `showcase/deployment/web-ui` — the web UI as a deployable static unit: the image build, its docker-compose service,
  its Helm Deployment/Service, and its nginx server-metrics ServiceMonitor.

### Modified Capabilities

- `showcase/deployment/helm-chart` — the chart now also renders the web-UI Deployment/Service; the "Service
  deployments" requirement's service list grows to include the UI, and the gateway CORS origin includes the UI.

## Impact

- **Build**: `frontend-conventions` — register the generic `dockerBuildImage` task (`pack` + Paketo NGINX buildpack
  over `build/dist`); `showcase-web-ui/build.gradle.kts` — set the image name; `docker-conventions` — compose
  build-first tasks depend on `dockerBuildImage` too. The `pack` CLI is a new build prerequisite.
- **Code**: `showcase-web-ui/src/shared/api.ts` — read `window.__API_BASE_URL__` at runtime; add `start.sh` rendering
  `config.js` from the `SHOWCASE_API_BASE_URL` env var at container start.
- **Compose**: `docker-compose.yml` — `web-ui` service + gateway CORS origin + `SHOWCASE_API_BASE_URL` env (or the
  baked `BPE_DEFAULT_SHOWCASE_API_BASE_URL`).
- **Helm**: `helm/chart` — `web-ui` Deployment/Service/ingress/route/hpa/vpa/pdb/networkpolicy/servicemonitor
  templates, `webUi`
  values incl. `SHOWCASE_API_BASE_URL`, gateway CORS origin in values, local values expose the UI via ingress, local
  gateway ingress hostname renamed to `axon-showcase-api`, lint value files updated.
- **Docs**: `AGENTS.md` / `README.md` — the web-UI image, its port, deployment notes, and a `setup-hosts.sh` helper
  for the local `/etc/hosts` hostname access.
- **Behavior**: the UI becomes reachable in the local and deployed stacks at its own address; the API base URL is
  runtime-configurable via the `SHOWCASE_API_BASE_URL` env var (rendered to `config.js` at container start,
  `BPE_DEFAULT_*` baked default) — no per-target image rebuild, no ConfigMap/mount.