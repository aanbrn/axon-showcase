## 1. Add lint value files

- [x] 1.1 Create `helm/chart/src/test/helm/helm-lint-full.yaml` enabling every optional feature (ingress, HTTPRoute,
      VPA/HPA, secured OpenSearch, observability, extraDeploy, RBAC rules, NetworkPolicy extras, ServiceMonitor
      tuning, extra env/ports, PDB `minAvailable`)
- [x] 1.2 Create `helm/chart/src/test/helm/helm-lint-minimal.yaml` disabling the default-on features (ServiceAccount,
      RBAC, NetworkPolicies, PDBs, ServiceMonitors, probes, autoscaling, observability)

## 2. Configure lint in the chart build

- [x] 2.1 Add a `lint` block to the `main` chart in `helm/chart/build.gradle.kts` with `strict` and `withSubcharts`
      enabled
- [x] 2.2 Add `full` and `minimal` lint `configurations`, each pointing at its value file under `src/test/helm/`

## 3. Verify

- [x] 3.1 Run `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal
      --no-configuration-cache` and confirm both pass (2 charts linted, 0 failed)
- [x] 3.2 Confirm the umbrella `helmLintMainChart` task depends on both configurations
