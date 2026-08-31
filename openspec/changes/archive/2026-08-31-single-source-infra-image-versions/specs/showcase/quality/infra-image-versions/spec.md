## Purpose

Keeps the infrastructure components (PostgreSQL, Kafka, OpenSearch) single-sourced: the test surfaces (docker-compose,
Testcontainers) pin the official image tags in the version catalog, the deployment pins the Bitnami Helm chart versions,
and a build gate ensures the Bitnami image tag preconfigured in each pinned chart agrees with the image version used in
tests.

## ADDED Requirements

### Requirement: Infrastructure image references are single-sourced

The version catalog SHALL declare, for each infrastructure component used across deployment, local dev, and tests —
PostgreSQL, Kafka, and OpenSearch — the concrete official Docker Hub image tag (`*-image-tag`) used by docker-compose
and Testcontainers, and the Bitnami Helm chart version (`bitnami-*`) used by the Helm charts. No surface SHALL
hard-code an independent version.

#### Scenario: All surfaces resolve from the catalog

- **WHEN** a maintainer looks up the PostgreSQL, Kafka, or OpenSearch image used by the Helm chart, docker-compose, or
  Testcontainers
- **THEN** the test surfaces resolve to the corresponding `*-image-tag` coordinate, and the Helm charts resolve to the
  pinned `bitnami-*` chart version (whose preconfigured `image.tag` the charts deploy)

#### Scenario: Bumping a version updates its surface

- **WHEN** a `*-image-tag` coordinate for a component is bumped
- **THEN** docker-compose and Testcontainers reference the new tag
- **WHEN** a `bitnami-*` chart version for a component is bumped
- **THEN** the Helm chart resolves to the new chart version, which carries its own preconfigured `image.tag`

### Requirement: The deployed Bitnami image tag is the chart's preconfigured tag

The Helm deployment SHALL use the Bitnami image tag preconfigured in the pinned chart version — the infra releases
SHALL NOT override `image.tag` in build logic or values files. The chart version is the single source for the deployed
Bitnami image tag.

#### Scenario: The chart's preconfigured tag is deployed

- **WHEN** the Helm chart for a component is installed at its pinned version
- **THEN** the deployed image tag is the tag preconfigured in that chart version (e.g. postgresql chart 16.7.27 ships
  `17.6.0-debian-12-r4`), not a separately maintained override

#### Scenario: A values file attempts to override the image tag

- **WHEN** a values file for an infra release (e.g. `helm/values/axon-showcase-kafka/values-local.yaml`) pins
  `image.tag`
- **THEN** `check` fails, because the Helm deployment must use the chart's preconfigured image tag rather than a
  separately maintained override

### Requirement: Drift between surfaces is caught by the build

The build SHALL verify, as part of `check`, that each component's pinned chart version carries a preconfigured Bitnami
`image.tag` whose leading numeric version segment, after stripping trailing `.0` zero-padding segments, equals the
`*-image-tag`'s leading numeric version segment after the same stripping, and SHALL fail the build when they diverge.
Zero-padding differences (e.g. postgres official `17.6` vs chart app version `17.6.0`, or `17` vs `17.0.0`) are
equivalent; a genuine patch difference is not. The verification resolves the chart's preconfigured tag from the chart
repository at the pinned version, so it requires the Helm CLI (the plugin-managed client) and network access to the
chart repository.

#### Scenario: Image and chart-preconfigured tags agree

- **WHEN** each pinned chart's preconfigured `image.tag` leading numeric version segment equals the `*-image-tag`'s
  leading numeric version segment after stripping trailing `.0` zero-padding from both
- **THEN** `check` passes without a drift error

#### Scenario: Image and chart-preconfigured tags disagree

- **WHEN** a pinned chart's preconfigured `image.tag` leading numeric version segment differs from the corresponding
  `*-image-tag`'s leading numeric version segment after stripping trailing `.0` zero-padding from both
- **THEN** `check` fails with a message naming the component and the mismatch

### Requirement: The drift gate follows the configured releases

The drift verification SHALL derive the set of infra checks from the actual Helm releases configured in the build
(their chart references, chart versions, and values directories), rather than from a separately maintained list. A
release that is renamed, moved, or removed SHALL automatically retarget the gate: a renamed release is still verified
against its new name and values directory, and a removed release drops its check.

#### Scenario: An infra release is renamed

- **WHEN** an infra release (and its values directory) is renamed
- **THEN** `check` still verifies the renamed release's chart-preconfigured image tag against the `*-image-tag` (after
  stripping trailing `.0` zero-padding from both), using the release's own chart reference, version, and values
  directory

#### Scenario: An infra release is removed

- **WHEN** an infra release is removed from the Helm configuration
- **THEN** its infra check is no longer derived, and `check` does not report a stale check for it