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
  browser calls; empty default = same-origin), rendered to `config.js` at container start — the chart never bakes a
  per-environment base URL into the image

### Requirement: Per-service services

The chart SHALL render a Service for each service exposing the server and management ports, except command-service SHALL
expose only the management port and query-service SHALL expose the management and gRPC ports.

#### Scenario: Command service is not directly reachable

- **WHEN** the command-service Service is rendered
- **THEN** it exposes only the management port and no server port

#### Scenario: Other services expose server and management ports

- **WHEN** a Service for projection-service or api-gateway is rendered
- **THEN** it exposes both the server port and the management port

#### Scenario: The query service exposes the management and gRPC ports

- **WHEN** the query-service Service is rendered
- **THEN** it exposes the management port and the gRPC port

### Requirement: Network policies

The chart SHALL render a NetworkPolicy per service gated by the service's `networkPolicy.enabled` flag (default true),
restricting ingress to the server and management ports (for query-service, the gRPC and management ports) and allowing
DNS, same-namespace, OTLP, and Kubernetes API egress. The `commandService` and `apiGateway` NetworkPolicies SHALL allow
egress to the Kubernetes API so JGroups kube-ping discovery can reach the API server.

#### Scenario: Command and API gateway server ports are open

- **WHEN** the command-service or api-gateway NetworkPolicy is rendered
- **THEN** the server port accepts ingress from any source

#### Scenario: Projection and query server ports are restricted

- **WHEN** the projection-service or query-service NetworkPolicy is rendered
- **THEN** the server port (projection-service) or the gRPC port (query-service) accepts ingress only from pods in the
  same service

#### Scenario: Management port is restricted

- **WHEN** any service NetworkPolicy is rendered
- **THEN** the management port accepts ingress only from same-service pods and, when configured, from pods carrying the
  release client label (`addExternalClientAccess`), pods matching `ingressPodMatchLabels`, pods in namespaces and pods
  matching `ingressManagementNSMatchLabels`/`ingressManagementNSPodMatchLabels`, and any extra ingress rules

#### Scenario: JGroups traffic is limited to the cluster

- **WHEN** the command-service or api-gateway NetworkPolicy is rendered
- **THEN** the JGroups port accepts ingress only from same-service pods carrying the `jgroups-cluster: axon-showcase`
  label

#### Scenario: DNS and same-namespace egress are allowed

- **WHEN** any service NetworkPolicy is rendered
- **THEN** egress is allowed for DNS and within the release namespace

#### Scenario: Kubernetes API egress enables kube-ping discovery

- **WHEN** the command-service or api-gateway NetworkPolicy is rendered
- **THEN** egress is allowed to the Kubernetes API via an `ipBlock` for the apiserver address on the apiserver port,
  discovered from the `default` namespace's `kubernetes` EndpointSlice, so JGroups kube-ping can discover cluster
  members

### Requirement: API gateway runtime tuning

The api-gateway Deployment SHALL wire its runtime tuning through environment: the query-service internal gRPC target for
read routing, two Caffeine query caches (the showcase list and showcase-by-id queries) with size and expiry settings,
the live event stream's keep-alive interval, and the resilience4j environment for the time limiter, circuit breaker, and
retry, each with defaults and per-service command/query overrides.

#### Scenario: Gateway routes reads to the query service

- **WHEN** an api-gateway container is rendered
- **THEN** it receives the internal query-service gRPC target for forwarding read requests

#### Scenario: Query caches are tunable

- **WHEN** an api-gateway container is rendered
- **THEN** it receives the showcase list and showcase-by-id query cache settings (maximum size and expiry after access
  and write)

#### Scenario: The live stream keep-alive interval is tunable

- **WHEN** an api-gateway container is rendered
- **THEN** it receives the live event stream's keep-alive interval

#### Scenario: Resilience4j is configured with defaults and per-service overrides

- **WHEN** an api-gateway container is rendered
- **THEN** it receives the time limiter, circuit breaker, and retry environment, with default settings and
  command-service and query-service overrides for each
