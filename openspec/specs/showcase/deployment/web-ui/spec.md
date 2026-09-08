# showcase/deployment/web-ui Specification

## Purpose

Documents the web UI as a deployable static unit: a dedicated container image serving the built frontend, its
docker-compose service, and its Helm Deployment/Service, so the browser UI is reachable in the local and deployed stacks
at its own address (standalone — not served by the API gateway).

## Requirements

### Requirement: The web UI is packaged as a standalone static image

The web-UI build SHALL produce a container image that serves the built frontend (`build/dist`) over HTTP using a static
web server (nginx). The image SHALL be tagged with the project version and built from the same `npmBuild` output that
the local dev/preview flow serves, so the packaged UI is the same bundle.

#### Scenario: The image serves the built bundle

- **WHEN** the web-UI image is built
- **THEN** it serves the `build/dist` output over HTTP on the standard container port, with no backend application
  process

#### Scenario: The API base URL is runtime-configurable

- **WHEN** a deployment sets the `SHOWCASE_API_BASE_URL` env var (no baked default — the browser needs the
  externally-visible gateway URL, which only the deployment knows)
- **THEN** the container renders `config.js` from it at start and the UI uses it at runtime, so a single image works
  across environments without rebuilding or ConfigMap/mount

#### Scenario: The served page loads config.js before the app bundle

- **WHEN** a browser loads the served web-UI page
- **THEN** the HTML includes a `<script src="/config.js">` tag before the app bundle, so `window.__API_BASE_URL__` is
  set (from the runtime-rendered `config.js`) before the app boots — otherwise the app would fall back to same-origin
  and call the web-UI's own nginx instead of the gateway

#### Scenario: A missing base URL fails fast

- **WHEN** the `SHOWCASE_API_BASE_URL` env var is unset or empty at container start
- **THEN** the container exits non-zero (the start script refuses to serve a UI that would call the wrong origin),
  restarting until configured

#### Scenario: The image is versioned

- **WHEN** the web-UI image is built
- **THEN** it is tagged with the project version, matching the image naming convention of the other services

#### Scenario: nginx server metrics are exposed

- **WHEN** the web-UI image is built with the stub-status port enabled
- **THEN** nginx exposes basic server metrics (`/stub_status`) that a ServiceMonitor can scrape into Prometheus

#### Scenario: The metrics exporter makes the target scrapable

- **WHEN** a metrics exporter sidecar is wired to the web-UI deployment
- **THEN** the exporter converts nginx stub_status to Prometheus format (`/metrics`), the ServiceMonitor scrapes that
  endpoint, and the web-UI metrics target is up in Prometheus

### Requirement: The UI is reachable in the local compose stack

The docker-compose stack SHALL include a `web-ui` service that runs the web-UI image, publishes the UI on a host port,
and lets the browser reach the API gateway cross-origin — the gateway's CORS allow-list SHALL include the compose UI
origin.

#### Scenario: Compose serves the UI

- **WHEN** the compose stack is up
- **THEN** the UI is reachable at its published host port and loads the built application

#### Scenario: The compose UI can call the gateway

- **WHEN** a browser at the compose UI origin calls the gateway
- **THEN** the gateway's CORS allow-list includes that origin, so the REST and SSE endpoints work cross-origin

### Requirement: The UI is deployed by the Helm chart

The Helm chart SHALL render a web-UI Deployment and Service running the web-UI image, with its own `webUi` values
(image, replicas, service port, resources, autoscaling, pdb), external exposure via both an Ingress and an HTTPRoute
(like the api-gateway), availability via HPA/VPA/PDB templates (defaulted off, like the other services), a NetworkPolicy
restricting its traffic (public HTTP ingress, monitoring-only metrics port, minimal egress), and the gateway's CORS
allow-list in the chart SHALL include the web-UI origin. The Deployment SHALL follow the shared service conventions
(common ServiceAccount, `/tmp` emptyDir, securityContexts, readiness probe), and the Service SHALL expose named ports
for the UI and its metrics.

#### Scenario: Helm deploys the UI

- **WHEN** the chart is installed
- **THEN** a web-UI Deployment and Service are rendered, serving the UI image on its service port

#### Scenario: The Deployment follows the shared service conventions

- **WHEN** the web-UI Deployment is rendered
- **THEN** it references the common ServiceAccount, mounts an emptyDir at `/tmp`, sets securityContexts, and defines a
  readiness probe, matching the other services

#### Scenario: Autoscaling and disruption budget are available

- **WHEN** an operator enables the web-UI `autoscaling` or `pdb` value
- **THEN** the chart renders the corresponding HPA/VPA/PDB for the web-UI, like the other services

#### Scenario: The UI can be exposed via Ingress or HTTPRoute

- **WHEN** an operator enables the web-UI `ingress` or `route` value
- **THEN** the chart renders the corresponding Ingress or HTTPRoute for the web-UI service

#### Scenario: The deployed UI can call the gateway

- **WHEN** a browser at the deployed UI origin calls the gateway
- **THEN** the gateway's CORS allow-list includes the UI origin, so the REST and SSE endpoints work cross-origin; the
  served UI loads `config.js` (setting `window.__API_BASE_URL__` to the externally-visible gateway URL) and a
  create-showcase POST from the UI origin reaches the gateway with a success status

#### Scenario: The UI origin is configurable

- **WHEN** an operator overrides the web-UI origin value
- **THEN** the gateway's CORS allow-list reflects the override
