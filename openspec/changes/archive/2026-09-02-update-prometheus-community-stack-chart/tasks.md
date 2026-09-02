## 1. Bump the chart coordinate

- [x] 1.1 Bump `prometheus-community-stack` from `77.14.0` to `88.6.2` in `gradle/libs.versions.toml`.
- [x] 1.2 Update the `--version 77.14.0` flag on the manual `helm install kps prometheus-community/kube-prometheus-stack`
  command in `AGENTS.md` to `--version 88.6.2`.

## 2. Update docs and verify

- [x] 2.1 Update the same `--version 77.14.0` flag in `README.md` to `--version 88.6.2`.
- [x] 2.2 Render the chart at `88.6.2` with `helm/values/kps/values-local.yaml` (`helm template kps
  prometheus-community/kube-prometheus-stack --version 88.6.2 -f helm/values/kps/values-local.yaml --namespace
  monitoring`) and confirm the render is clean and the key overrides (Grafana Tempo datasource, resource
  requests/limits, alertmanager disabled) are present.
- [x] 2.3 Run `./gradlew helmUpdates` and confirm `prometheus-community-stack` no longer appears in
  `build/helm-updates/report.txt`.
- [x] 2.4 Install the chart at `88.6.2` into the local target (`./gradlew helmInstallKpsToLocal`) and smoke-test the
  deployed stack: kps pods ready, all Prometheus targets `up`, and Grafana serving with the Prometheus + Tempo
  datasources wired. Uninstall (`./gradlew helmUninstallKpsFromLocal`) and remove the `monitoring` namespace
  afterwards.