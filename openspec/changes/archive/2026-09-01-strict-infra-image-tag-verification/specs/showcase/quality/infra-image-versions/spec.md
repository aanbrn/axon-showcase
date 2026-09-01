## MODIFIED Requirements

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