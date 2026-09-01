# showcase/helm-chart Specification

## Purpose
Documents the current behavior of the Helm chart that deploys the four showcase services, their database migration and
index initialization jobs, RBAC, network policies, autoscaling, and observability wiring.

## Requirements

### Requirement: Chart metadata

The chart SHALL be an apiVersion `v2` Helm chart named after the project, with a version and an application version, and
SHALL declare the Bitnami `common` chart as a dependency.

#### Scenario: Chart declares Bitnami common dependency

- **WHEN** the chart is packaged
- **THEN** it declares a dependency on the Bitnami `common` chart

### Requirement: Service deployments

The chart SHALL render one Deployment per service (command-service, query-service, projection-service, api-gateway),
each with a single `main` container running the service image, exposing the server, management, and (for command-service
and api-gateway) JGroups ports, and mounting an empty-dir volume at `/tmp`.

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
  port (default 7800) for command-service and api-gateway

#### Scenario: Environment comes from values with defaults

- **WHEN** a service Deployment is rendered
- **THEN** it applies `extraEnvVars`, `extraEnvVarsCM`, and `extraEnvVarsSecret`, defaulting `JAVA_OPTS` to
  `-XX:MaxDirectMemorySize=128M -XX:MaxGCPauseMillis=20`

### Requirement: Health and readiness probes

Each service Deployment SHALL configure startup, liveness, and readiness probes on the management port against the
Spring Boot actuator endpoints, all gated by their `enabled` flags (default true).

#### Scenario: Startup probe checks the health endpoint

- **WHEN** a service's startup probe is enabled
- **THEN** the container is probed with `GET /actuator/health` on the management port

#### Scenario: Liveness probe checks the liveness endpoint

- **WHEN** a service's liveness probe is enabled
- **THEN** the container is probed with `GET /actuator/health/liveness` on the management port

#### Scenario: Readiness probe checks the readiness endpoint

- **WHEN** a service's readiness probe is enabled
- **THEN** the container is probed with `GET /actuator/health/readiness` on the management port

### Requirement: Security contexts

Each service pod and container SHALL run with hardened security contexts when enabled (default true): the pod with
`fsGroup` 1001 and `Always` fsGroup change policy, and the container as a non-root user with a read-only root filesystem,
all Linux capabilities dropped, and the `RuntimeDefault` seccomp profile.

#### Scenario: Container drops all capabilities

- **WHEN** a service's container security context is enabled
- **THEN** the container runs as non-root, with a read-only root filesystem, no Linux capabilities, and the
  `RuntimeDefault` seccomp profile

### Requirement: Per-service services

The chart SHALL render a Service for each service exposing the server and management ports, except command-service
SHALL expose only the management port.

#### Scenario: Command service is not directly reachable

- **WHEN** the command-service Service is rendered
- **THEN** it exposes only the management port and no server port

#### Scenario: Other services expose server and management ports

- **WHEN** a Service for query-service, projection-service, or api-gateway is rendered
- **THEN** it exposes both the server port and the management port

### Requirement: Database migration job

The chart SHALL render a Helm `pre-install,pre-upgrade` hook Job that runs the command-service image with
`EXIT_AFTER_FLYWAY_MIGRATION=true` and the database connection environment, executing Flyway migrations against the
PostgreSQL event store and exiting successfully.

#### Scenario: Migration job runs before install and upgrade

- **WHEN** the chart is installed or upgraded
- **THEN** the command-service database migration Job runs first with hook annotations `pre-install,pre-upgrade` and is
  deleted after success

#### Scenario: Migration job connects to the configured database

- **WHEN** the migration Job runs
- **THEN** it receives the database hosts, name, schema, params, user, and password (from the password secret) and exits
  after Flyway migrations complete

#### Scenario: Failed migration pods are restarted

- **WHEN** a migration Job pod fails
- **THEN** it is restarted with `restartPolicy: OnFailure`

### Requirement: Index initialization job

The chart SHALL render a Helm `pre-install,pre-upgrade` hook Job that runs the query-service image with
`EXIT_AFTER_INDEX_INITIALIZATION=true` and the OpenSearch connection environment, initializing the index and exiting
successfully.

#### Scenario: Index initialization runs before install and upgrade

- **WHEN** the chart is installed or upgraded
- **THEN** the query-service index-initialization Job runs first with hook annotations `pre-install,pre-upgrade` and is
  deleted after success

#### Scenario: Index initialization connects to the configured OpenSearch

