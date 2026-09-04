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
  built from the `aanbrn/axon-showcase-web-ui:${PROJECT_VERSION}` image, with the gateway's CORS allow-list updated to
  include the UI's origin.
- Add a `web-ui` Deployment + Service (and optional ingress) to the Helm chart, mirroring the existing service
  structure but with an nginx static container (port 80), its own `webUi` values section (image, replicaCount, port,
  resources), and the gateway CORS origin updated to the UI's in-cluster address.
- No behavioral change to the UI app itself — the same `npmBuild` output is served; only the packaging and deployment
  change.

## Capabilities

### New Capabilities

- `showcase/deployment/web-ui` — the web UI as a deployable static unit: the image build, its docker-compose service,
  and its Helm Deployment/Service.

### Modified Capabilities

- `showcase/deployment/helm-chart` — the chart now also renders the web-UI Deployment/Service; the "Service
  deployments" requirement's service list grows to include the UI, and the gateway CORS origin includes the UI.

## Impact

- **Build**: `frontend-conventions` — register the generic `dockerBuildImage` task (`pack` + Paketo NGINX buildpack
  over `build/dist`); `showcase-web-ui/build.gradle.kts` — set the image name; `docker-conventions` — compose
  build-first tasks depend on `dockerBuildImage` too. The `pack` CLI is a new build prerequisite.
- **Compose**: `docker-compose.yml` — `web-ui` service + gateway CORS origin.
- **Helm**: `helm/chart` — `web-ui` Deployment/Service/ingress templates, `webUi` values, gateway CORS origin in
  values, lint value files updated.
- **Docs**: `AGENTS.md` / `README.md` — the web-UI image, its port, and deployment notes.
- **Behavior**: the UI becomes reachable in the local and deployed stacks at its own address; no change to the UI app.