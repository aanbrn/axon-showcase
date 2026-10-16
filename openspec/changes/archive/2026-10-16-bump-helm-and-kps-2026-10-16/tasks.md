# Tasks

## 1. The bumps

- [x] 1.1 Re-resolve both rows with the tools' own lookups (the Helm CLI's latest release; the chart only after
      `helm repo update prometheus-community`, since the local index was stale at `92.3.0`) and set `helm` to `4.3.1`
      and `prometheus-community-stack` to `92.9.0` in `gradle/libs.versions.toml`.
- [x] 1.2 Move the manual `helm install kps prometheus-community/kube-prometheus-stack --version` command in `AGENTS.md`
      to `92.9.0` (`verifyInstallCommands` asserts it against the catalog pin). (Passes: every documented command
      matched its catalog pin.)

## 2. Verification

- [x] 2.1 Run `./gradlew verifyInstallCommands` and `./gradlew helmUpdates`; confirm the first passes and both rows are
      gone from the report.
- [x] 2.2 **Install live and smoke it**: `./gradlew helmInstallKpsToLocal`, then confirm the `monitoring` pods are
      ready, Prometheus reports its targets up, and Grafana lists its datasources (its provisioned Prometheus plus the
      Tempo one from the values). This exercises the new client as well as the new chart. Record the release's new
      revision and chart version. (Upgraded the running release to revision 8 on `kube-prometheus-stack-92.9.0`; the
      plugin downloaded and used client **v4.3.1** (`.gradle/helm/client/4.3.1/darwin-arm64/helm`); the `monitoring`
      pods are all Running/Ready (Grafana 3/3, Prometheus 2/2, operator and kube-state-metrics 1/1, Tempo 1/1);
      Prometheus reports **14 targets up, 0 down**; Grafana lists Prometheus (default) and Tempo as datasources.)
- [x] 2.3 Run `./gradlew spotlessApply` then `spotlessCheck` and `openspec validate --all`; run the manual 120-character
      check over the lines this change introduces in `gradle/libs.versions.toml` and `AGENTS.md` (the install command
      and the chart pin, both un-gated by Spotless); confirm clean.
- [x] 2.4 Run a `review-quick` pass over the proposal and the implementation, repeating until clean: two rounds (4,
      clean). Round 1 found a defect I had introduced — a join-match correction wrote its joined form back and collapsed
      `design.md` to one physical line, which Prettier accepts as a single heading, so no gate saw it — plus a claims
      row sourced to a tracker issue that carries the previous unit's rows, a missing row, and a `Why` over the cap; all
      four were fixed and confirmed.

## 3. Capture

- [x] 3.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta: **+4 lines**, one clause appended to the stale-target bullet — a hand-run `helm search repo`
      reads the local index, so refresh it first or re-resolve from `./gradlew helmUpdates` (two independent
      occurrences: this unit's run and the 2026-10-13 kps bump). The collapsed-artifact and unswept-row candidates were
      judged compliance misses of existing rules (the structured-file edit bullet and the doc-consistency sweep bullet),
      and no retirements were proposed.
- [x] 3.2 Ask the owner for the manual review pass, after 2.4 is clean and 3.1 is done.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass as a task (every unit owes it). It ran: the claims table is in
      `design.md`, and its rows are derived — the CLI's latest from the releases API, the chart's from the repo search
      after the index update, the CLI pin's consumer from the convention plugin, and the "no other surface" row from a
      grep over the workflows.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The Paketo findings are a separate unit.