- **WHEN** the index-initialization Job runs
- **THEN** it receives the OpenSearch URIs and, when OpenSearch is secured, the password from the password secret, and
  exits after index initialization completes

### Requirement: Pod disruption budgets

The chart SHALL render a PodDisruptionBudget for each service gated by the service's `pdb.create` flag (default true),
allowing at most one unavailable pod unless `minAvailable` or `maxUnavailable` overrides are set.

#### Scenario: Default PDB allows one unavailable pod

- **WHEN** a service's PDB is created and neither `minAvailable` nor `maxUnavailable` is set
- **THEN** the PDB sets `maxUnavailable` to 1

### Requirement: Horizontal and vertical autoscaling

The chart SHALL render an HPA and a VPA per service, both gated by their `enabled` flags (default false).

#### Scenario: HPA is opt-in

- **WHEN** a service's HPA is enabled
- **THEN** the chart renders an HPA scaling between the configured minimum and maximum replicas (defaults 3 and 5) on CPU
  utilization (default target 80 percent)

#### Scenario: VPA is opt-in

- **WHEN** a service's VPA is enabled
- **THEN** the chart renders a VPA for the `main` container with the configured resource bounds and update mode
  (default `Auto`)

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

### Requirement: JGroups clustering

The chart SHALL enable JGroups clustering with kube-ping discovery for command-service and api-gateway only: their
pods carry the `jgroups-cluster: axon-showcase` label and receive the JGroups configuration, bind port, namespace, and
labels environment.

#### Scenario: JGroups pods carry the cluster label

- **WHEN** a command-service or api-gateway pod is rendered
- **THEN** it carries the label `jgroups-cluster: axon-showcase`

#### Scenario: JGroups environment is configured

- **WHEN** a command-service or api-gateway container is rendered
- **THEN** it receives the kube-ping config file, bind port, namespace, and labels environment

### Requirement: Service account and RBAC

The chart SHALL render a ServiceAccount gated by `serviceAccount.create` (default true) and a Role and RoleBinding
gated by `rbac.create` (default true), granting the service account permission to get and list pods for JGroups
discovery.

#### Scenario: Role grants pod discovery

- **WHEN** RBAC is created
- **THEN** the chart renders a Role permitting `get` and `list` on `pods` and a RoleBinding to the service account

#### Scenario: Service account is created by default

- **WHEN** `serviceAccount.create` is true
- **THEN** the chart renders a ServiceAccount with the configured name and automount settings

### Requirement: API gateway ingress and route

The chart SHALL render an Ingress and an HTTPRoute for the api-gateway, each gated by its `enabled` flag (default
false), routing to the api-gateway server port.

#### Scenario: Ingress is opt-in

- **WHEN** `apiGateway.ingress.enabled` is true
- **THEN** the chart renders an Ingress with the configured ingress class, hostname, path (default `/`), and path type
  (default `Prefix`), routing to the api-gateway server port

#### Scenario: Route is opt-in

- **WHEN** `apiGateway.route.enabled` is true
- **THEN** the chart renders an HTTPRoute with the configured hostnames, parent refs, and matches (default a `PathPrefix`
  of `/`), routing to the api-gateway server port

### Requirement: Connection settings

The chart SHALL configure each service's connection to its dependencies through environment: the command-service to
PostgreSQL (default `axon-showcase-db-events` / `showcase-events` with user `showcase`) and Kafka (default
`axon-showcase-kafka:9092`, topic `axon-showcase-events`, PLAINTEXT), and the projection and query services to
OpenSearch (default `http://axon-showcase-os-views:9200`, unsecured).

#### Scenario: Command service connects to the event store database

- **WHEN** a command-service container is rendered
- **THEN** it receives the database hosts, name, schema, user, and password-from-secret environment and disables
  in-app Flyway (`FLYWAY_MIGRATION_ENABLED=false`) since migrations run as a hook Job

#### Scenario: Projection and query services connect to OpenSearch

- **WHEN** a projection-service or query-service container is rendered
- **THEN** it receives the OpenSearch URIs, timeout, keep-alive, connection pool, and idle-eviction environment, plus
  the password from the secret when OpenSearch is secured

#### Scenario: Kafka connection is shared by command and projection services

- **WHEN** a command-service or projection-service container is rendered
- **THEN** it receives the Kafka bootstrap servers, events topic, security protocol, and producer retries environment

### Requirement: Observability wiring

The chart SHALL wire observability through environment: sampling probability and structured logging, Prometheus metrics
export, and OTLP metrics and tracing export, each gated on the respective feature being enabled, with validation of the
configured endpoints.

