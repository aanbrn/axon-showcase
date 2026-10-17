# Spec Delta

## ADDED Requirements

### Requirement: The chart is packaged by the standard check

The standard `check` task SHALL package the chart — the repository setup, the dependency fetch, the strict lint, and the
package step — so a chart whose declared Bitnami `common` dependency cannot be bundled fails the pull request rather
than only the nightly deployment smoke's install. The check SHALL require no Kubernetes cluster.

#### Scenario: The standard check packages the chart

- **WHEN** the standard `check` task runs
- **THEN** the chart's packaging runs and the packaged chart carries its bundled `common` dependency

#### Scenario: A chart that cannot bundle its dependency fails the check

- **WHEN** the chart's declared `common` dependency cannot be bundled, as when the dependency fetch is skipped
- **THEN** the check fails and names the missing dependency

#### Scenario: The check needs no cluster

- **WHEN** the check runs on a machine with the Helm CLI and no Kubernetes cluster
- **THEN** the chart's packaging still runs
