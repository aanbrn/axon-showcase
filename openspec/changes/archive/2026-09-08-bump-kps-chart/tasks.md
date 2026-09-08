## 1. Bump and verify the kps chart

- [x] 1.1 Bump `prometheus-community-stack` in `gradle/libs.versions.toml` from `88.6.2` to `90.0.0`.
- [x] 1.2 Run `./gradlew helmUpdates` and confirm the report no longer lists `prometheus-community-stack` (or lists it
      as current).
- [x] 1.3 Install the `kps` release at the new version (`./gradlew helmInstallKpsToLocal` or a full
      `helmInstallToLocal`) and confirm the release installs cleanly (prometheus, alertmanager, grafana, operator pods
      ready).
- [x] 1.4 Smoke-test the observability wiring: the services' ServiceMonitors appear as up Prometheus targets, and the
      Grafana datasources (Prometheus, Tempo) are wired. The web-UI `http-metrics` target is excluded here — its
      stub_status → `/metrics` converter is a separate follow-up (see the web-UI metrics change).
- [x] 1.5 If the `90.0.0` chart drops/renames a value the local values use, adjust
      `helm/values/axon-showcase/values-local.yaml` accordingly and re-verify with the smoke test. No values
      adjustment was needed.
- [x] 1.6 Update `AGENTS.md` / `README.md` if they quote the `kps` chart version (`--version 88.6.2` → `90.0.0`).

## 2. Docs and verify

- [x] 2.1 Run `openspec validate --all` and confirm the change passes.
- [x] 2.2 Confirm `./gradlew helmUpdates` passes cleanly.