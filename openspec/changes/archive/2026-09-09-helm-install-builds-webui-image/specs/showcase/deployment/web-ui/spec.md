## MODIFIED Requirements

### Requirement: The UI is deployed by the Helm chart

The Helm chart SHALL render a web-UI Deployment and Service running the web-UI image, with its own `webUi` values
(image, replicas, service port, resources, autoscaling, pdb), external exposure via both an Ingress and an HTTPRoute
(like the api-gateway), availability via HPA/VPA/PDB templates (defaulted off, like the other services), a NetworkPolicy
restricting its traffic (public HTTP ingress, monitoring-only metrics port, minimal egress), and the gateway's CORS
allow-list in the chart SHALL include the web-UI origin. The Deployment SHALL follow the shared service conventions
(common ServiceAccount, `/tmp` emptyDir, securityContexts, readiness probe), and the Service SHALL expose named ports
for the UI and its metrics. The local deploy task (`helmInstallToLocal`) SHALL build the web-UI image before installing,
so a fresh local deploy has an image for the web-UI pod.

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

#### Scenario: The local deploy builds the web-UI image

- **WHEN** `./gradlew helmInstallToLocal` runs
- **THEN** it builds the web-UI image (alongside the four JVM service images) before installing the chart, so the web-UI
  pod has an image to run
