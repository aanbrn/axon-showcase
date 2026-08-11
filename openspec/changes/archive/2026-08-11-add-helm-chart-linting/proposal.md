## Why

The `helm:chart` module is validated only at package/install time against a live cluster. Template errors in
optional, non-default branches (ingress, HTTPRoute, VPA/HPA, secured OpenSearch, observability, ServiceMonitor
tuning) are not caught by the default build, so a broken conditional can ship unnoticed. `helm lint` renders every
template with the supplied values and checks the resulting YAML for correctness, giving a fast, cluster-free gate.

## What Changes

- Enable strict linting of the `main` chart in `helm/chart/build.gradle.kts` (`helmLintMainChart`), treating
  linter warnings as errors and linting the Bitnami `common` subchart.
- Add two lint configurations that render the chart with alternate value sets so every conditional template branch
  is exercised:
  - `full` (`src/test/helm/helm-lint-full.yaml`): enables all optional features (ingress, HTTPRoute, VPA/HPA,
    secured OpenSearch, observability, extraDeploy, RBAC rules, NetworkPolicy extras, ServiceMonitor tuning,
    extra env/ports, PDB `minAvailable`).
  - `minimal` (`src/test/helm/helm-lint-minimal.yaml`): disables the default-on features (ServiceAccount, RBAC,
    NetworkPolicies, PDBs, ServiceMonitors, probes, autoscaling, observability).
- No chart templates or rendered behavior change; this is build-time verification only.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/helm-chart`: adds a requirement that the chart lints clean under strict mode with the `full` and
  `minimal` value configurations, covering both the enabled and disabled conditional branches.

## Impact

- `helm/chart/build.gradle.kts` — add the `lint` block (strict, subcharts, two configurations).
- `helm/chart/src/test/helm/helm-lint-full.yaml` and `helm/chart/src/test/helm/helm-lint-minimal.yaml` — new lint
  value files.
- Gradle tasks: `helmLintMainChartFull`, `helmLintMainChartMinimal`, and the umbrella `helmLintMainChart`.
- The citi gradle-helm-plugin tasks are configuration-cache incompatible, so these tasks must be run with
  `--no-configuration-cache`.
