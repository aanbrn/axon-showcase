## MODIFIED Requirements

### Requirement: Drift between surfaces is caught by the build

The build SHALL verify, as part of `check`, that each component's pinned chart version carries a preconfigured Bitnami
`image.tag` whose leading numeric version segment, after stripping trailing `.0` zero-padding segments, equals the
`*-image-tag`'s leading numeric version segment after the same stripping, and SHALL fail the build when they diverge.
Zero-padding differences (e.g. postgres official `17.6` vs chart app version `17.6.0`, or `17` vs `17.0.0`) are
equivalent; a genuine patch difference is not. The verification resolves the chart's preconfigured tag from the chart
repository at the pinned version, so it requires the Helm CLI (the plugin-managed client) and network access to the
chart repository. The verification SHALL be cacheable on its inputs (the pinned coordinates and the infra values
files): when none of them change, `check` SHALL NOT re-run the Helm resolution, because a pinned chart version's
preconfigured `image.tag` is immutable and a cached result remains valid until a coordinate or values file changes.

#### Scenario: Image and chart-preconfigured tags agree

- **WHEN** each pinned chart's preconfigured `image.tag` leading numeric version segment equals the `*-image-tag`'s
  leading numeric version segment after stripping trailing `.0` zero-padding from both
- **THEN** `check` passes without a drift error

#### Scenario: Image and chart-preconfigured tags disagree

- **WHEN** a pinned chart's preconfigured `image.tag` leading numeric version segment differs from the corresponding
  `*-image-tag`'s leading numeric version segment after stripping trailing `.0` zero-padding from both
- **THEN** `check` fails with a message naming the component and the mismatch

#### Scenario: Unchanged inputs skip the Helm resolution

- **WHEN** `check` runs and the pinned coordinates and infra values files are unchanged from a previous run
- **THEN** the drift verification is restored from cache and the Helm client download, repository update, and chart
  resolution do not re-run

#### Scenario: A changed coordinate re-verifies

- **WHEN** a pinned coordinate or an infra values file changes
- **THEN** the drift verification re-runs the Helm resolution against the changed inputs