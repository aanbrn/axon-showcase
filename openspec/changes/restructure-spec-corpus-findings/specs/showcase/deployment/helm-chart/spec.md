## REMOVED Requirements

### Requirement: Horizontal autoscaling details

**Reason**: merged into `Horizontal and vertical autoscaling`, which now carries its scenarios; a separate "details"
requirement for the same subject accreted rather than refining the core one.

**Migration**: its behaviours (CPU and optional memory scaling; custom metrics and behavior) are carried by the
`Horizontal and vertical autoscaling` requirement's scenarios.

### Requirement: Vertical autoscaling details

**Reason**: merged into `Horizontal and vertical autoscaling`, as above.

**Migration**: its behaviour (configurable VPA bounds) is carried by the `Horizontal and vertical autoscaling`
requirement's scenarios.

### Requirement: Pod disruption budget details

**Reason**: merged into `Pod disruption budgets`, as above.

**Migration**: its behaviour (PDB preferring `minAvailable`) is carried by the `Pod disruption budgets` requirement's
scenarios.

## MODIFIED Requirements

### Requirement: Horizontal and vertical autoscaling

The chart SHALL render an HPA and a VPA per service, both gated by their `enabled` flags (default false).

A service's HPA SHALL scale on CPU utilization by default, and SHALL support an optional memory target, custom metrics,
and scale behavior (scale-up and scale-down policies) when configured.

A service's VPA SHALL render for the main container with the configured update mode and SHALL support minimum and
maximum allowed resource bounds.

#### Scenario: HPA is opt-in

- **WHEN** a service's HPA is enabled
- **THEN** the chart renders an HPA scaling between the configured minimum and maximum replicas (defaults 3 and 5) on
  CPU utilization (default target 80 percent)

#### Scenario: VPA is opt-in

- **WHEN** a service's VPA is enabled
- **THEN** the chart renders a VPA for the `main` container with the configured resource bounds and update mode (default
  `Auto`)

#### Scenario: HPA scales on CPU and optionally memory

- **WHEN** a service's HPA is enabled
- **THEN** it scales on CPU utilization by default and additionally on memory utilization when a memory target is set

#### Scenario: HPA supports custom metrics and behavior

- **WHEN** a service's HPA is enabled with custom metrics or scale behavior configured
- **THEN** the HPA renders the configured custom metrics and scale-up/scale-down policies

#### Scenario: VPA bounds are configurable

- **WHEN** a service's VPA is enabled
- **THEN** it renders with the configured update mode and, when set, the minimum and maximum allowed resources

### Requirement: Pod disruption budgets

The chart SHALL render a PodDisruptionBudget for each service gated by the service's `pdb.create` flag (default true),
allowing at most one unavailable pod unless `minAvailable` or `maxUnavailable` overrides are set.

A service's PDB SHALL use the configured `minAvailable` when set, and otherwise fall back to `maxUnavailable` (default
1).

#### Scenario: Default PDB allows one unavailable pod

- **WHEN** a service's PDB is created and neither `minAvailable` nor `maxUnavailable` is set
- **THEN** the PDB sets `maxUnavailable` to 1

#### Scenario: PDB prefers minAvailable

- **WHEN** a service's PDB has `minAvailable` set
- **THEN** the PDB sets `minAvailable` rather than the default `maxUnavailable`

## ADDED Requirements

### Requirement: Helm release namespaces are declared in the build

The Helm releases for the local deployment target SHALL declare their namespaces explicitly in `build.gradle.kts`: the
observability releases (kps, tempo) SHALL use the `monitoring` namespace, and the application and infrastructure
releases (db-events, kafka, os-views, axon-showcase) SHALL use a dedicated `axon-showcase` namespace created on install.
The local deployment SHALL NOT depend on the user's kube-context current namespace or a `helm.namespace` gradle property
for the release namespaces.

#### Scenario: All releases declare their namespaces explicitly

- **WHEN** a maintainer reads the Helm release configuration in `build.gradle.kts`
- **THEN** every release sets its `namespace` (kps and tempo in `monitoring`; db-events, kafka, os-views, and
  axon-showcase in `axon-showcase`), and the four app/infra releases set `createNamespace = true`

#### Scenario: The app and infrastructure releases share one namespace

- **WHEN** the four app/infra releases are installed
- **THEN** they are created in the `axon-showcase` namespace, so their short service DNS names resolve within it, and
  the namespace is created if absent

### Requirement: Each Helm release target declares its kube context

Every Helm release target SHALL declare the kube context it deploys to in the build's `releaseTargets` configuration.
The `local` target SHALL resolve its kube context per-machine, from a `helm.local.kubeContext` Gradle property, falling
back to the developer's current kube context when unset. Any remote target (e.g. staging) SHALL declare a shared, fixed
kube context in the build, since the same remote cluster serves every contributor. The repo SHALL NOT hard-code a
machine-specific local context name (such as a macOS-only colima context) in the versioned build.

#### Scenario: The local target resolves the developer's local cluster

- **WHEN** a developer runs a Helm install with the `local` release target active
- **THEN** the target uses the `helm.local.kubeContext` property if set, or the developer's current kube context
  otherwise, so macOS (colima) and Linux (kind/minikube) contributors each deploy to their own local cluster

#### Scenario: A remote target uses a shared fixed context

- **WHEN** a release target other than `local` (e.g. staging) is active
- **THEN** the target deploys to the kube context declared for it in the build, which is the same for every contributor

#### Scenario: No machine-specific context name in the repo

- **WHEN** a maintainer reads the `releaseTargets` configuration in `build.gradle.kts`
- **THEN** the `local` target does not hard-code a context name that only exists on one OS (such as `colima`), and any
  per-machine context value is supplied via the `helm.local.kubeContext` property
