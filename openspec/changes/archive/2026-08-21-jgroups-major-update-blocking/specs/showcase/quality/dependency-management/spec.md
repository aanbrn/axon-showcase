# showcase/quality/dependency-management

## ADDED Requirements

### Requirement: Major-disabled entries are limited to majors the project cannot migrate

The major-disabled configuration SHALL only list coordinates whose major version the project cannot migrate to while
its ecosystem remains on the current major. The shipped list SHALL therefore include `org.jgroups`: JGroups is used
only through Axon's JGroups extension and the `jgroups-kubernetes` (KUBE_PING) discovery, both of which pin JGroups
4.x, so a JGroups 5 migration is not actionable until those components support it. Minor and patch updates for these
coordinates SHALL remain reported.

#### Scenario: JGroups major jump is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of `org.jgroups:jgroups` or
  `org.jgroups.kubernetes:jgroups-kubernetes` whose major exceeds the current major (5.x vs 4.x) is available
- **THEN** the report does not list that major-jump update

#### Scenario: JGroups minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 4.x (same-major) version of `org.jgroups:jgroups` is
  available
- **THEN** the report lists that 4.x update