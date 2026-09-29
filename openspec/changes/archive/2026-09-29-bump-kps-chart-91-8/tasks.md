# Tasks

## 1. Apply the bump

- [x] 1.1 Bump the `prometheus-community-stack` pin in `gradle/libs.versions.toml` from `91.4.1` to `91.8.1` (the
      resolved latest; the tracker named `91.8.0`). Verify by reading the catalog and
      `helm search repo prometheus-community/kube-prometheus-stack`.
- [x] 1.2 Refresh the manual install command in `AGENTS.md`'s Kubernetes Deployment section (`--version 91.4.1` →
      `91.8.1`). Verify by reading the command.

## 2. Verification

- [x] 2.1 Install the bumped chart on the **local cluster** (`./gradlew helmInstallKpsToLocal`) and read the deployed
      signal: the `kps` pods ready, the Prometheus targets up, and the Grafana datasources wired. Done:
      `helm list -n monitoring` → `kube-prometheus-stack-91.8.1` deployed; all monitoring pods Ready; Prometheus 14/14
      active targets `up`; Grafana datasources `Prometheus` and `Tempo` wired. The runner-hosted `kind` second-cluster
      run was deliberately skipped (the owner's decision), and the `kps` release was upgraded in place, so no cleanup is
      owed.
- [x] 2.2 Run `./gradlew helmUpdates` and confirm the report no longer lists `prometheus-community-stack` — the check
      that produced the finding is cleared. Done: the report reads "No Helm updates available."
- [x] 2.3 Run the implementation `review-quick` loop over the diff; fix its findings and re-run until it reports nothing
      new.
- [x] 2.4 Run the per-unit `lesson-capture` over this change and apply its durable proposals, recording the applied net
      `AGENTS.md` delta on this change's record. Done: 0 durable proposals (net 0); the manual-install-version drift was
      routed to a check and parked in `docs/ideas.md`.
- [x] 2.5 Run `./gradlew spotlessApply` after the last edit and confirm `spotlessCheck` passes; run the manual
      120-character check over the lines this change introduces in `gradle/libs.versions.toml`, `AGENTS.md` (the install
      command), and the change dir's `.openspec.yaml`.
- [x] 2.6 Request the user's manual review pass — the step before committing.
