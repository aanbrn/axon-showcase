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
- **THEN** the management port accepts ingress only from same-service pods and, when `addExternalClientAccess` is
  enabled, from pods labeled as clients and any configured management pods

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