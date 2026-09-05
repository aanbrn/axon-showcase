## MODIFIED Requirements

### Requirement: Service deployments

The chart SHALL render one Deployment per service (command-service, query-service, projection-service, api-gateway,
web-ui), each with a single `main` container running the service image, exposing the server, management, and (for
command-service and api-gateway) JGroups ports, and mounting an empty-dir volume at `/tmp`. The web-ui Deployment
runs the static web-UI image on its container port (80) and has no JGroups or management port.

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
- **THEN** the container exposes the server port (default 8080) and management port (default 8888), and the JGroups
  port (default 7800) for command-service and api-gateway; the web-ui container exposes its static-serve port (80)

#### Scenario: Environment comes from values with defaults

- **WHEN** a service Deployment is rendered
- **THEN** it applies `extraEnvVars`, `extraEnvVarsCM`, and `extraEnvVarsSecret`, defaulting `JAVA_OPTS` to
  `-XX:MaxDirectMemorySize=128M -XX:MaxGCPauseMillis=20`; the web-ui container (nginx) needs no `JAVA_OPTS`