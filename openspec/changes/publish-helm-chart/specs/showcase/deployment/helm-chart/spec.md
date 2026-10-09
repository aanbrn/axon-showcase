# Spec Delta

## ADDED Requirements

### Requirement: The chart defaults its service images to the published registry

The chart SHALL default each service's `image.registry` to `ghcr.io`, so an install with the chart's default values
resolves each service image from the GitHub Container Registry. The local and `ci` values SHALL set each service's
registry empty so those targets resolve their images from the local daemon. The web UI's metrics-exporter sidecar is not
a service image and SHALL keep its own registry (Docker Hub), which the empty global registry preserves.

#### Scenario: A default install pulls the published images

- **WHEN** the chart is installed with its default values
- **THEN** each service image is resolved from `ghcr.io`

#### Scenario: The metrics-exporter sidecar keeps its own registry

- **WHEN** the web UI's metrics-exporter sidecar is rendered
- **THEN** its image is resolved from its own registry (Docker Hub), not `ghcr.io`

#### Scenario: The local and ci targets use local images

- **WHEN** the local or `ci` values are applied
- **THEN** each service's registry is empty and each service image is resolved from the local daemon rather than a
  registry

## MODIFIED Requirements

### Requirement: Service deployments

The chart SHALL render one Deployment per service (command-service, query-service, projection-service, api-gateway,
web-ui), each with a single `main` container running the service image, exposing the server and management ports, the
JGroups ports for command-service and api-gateway, and, for query-service, the management and gRPC ports (the query
service has no HTTP API, so its HTTP server is its management server), and mounting an empty-dir volume at `/tmp`. The
web-ui Deployment runs the static web-UI image on its container port (8080) and has no JGroups or management port.

#### Scenario: Deployment replicas are taken from the replica count

- **WHEN** a service's HPA is not enabled
- **THEN** the Deployment sets `replicas` to the service's `replicaCount` (default 1)

#### Scenario: HPA-enabled services omit replicas

- **WHEN** a service's HPA is enabled
- **THEN** the Deployment omits `replicas` so the HPA controls the scale

#### Scenario: Images are configurable per service

- **WHEN** a service Deployment is rendered
- **THEN** the image is resolved from the service image registry (or the global image registry), repository, and tag,
  with an explicit pull policy, or `Always` for `latest` tags and `IfNotPresent` otherwise

#### Scenario: Container ports are exposed per service

- **WHEN** a service Deployment is rendered
- **THEN** the container exposes the server port (default 8080) and management port (default 8888), the JGroups port
  (default 7800) for command-service and api-gateway, and the management (default 8888) and gRPC (default 9090) ports
  for query-service (which exposes no server port); the web-ui container exposes its static-serve port (8080)

#### Scenario: Environment comes from values with defaults

- **WHEN** a service Deployment is rendered
- **THEN** it applies `extraEnvVars`, `extraEnvVarsCM`, and `extraEnvVarsSecret`, defaulting `JAVA_OPTS` to
  `-XX:MaxDirectMemorySize=128M -XX:MaxGCPauseMillis=20`; the web-ui container (nginx) needs no `JAVA_OPTS`

#### Scenario: The web-UI Deployment configures the API base URL from values

- **WHEN** a web-UI Deployment is rendered
- **THEN** it sets the `SHOWCASE_API_BASE_URL` env var from `webUi.apiBaseUrl` (the externally-visible gateway URL the
  browser calls; an empty value makes the container fail fast, so an install must set it), rendered to `config.js` at
  container start — the chart never bakes a per-environment base URL into the image
