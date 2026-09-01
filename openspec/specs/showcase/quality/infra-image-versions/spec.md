# Infra Image Versions Specification

## Purpose

Keeps the infrastructure components (PostgreSQL, Kafka, OpenSearch) single-sourced: the test surfaces (docker-compose,
Testcontainers) pin the official image tags in the version catalog, the deployment pins the Bitnami Helm chart versions,
and a build gate ensures the Bitnami image tag preconfigured in each pinned chart agrees with the image version used in
tests.

## Requirements

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
`image.tag` whose leading numeric version, truncated to the number of numeric segments in the official `*-image-tag`,
equals the official `*-image-tag`'s leading numeric version, and SHALL fail the build when they diverge. The official
tag is never mutated: a two-segment official tag (`17.6`) matches a chart app version `17.6.0` because the chart is
truncated to two segments, while a full-patch official tag (`3.9.0`) requires an exact chart app version match
(`3.9.0`); a genuine patch difference still fails. The official `*-image-tag` SHALL declare at least a minor version
(two numeric segments): a bare-major tag such as `17` is a floating reference (Docker Hub re-points `postgres:17` to
the latest 17.x) and SHALL be rejected, because a floating tag cannot be a single source of truth for the version used
in tests. The verification resolves the chart's preconfigured tag from the chart repository at the pinned version, so
it requires the Helm CLI (the plugin-managed client) and network access to the chart repository. The verification
SHALL be cacheable on its inputs (the pinned coordinates and the infra values files): when none of them change, `check`
SHALL NOT re-run the Helm resolution, because a pinned chart version's preconfigured `image.tag` is immutable and a
cached result remains valid until a coordinate or values file changes.

#### Scenario: Image and chart-preconfigured tags agree

- **WHEN** a pinned chart's preconfigured `image.tag` leading numeric version, truncated to the official `*-image-tag`'s
  segment count, equals the official `*-image-tag`'s leading numeric version
- **THEN** `check` passes without a drift error (e.g. official `17.6` with chart app version `17.6.0`, or official
  `3.9.0` with chart app version `3.9.0`)

#### Scenario: Image and chart-preconfigured tags disagree

- **WHEN** a pinned chart's preconfigured `image.tag` leading numeric version, truncated to the official `*-image-tag`'s
  segment count, differs from the official `*-image-tag`'s leading numeric version
- **THEN** `check` fails with a message naming the component and the mismatch

#### Scenario: A bare-major official tag is rejected

- **WHEN** an official `*-image-tag` carries fewer than two numeric version segments (e.g. `17`)
- **THEN** `check` fails with a message stating that the tag is a floating reference and must declare at least the
  minor version

#### Scenario: Unchanged inputs skip the Helm resolution

- **WHEN** `check` runs and the pinned coordinates and infra values files are unchanged from a previous run
- **THEN** the drift verification is restored from cache and the Helm client download, repository update, and chart
  resolution do not re-run

#### Scenario: A changed coordinate re-verifies

- **WHEN** a pinned coordinate or an infra values file changes
- **THEN** the drift verification re-runs the Helm resolution against the changed inputs

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