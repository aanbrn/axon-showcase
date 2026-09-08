## MODIFIED Requirements

### Requirement: The web UI is packaged as a standalone static image

The web-UI build SHALL produce a container image that serves the built frontend (`build/dist`) over HTTP using a
static web server (nginx). The image SHALL be tagged with the project version and built from the same `npmBuild`
output that the local dev/preview flow serves, so the packaged UI is the same bundle.

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