#### Scenario: Sampling probability is set when observability is enabled

- **WHEN** any observability feature is enabled and a service container is rendered
- **THEN** it receives `MANAGEMENT_TRACING_SAMPLING_PROBABILITY` (default 0.1)

#### Scenario: Prometheus metrics gated and servicemonitors rendered

- **WHEN** `observability.prometheus.metrics.export.enabled` is true and a service's `serviceMonitor.enabled` is true
- **THEN** the chart renders a ServiceMonitor for the service scraping the management port at `/actuator/prometheus`

#### Scenario: OTLP metrics export requires a metrics endpoint

- **WHEN** OTLP metrics export is enabled
- **THEN** the chart validates that the endpoint is http(s) and ends with `:4318`, and renders the export URL under
  `/v1/metrics` and the step

#### Scenario: OTLP tracing export requires a compatible endpoint

- **WHEN** OTLP tracing export is enabled
- **THEN** the chart validates that the endpoint is http(s) on port `4317` or `4318`, selects the transport
  accordingly (grpc or http), and renders the endpoint, transport, and compression

### Requirement: Extra deployments and dashboards

The chart SHALL render extra deployments from `extraDeploy` verbatim, and SHALL render a ConfigMap per bundled Grafana
dashboard under the `grafana_dashboard: "1"` label so dashboards auto-provision.

#### Scenario: Extra resources are rendered from extraDeploy

- **WHEN** `extraDeploy` is set
- **THEN** each entry is rendered verbatim (templates allowed) as its own YAML document

#### Scenario: Grafana dashboards are provisioned as ConfigMaps

- **WHEN** the chart is rendered
- **THEN** a ConfigMap is created for the bundled dashboard carrying the `grafana_dashboard: "1"` label and the
  dashboard JSON as data

### Requirement: Chart linting

The chart SHALL be linted with `helm lint` in strict mode, treating warnings as errors, and SHALL also lint the
Bitnami `common` subchart.

#### Scenario: Chart lints clean in strict mode

- **WHEN** the chart is linted
- **THEN** `helm lint` runs with warnings treated as errors and reports no failures for the chart or its subcharts

### Requirement: Lint configurations cover template branches

The chart SHALL be linted with a `full` configuration that enables every optional feature and a `minimal`
configuration that disables the default-on features, so all conditional template branches are rendered during lint.

#### Scenario: Full configuration renders optional branches

- **WHEN** the chart is linted with the `full` configuration
- **THEN** the optional template branches are rendered, including ingress, HTTPRoute, VPA and HPA, secured
  OpenSearch, observability, extraDeploy, RBAC rules, NetworkPolicy extras, and ServiceMonitor tuning

#### Scenario: Minimal configuration renders disabled branches

- **WHEN** the chart is linted with the `minimal` configuration
- **THEN** the templates render with the default-on features disabled, including ServiceAccount, RBAC,
  NetworkPolicies, PDBs, ServiceMonitors, probes, autoscaling, and observability

### Requirement: Command-service saga and snapshot environment

The chart SHALL wire the command-service saga cache, saga associations cache, and showcase snapshot trigger settings
from values to environment variables, mirroring the existing `showcaseCache` wiring, so the settings are tunable per
deployment.

#### Scenario: Saga cache settings are passed as environment

- **WHEN** a command-service Deployment is rendered
- **THEN** it receives the saga cache settings as the `SAGA_CACHE_MAX_SIZE`, `SAGA_CACHE_EXPIRES_AFTER_ACCESS`, and
  `SAGA_CACHE_EXPIRES_AFTER_WRITE` environment variables from the `commandService.sagaCache` values

#### Scenario: Saga associations cache settings are passed as environment

- **WHEN** a command-service Deployment is rendered
- **THEN** it receives the saga associations cache settings as the `SAGA_ASSOCIATIONS_CACHE_MAX_SIZE`,
  `SAGA_ASSOCIATIONS_CACHE_EXPIRES_AFTER_ACCESS`, and `SAGA_ASSOCIATIONS_CACHE_EXPIRES_AFTER_WRITE` environment
  variables from the `commandService.sagaAssociationsCache` values

#### Scenario: Showcase snapshot trigger settings are passed as environment

- **WHEN** a command-service Deployment is rendered
- **THEN** it receives the showcase snapshot trigger setting as the `SHOWCASE_SNAPSHOT_TRIGGER_LOAD_TIME_THRESHOLD`
  environment variable from the `commandService.showcaseSnapshotTrigger` values

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
