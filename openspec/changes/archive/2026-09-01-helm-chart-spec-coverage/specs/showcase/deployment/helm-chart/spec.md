## ADDED Requirements

### Requirement: API gateway runtime tuning

The api-gateway Deployment SHALL wire its runtime tuning through environment: the query-service internal URL for read
routing, two Caffeine query caches (the showcase list and showcase-by-id queries) with size and expiry settings, and
the resilience4j environment for the time limiter, circuit breaker, and retry, each with defaults and per-service
command/query overrides.

#### Scenario: Gateway routes reads to the query service

- **WHEN** an api-gateway container is rendered
- **THEN** it receives the internal query-service URL for forwarding read requests

#### Scenario: Query caches are tunable

- **WHEN** an api-gateway container is rendered
- **THEN** it receives the showcase list and showcase-by-id query cache settings (maximum size and expiry after access
  and write)

#### Scenario: Resilience4j is configured with defaults and per-service overrides

- **WHEN** an api-gateway container is rendered
- **THEN** it receives the time limiter, circuit breaker, and retry environment, with default settings and
  command-service and query-service overrides for each

### Requirement: Projection-service projector tuning

The projection-service Deployment SHALL wire its projector processing settings through environment, including the
minimum and maximum concurrency, batch size and time bounds, batch buffer maximum, retry attempts and minimum backoff,
and restart delay.

#### Scenario: Projector processing is tunable

- **WHEN** a projection-service container is rendered
- **THEN** it receives the projector concurrency, batching, buffer, retry, and restart-delay environment

### Requirement: Command-service database pool and scheduler environment

The command-service Deployment SHALL wire its database connection pool and scheduler settings through environment,
including pool maximum size, minimum idle, maximum lifetime, connection, validation, idle, and leak-detection timeouts,
and scheduler threads, heartbeat interval, immediate-execution flag, polling interval and strategy with its limits,
shutdown maximum wait, and delete-unresolved interval.

#### Scenario: Database pool is tunable

- **WHEN** a command-service container is rendered
- **THEN** it receives the database connection-pool environment (size, idle, lifetime, and timeout settings)

#### Scenario: Database scheduler is tunable

- **WHEN** a command-service container is rendered
- **THEN** it receives the database scheduler environment (threads, heartbeat, polling, shutdown, and cleanup settings)

### Requirement: Command-service showcase cache environment

The command-service Deployment SHALL wire the main showcase cache settings through environment, including the maximum
size and expiry after access and write.

#### Scenario: Showcase cache is tunable

- **WHEN** a command-service container is rendered
- **THEN** it receives the showcase cache maximum-size and expiry settings

### Requirement: Metrics tags

Every service Deployment SHALL set the micrometer application tag environment to the release full name, so metrics are
labeled per deployment.

#### Scenario: Application metrics tag is set

- **WHEN** any service container is rendered
- **THEN** it receives the micrometer application tag set to the release full name

### Requirement: Label and annotation merge model

Every chart resource SHALL merge its service-level and common labels and annotations: deployment and pod labels and
annotations from the service's `labels`, `podLabels`, `annotations`, and `podAnnotations` merged with the global
`commonLabels` and `commonAnnotations`, with the standard Kubernetes labels applied and the
`app.kubernetes.io/component` label identifying the service.

#### Scenario: Service labels merge with common labels

- **WHEN** a service resource or pod is rendered
- **THEN** its labels are the service's configured labels merged with `commonLabels`, carrying the standard Kubernetes
  labels and the service's `app.kubernetes.io/component`

#### Scenario: Service annotations merge with common annotations

- **WHEN** a service resource or pod is rendered with annotations configured
- **THEN** its annotations are the service's configured annotations merged with `commonAnnotations`

### Requirement: Horizontal autoscaling details

A service's HPA SHALL scale on CPU utilization by default, and SHALL support an optional memory target, custom metrics,
and scale behavior (scale-up and scale-down policies) when configured.

#### Scenario: HPA scales on CPU and optionally memory

- **WHEN** a service's HPA is enabled
- **THEN** it scales on CPU utilization by default and additionally on memory utilization when a memory target is set

#### Scenario: HPA supports custom metrics and behavior

- **WHEN** a service's HPA is enabled with custom metrics or scale behavior configured
- **THEN** the HPA renders the configured custom metrics and scale-up/scale-down policies

### Requirement: Vertical autoscaling details

A service's VPA SHALL render for the main container with the configured update mode and SHALL support minimum and
maximum allowed resource bounds.

#### Scenario: VPA bounds are configurable

- **WHEN** a service's VPA is enabled
- **THEN** it renders with the configured update mode and, when set, the minimum and maximum allowed resources

### Requirement: Pod disruption budget details

A service's PDB SHALL use the configured `minAvailable` when set, and otherwise fall back to `maxUnavailable`
(default 1).

#### Scenario: PDB prefers minAvailable

- **WHEN** a service's PDB has `minAvailable` set
- **THEN** the PDB sets `minAvailable` rather than the default `maxUnavailable`

### Requirement: ServiceMonitor tunables

A service's ServiceMonitor SHALL scrape the management port at `/actuator/prometheus` with the configured interval and
scrape timeout.

#### Scenario: Scrape interval and timeout are tunable

- **WHEN** a service's ServiceMonitor is rendered
- **THEN** it scrapes the management port at `/actuator/prometheus` with the configured interval and scrape timeout

### Requirement: Ingress extra rules

An api-gateway Ingress SHALL render one rule per configured extra host in addition to the primary hostname rule, all
routing to the api-gateway server port, and SHALL support extra paths on the primary rule.

#### Scenario: Extra hosts get their own rules

- **WHEN** `apiGateway.ingress.enabled` is true and extra hosts are configured
- **THEN** the Ingress renders a rule per extra host routing to the api-gateway server port

### Requirement: HTTPRoute extras

An api-gateway HTTPRoute SHALL render the configured hostnames, parent refs, and matches, SHALL support filters on the
match, and SHALL support extra rules beyond the primary rule, all routing to the api-gateway server port.

#### Scenario: HTTPRoute filters and extra rules are rendered

- **WHEN** `apiGateway.route.enabled` is true and filters or extra rules are configured
- **THEN** the HTTPRoute renders the configured filters and extra rules routing to the api-gateway server port

## MODIFIED Requirements

### Requirement: Network policies

The chart SHALL render a NetworkPolicy per service gated by the service's `networkPolicy.enabled` flag (default true),
restricting ingress to the server and management ports and allowing DNS, same-namespace, OTLP, and Kubernetes API
egress. The `commandService` and `apiGateway` NetworkPolicies SHALL allow egress to the Kubernetes API so JGroups
kube-ping discovery can reach the API server.

#### Scenario: Command and API gateway server ports are open

- **WHEN** the command-service or api-gateway NetworkPolicy is rendered
- **THEN** the server port accepts ingress from any source

#### Scenario: Projection and query server ports are restricted

- **WHEN** the projection-service or query-service NetworkPolicy is rendered
- **THEN** the server port accepts ingress only from pods in the same service

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