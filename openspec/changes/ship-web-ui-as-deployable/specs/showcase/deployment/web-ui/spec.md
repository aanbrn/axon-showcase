# showcase/deployment/web-ui Specification

## Purpose

Documents the web UI as a deployable static unit: a dedicated container image serving the built frontend, its
docker-compose service, and its Helm Deployment/Service, so the browser UI is reachable in the local and deployed
stacks at its own address (standalone — not served by the API gateway).

## ADDED Requirements

### Requirement: The web UI is packaged as a standalone static image

The web-UI build SHALL produce a container image that serves the built frontend (`build/dist`) over HTTP using a
static web server (nginx). The image SHALL be tagged with the project version and built from the same `npmBuild`
output that the local dev/preview flow serves, so the packaged UI is the same bundle.

#### Scenario: The image serves the built bundle

- **WHEN** the web-UI image is built
- **THEN** it serves the `build/dist` output over HTTP on the standard container port, with no backend application
  process

#### Scenario: The image is versioned

- **WHEN** the web-UI image is built
- **THEN** it is tagged with the project version, matching the image naming convention of the other services

### Requirement: The UI is reachable in the local compose stack

The docker-compose stack SHALL include a `web-ui` service that runs the web-UI image, publishes the UI on a host
port, and lets the browser reach the API gateway cross-origin — the gateway's CORS allow-list SHALL include the
compose UI origin.

#### Scenario: Compose serves the UI

- **WHEN** the compose stack is up
- **THEN** the UI is reachable at its published host port and loads the built application

#### Scenario: The compose UI can call the gateway

- **WHEN** a browser at the compose UI origin calls the gateway
- **THEN** the gateway's CORS allow-list includes that origin, so the REST and SSE endpoints work cross-origin

### Requirement: The UI is deployed by the Helm chart

The Helm chart SHALL render a web-UI Deployment and Service running the web-UI image, with its own `webUi` values
(image, replicas, service port, resources), and the gateway's CORS allow-list in the chart SHALL include the web-UI
origin.

#### Scenario: Helm deploys the UI

- **WHEN** the chart is installed
- **THEN** a web-UI Deployment and Service are rendered, serving the UI image on its service port

#### Scenario: The deployed UI can call the gateway

- **WHEN** a browser at the deployed UI origin calls the gateway
- **THEN** the gateway's CORS allow-list includes the UI origin, so the REST and SSE endpoints work cross-origin

#### Scenario: The UI origin is configurable

- **WHEN** an operator overrides the web-UI origin value
- **THEN** the gateway's CORS allow-list reflects the override