# Tasks

## 1. The bump

- [x] 1.1 Re-resolve the chart's latest (`helm repo update prometheus-community` then
      `helm search repo prometheus-community/kube-prometheus-stack --versions`) and set `prometheus-community-stack` in
      `gradle/libs.versions.toml` to it. (Done: the index was stale at `91.9.0`; after the repo update the search lists
      `92.3.0`, and the catalog reads `92.3.0`.)
- [x] 1.2 Move the manual `helm install kps … --version` command in `AGENTS.md`'s Kubernetes Deployment section to the
      same version (`verifyInstallCommands` asserts them against the catalog). (Done: the command's `--version 92.3.0`
      matches the catalog.)

## 2. Verification

- [x] 2.1 Run `./gradlew verifyInstallCommands` and `./gradlew helmUpdates`; confirm the first passes and the `kps` row
      is gone from the report. (Both green — every documented install command matched the catalog pin, and the report
      now reads "No Helm updates available.")
- [x] 2.2 **Install the chart live and smoke it**: `./gradlew helmInstallKpsToLocal`, then confirm the `monitoring` pods
      are ready, Prometheus shows its targets up, and Grafana's datasources are wired (its provisioned Prometheus plus
      the Tempo one from the values). (Upgraded the running release to revision 7 on `kube-prometheus-stack-92.3.0`; all
      pods Running/Ready — Grafana 3/3, Prometheus 2/2, operator and kube-state-metrics 1/1; the `prometheuses` /
      `servicemonitors` CRDs are already `v1`; Prometheus reports **14 targets up, 0 down**; Grafana lists Prometheus
      (default) and Tempo as datasources.)
- [x] 2.3 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`; run the manual
      120-character check over **the lines this change introduces** in `gradle/libs.versions.toml`, `AGENTS.md` (the
      install command, which sits in an un-gated `bash` fence), and the change dir's `.openspec.yaml`; confirm clean.
      (Done: `spotlessCheck` and `openspec validate --all` — 27/0 — pass, and the manual check over those introduced
      lines is clean.)
- [x] 2.4 Run a `review-quick` pass over the proposal and the implementation, repeating until clean.

## 3. Capture

- [x] 3.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: **net +1 rendered line** — the analogous-change bullet widened from "A
      subagent/command change …" to "A recurring unit type — a subagent/command change, a version bump — derives its
      change dir from the last analogous archived change", naming what a bump takes from it (the task set, each task's
      result record, the spec-position rationale). The capture judged every other finding a **compliance miss of an
      existing rule** — the `skip_specs` grep / "read the spec's existing wording", the pre-write derivation pass, the
      Formatting convention's inline-span rule, the cite-the-output rule, the sweep rules — not a gap, and noted the
      eight rounds' dominant cause: the mandatory pre-write derivation pass was skipped on a small, familiar unit. No
      retirements proposed.
- [x] 3.2 Ask the owner for the manual review pass, after 2.4 is clean and 3.1 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- No CI job installs the observability stack (the `ci` target excludes its tag), so this change's evidence is the local
  smoke's record; the runner-hosted `kind` route `AGENTS.md` documents stays available if a CI check is wanted.
