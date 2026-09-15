## 1. Bump the chart coordinate

- [x] 1.1 Bump `prometheus-community-stack` in `gradle/libs.versions.toml` from `90.0.0` to `91.4.0`. The issue named
      `91.2.3`, but `helm search repo prometheus-community/kube-prometheus-stack` (no `--versions`) resolves `91.4.0` as
      the current latest (same app version `v0.94.0`), so the bump clears the check.
- [x] 1.2 Update the `--version 90.0.0` flag on the manual `helm install kps prometheus-community/kube-prometheus-stack`
      command in `AGENTS.md` to `--version 91.4.0`. (`README.md` does not quote the version.)

## 2. Verify

- [x] 2.1 Render the chart at `91.4.0` with the local values:

      ```bash
      helm template kps prometheus-community/kube-prometheus-stack --version 91.4.0 \
        -f helm/values/kps/values-local.yaml --namespace monitoring
      ```

      The render is clean, the resource-kind set is identical to `90.0.0`, and the key overrides land: the Grafana Tempo
      datasource, the Grafana/kube-state-metrics/prometheus/operator resource requests+limits, and alertmanager disabled
      (no `Alertmanager` resource). The only image change is the operator (`v0.93.1` → `v0.94.0`).
- [x] 2.2 Run `./gradlew helmUpdates` — the report is `No Helm updates available.` (no `prometheus-community-stack`
      row).
- [x] 2.3 Smoke-test the deployed stack on a local `kind` cluster: `helmInstallKpsToLocal` deploys the `kps` release at
      `91.4.0` with all pods ready (grafana 3/3, kube-state-metrics 1/1, operator 1/1, prometheus 2/2; alertmanager
      absent as configured). Prometheus reports 6 targets `up` (kubelet, coredns, kube-state-metrics) built from the
      chart's own ServiceMonitors, proving the operator's ServiceMonitor → scrape-config path; the `down`/`unknown`
      control-plane targets are kind's standard unreachable kube-proxy/etcd/scheduler endpoints, not a chart defect. The
      Grafana datasource configmap carries both the default Prometheus datasource and the Tempo datasource. The app
      services' ServiceMonitors were not exercised — the smoke test deployed only the kps release, not the app chart;
      the ServiceMonitor reconciliation path is proven by the chart's own monitors.
- [x] 2.4 Run `openspec validate --all` — passes.
