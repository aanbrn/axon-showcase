## REMOVED Requirements

### Requirement: Helm release namespaces are declared in the build

**Reason**: describes the Gradle Helm-release configuration (a deployment surface), not how changes land on `main`,
which is this capability's scope; moved to `deployment/helm-chart`.

**Migration**: the requirement is unchanged; it now lives in `showcase/deployment/helm-chart`.

### Requirement: Each Helm release target declares its kube context

**Reason**: describes the Gradle Helm-release configuration (a deployment surface), not how changes land on `main`,
which is this capability's scope; moved to `deployment/helm-chart`.

**Migration**: the requirement is unchanged; it now lives in `showcase/deployment/helm-chart`.
