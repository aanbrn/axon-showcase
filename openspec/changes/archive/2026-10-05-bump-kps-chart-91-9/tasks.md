# Tasks

## 1. Apply the bump

- [x] 1.1 Bump the `prometheus-community-stack` pin in `gradle/libs.versions.toml` from `91.8.1` to `91.9.0` (the
      resolved latest). Verify by reading the catalog and `helm search repo prometheus-community/kube-prometheus-stack`.
      Done: catalog reads `91.9.0`; the lookup's head row is
      `prometheus-community/kube-prometheus-stack 91.9.0 v0.94.1`.
- [x] 1.2 Refresh the manual install command in `AGENTS.md`'s Kubernetes Deployment section (`--version 91.8.1` →
      `91.9.0`). Verify by reading the command and running `./gradlew verifyInstallCommands`. Done: command reads
      `--version 91.9.0`; `verifyInstallCommands` `BUILD SUCCESSFUL`.

## 2. Verification

- [x] 2.1 Install the bumped chart on the **local cluster** (`./gradlew helmInstallKpsToLocal`) and read the deployed
      signal: the `kps` pods ready, the Prometheus targets up, and the Grafana datasources wired. Done:
      `helm list -n monitoring` → `kube-prometheus-stack-91.9.0` `deployed` (revision 5); all monitoring pods Ready;
      Prometheus 14/14 active targets `up`; Grafana `/api/datasources` returns `Prometheus` (default) + `Tempo`.
- [x] 2.2 Run `./gradlew helmUpdates` and confirm the report no longer lists `prometheus-community-stack` — the check
      that produced the finding is cleared. Done: the report reads "No Helm updates available."
- [x] 2.3 Run `./gradlew check` (or the affected gates) and confirm `verifyInstallCommands` passes against the refreshed
      `AGENTS.md` command. Done: `verifyInstallCommands` `BUILD SUCCESSFUL`; the full `check` is the PR gate.
- [x] 2.4 Run the implementation `review-quick` loop over the diff; fix its findings and re-run until it reports nothing
      new. Done: clean on the first round (no findings).
- [x] 2.5 Run the per-unit `lesson-capture` over this change and apply its durable proposals; record the applied net
      `AGENTS.md` delta. Done: 0 durable proposals (net 0) — the candidates were already covered by the
      `verifyInstallCommands` gate and existing bullets.
- [x] 2.6 Run `./gradlew spotlessApply` after the last edit and confirm `spotlessCheck` passes; run the manual
      120-character check over the lines this change introduces in `gradle/libs.versions.toml`, `AGENTS.md` (the install
      command), and the change dir's `.openspec.yaml`. Done (final pass after the task ticks).
- [x] 2.7 Request the user's manual review pass — the step before committing. Done: the user approved the
      implementation.
