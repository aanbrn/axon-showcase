# Spec Delta

## ADDED Requirements

### Requirement: The release workflow publishes the Helm chart

On a release dispatch, the `release` workflow SHALL package the Helm chart and push it to the GitHub Container Registry
as an OCI artifact at `oci://ghcr.io/<owner>/charts/axon-showcase`, versioned with the released version, before it
creates the tag and the GitHub Release. A `dry_run` dispatch SHALL publish the chart under a throwaway version; a
backfill dispatch SHALL publish no chart.

#### Scenario: Manual dispatch publishes the chart

- **WHEN** a maintainer dispatches the `release` workflow from `main` with a valid version
- **THEN** the workflow packages the chart and pushes it to `oci://ghcr.io/<owner>/charts/axon-showcase`

#### Scenario: The published chart version matches the release

- **WHEN** the workflow publishes the chart for version `X.Y.Z`
- **THEN** the chart's version is `X.Y.Z`, so the chart and the release tag name the same version

#### Scenario: A dry run publishes the chart under a throwaway version

- **WHEN** the workflow is dispatched with the dry-run input
- **THEN** the chart is pushed under a throwaway version and no tag or GitHub Release is created

#### Scenario: A backfill publishes no chart

- **WHEN** the workflow is dispatched with a `publish_tag`
- **THEN** no chart is packaged for publishing and none is pushed

#### Scenario: A failed chart publish leaves no tag or release

- **WHEN** the chart packaging or push fails
- **THEN** the run fails without creating the tag `v<version>` or a GitHub Release for it
