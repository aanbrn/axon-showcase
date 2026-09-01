## ADDED Requirements

### Requirement: Helm release namespaces are declared in the build

The Helm releases for the local deployment target SHALL declare their namespaces explicitly in `build.gradle.kts`: the
observability releases (kps, tempo) SHALL use the `monitoring` namespace, and the application and infrastructure
releases (db-events, kafka, os-views, axon-showcase) SHALL use a dedicated `axon-showcase` namespace created on
install. The local deployment SHALL NOT depend on the user's kube-context current namespace or a `helm.namespace`
gradle property for the release namespaces.

#### Scenario: All releases declare their namespaces explicitly

- **WHEN** a maintainer reads the Helm release configuration in `build.gradle.kts`
- **THEN** every release sets its `namespace` (kps and tempo in `monitoring`; db-events, kafka, os-views, and
  axon-showcase in `axon-showcase`), and the four app/infra releases set `createNamespace = true`

#### Scenario: The app and infrastructure releases share one namespace

- **WHEN** the four app/infra releases are installed
- **THEN** they are created in the `axon-showcase` namespace, so their short service DNS names resolve within it, and
  the namespace is created if absent