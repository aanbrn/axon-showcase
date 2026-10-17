# Proposal

## Why

Nothing in the standard `check` task packages the Helm chart. The packaging path that bundles the chart's Bitnami
`common` dependency therefore never runs on a pull request, so a dependency-fetch regression — such as the
configuration-cache-dependent skip of `helmUpdateMainChartDependencies` introduced with the cache (#581) — reaches
`main` and surfaces only in the nightly deployment smoke, days after the pull request that broke it. Running the chart's
packaging in `check` fails that pull request instead.

## What Changes

- `build.gradle.kts`: the standard `check` task gains a dependency on `:helm:chart:helmPackageMainChart`, so the chart's
  packaging path runs on every pull request.
- Add a requirement to the `showcase/deployment/helm-chart` spec: the standard `check` task packages the chart and fails
  when the chart cannot bundle the `common` dependency it declares.
- `AGENTS.md`'s `check` note and its chart-validation bullet name the new member.
- Remove the idea this change implements from `docs/ideas.md`.
- Record the `showcase/deployment/helm-chart` Purpose refresh for the archive commit, since a delta cannot carry a
  Purpose.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/deployment/helm-chart`: adds a requirement that the standard `check` task packages the chart and fails when
  the chart cannot bundle its declared dependency.

## Impact

- Build: the standard `check` task gains the chart's packaging, which the release workflow already runs. A pull request
  that breaks packaging now fails the pull-request gate rather than only the nightly deployment smoke's install.
- Tests: no committed test is added; the gate's decision is helm's own dependency validation, not repository-authored
  logic (see `design.md` — Decisions).
- Deployment: no runtime behavior changes — the chart, its templates, and its released artifact are unchanged.
- Docs: `AGENTS.md`'s `check` note and chart-validation bullet describe the new member; the `helm-chart` Purpose gains
  it at archive; the parked idea in `docs/ideas.md` is removed by this change.
