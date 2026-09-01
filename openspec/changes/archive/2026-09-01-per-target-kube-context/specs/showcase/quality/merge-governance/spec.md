## ADDED Requirements

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
