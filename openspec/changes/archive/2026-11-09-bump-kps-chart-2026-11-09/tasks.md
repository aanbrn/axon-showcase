# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is `docs/units/bump-kps-chart-2026-11-09/unit.md`.

## 1. The bump

- [x] 1.1 Re-resolve the chart with the check's own run (`./gradlew helmUpdates`, which refreshes the chart repositories
      its own Helm client reads before searching) rather than a stale index. Record the row the run prints. Then,
      because that client's repository file lives under `build/helm/`, refresh the host CLI's index
      (`helm repo update prometheus-community`) and record the newest `92.x` from
      `helm search repo prometheus-community/kube-prometheus-stack --versions`. **Done**: the run reports
      `prometheus-community-stack: 92.17.0 -> 93.1.0`, and the refreshed host listing's newest `92.x` is `92.22.0`.
- [x] 1.2 Set the `prometheus-community-stack` coordinate in `gradle/libs.versions.toml` to `92.22.0`, the newest
      reachable `92.x`, as a concrete pin. **Done**: `92.17.0` → `92.22.0`.
- [x] 1.3 Move the manual `helm install kps … --version` command in `.opencode/skills/deployment-operations/SKILL.md` to
      `92.22.0`. **Done**: the command quotes `92.22.0`.
- [x] 1.4 Pass `./gradlew verifyInstallCommands`. **Done**: BUILD SUCCESSFUL.
- [x] 1.5 Record the gate's known-bad control: run it against `92.17.0` and confirm it fails naming the catalog pin, so
      the gate is shown to read the edited file. **Done**: with the command temporarily at `92.17.0` the task fails with
      "installs chart 'prometheus-community/kube-prometheus-stack' at '92.17.0', but the catalog pins '92.22.0'"; the
      file was restored and the task re-passed.
- [x] 1.6 Sweep the old pin across the live tree: `git grep -n '92\.17\.0' -- . ':!openspec/changes' ':!docs/units'`
      must exit 1, and a wider `git grep -n '92\.17\.0'` must show its remaining hits only under `openspec/changes/` and
      `docs/units/` (dated records). **Done**: the narrowed grep exits 1; the wider one lists hits only under
      `docs/units/bump-kps-chart-2026-11-09/` and `openspec/changes/`.
- [x] 1.7 Sweep the docs this bump makes stale: grep `kube-prometheus-stack`, `prometheus-community-stack`, and the new
      version across `README.md`, `AGENTS.md`, `docs/`, and `.opencode/`, and correct every present-tense claim. Dated
      records (baselines, retrospectives, audits) keep the values they ran under. **Done**: the live mentions are the
      parked hold entry, the pack's install command (updated) and its concrete-pin rule, which states no version; the
      rest are dated records or the change's own archive.
- [x] 1.8 Park the `93.x` hold in `docs/ideas.md` under a `## 2026-11-09` section: the operator image gap, the `92.22.0`
      pin it forces, and the verifiable return condition (the chart's operator line publishing a `linux/arm64` image).
      Keep the trailing status tag. **Done**: the entry is in the `## 2026-11-09` section with the
      `— parked; no change yet.` tag and the `linux/arm64` return condition.

## 2. Verification

- [x] 2.1 Re-run the check that produced the finding (`./gradlew helmUpdates`) and confirm it reports
      `92.22.0 -> 93.1.0`, whose left side is the new pin, and that `92.17.0` no longer appears. **Done**: the report
      reads `prometheus-community-stack: 92.22.0 -> 93.1.0`.
- [x] 2.2 Read the migration notes of the exact new subchart versions (kube-state-metrics `9.0.0`, grafana `14.4.0`;
      node-exporter and windows-exporter are disabled or absent) and record any value an upgrade requires. A required
      values change stops the plan for the owner's re-agreement rather than being applied silently. **Done**: the
      subcharts are the versions `93.1.0` also ships; kube-state-metrics `9.0.0`'s only upgrade note is the default
      `nodeSelector: kubernetes.io/os: linux` (a Linux cluster takes it), and grafana `14.4.0` leaves the keys the file
      sets unchanged — no values change.
- [x] 2.3 Render the pinned chart with the local values
      (`helm template kps prometheus-community/kube-prometheus-stack --version 92.22.0 -f helm/values/kps/values-local.yaml`
      after `helm repo update prometheus-community`) and inspect the output for each override the file sets: the
      resources on kube-state-metrics, grafana, grafana's sidecar, the operator, the operator's config-reloader and
      Prometheus; the Tempo datasource; and the Grafana ingress host. Confirm every key the file disables renders
      nothing (`grep -niE 'enabled: false|selfmonitor: false' helm/values/kps/values-local.yaml` gives the set). A zero
      exit status alone is not the check, since Helm ignores a key the chart does not read. **Done**: the render carries
      kube-state-metrics `10m/64Mi` (`128Mi` limit), grafana `50m/0.75Gi` (`1Gi`), grafana's datasource sidecar
      `50m/128Mi` (`160Mi`), the operator `50m/64Mi` (`128Mi`), the operator's config-reloader through the operator's
      args (`--config-reloader-cpu-request=50m`, `--config-reloader-memory-request=64Mi`,
      `--config-reloader-memory-limit=128Mi`, no CPU limit), and Prometheus `100m/1.5Gi` (`2Gi`). It carries the Tempo
      datasource and the `axon-showcase-grafana` ingress host. Each of the seven disabled keys is recorded: no
      alertmanager StatefulSet, no node-exporter DaemonSet, no grafana or Prometheus/operator self ServiceMonitor, no
      default-dashboard ConfigMaps (a render with `grafana.defaultDashboardsEnabled=true` adds 23; ours has none as the
      `--set`-flipped comparison shows, and the live `/api/search` returns only `Axon Showcase`), and the
      `kps-grafana-datasource` ConfigMap and the live `/api/datasources` list Prometheus and Tempo with no Alertmanager
      entry (a render with `grafana.sidecar.datasources.alertmanager.enabled=true` adds it). The render also carries the
      eight kubernetes ServiceMonitors.
- [x] 2.4 Read the pre-upgrade baseline on the local target's context
      (`helm list -n monitoring --kube-context "$(grep '^helm.local.kubeContext=' ~/.gradle/gradle.properties | cut -d= -f2)"`,
      shared with the Gradle, Helm and kubectl reads): the chart and revision, pod readiness, the active Prometheus
      targets with their names, Grafana's datasources, and the operator's state. An absent release makes the install a
      fresh one. **Done**: `kps` revision 4 on `kube-prometheus-stack 92.17.0` (app `v0.94.2`); every pod Ready; 14
      active targets, 14 up, 0 down; Grafana reads Prometheus and Tempo; the operator image and the CRD annotation are
      `v0.94.2`.
- [x] 2.5 Stop and report to the owner before proceeding if any of these holds: the baseline is unhealthy, the baseline
      needs a destructive recovery, the release contradicts the expected `92.17.0`, or task 2.2's migration notes
      require a values change. **Done**: none held.
- [x] 2.6 Confirm the operator version is unchanged (`92.17.0` and `92.22.0` both carry `appVersion: v0.94.2`, and the
      running operator image is `v0.94.2`), so no CRD apply is owed. **Done**: both tags carry `v0.94.2`, the chart's
      `charts/.../crds` tree is identical between them (every blob SHA matches), and the running operator stayed
      `v0.94.2`.
- [x] 2.7 Upgrade or install the release with `./gradlew helmInstallKpsToLocal`. **Done**: BUILD SUCCESSFUL; `kps`
      reached revision 5 on `kube-prometheus-stack-92.22.0` (app `v0.94.2`).
- [x] 2.8 Compare the deployed signal with task 2.4's baseline rather than only counting failures:
      `helm list -n monitoring` shows the new chart, every pod is Ready, the active target set is non-empty and loses
      none of the baseline's named targets, and Grafana's datasources still read Prometheus and Tempo
      (`/api/datasources`, `/api/search` for `Axon Showcase`), authenticating with the admin credentials read from the
      `kps-grafana` secret without printing them. **Done**: every pod Ready with no restarts; 14 active targets, all up,
      and the same job set as the baseline — the kube-state-metrics scrape URL moved with its pod IP; Grafana reads
      Prometheus and Tempo and the `Axon Showcase` dashboard. (A read taken immediately after the upgrade still showed
      three targets down while the pods re-registered; a read 30 s later showed 14/14.)
- [x] 2.9 Run the pull-request gate, since the change touches the root build's configuration:
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`. **Done**: BUILD SUCCESSFUL after `spotlessApply`
      re-wrapped a hand-over line the first run flagged.
- [x] 2.10 Run `openspec validate --all` and confirm it reports no failure. **Done**: 27 passed, 0 failed.
- [x] 2.11 Run `./gradlew spotlessApply` and then `spotlessCheck`, after the last edit to a gated file. **Done**:
      `spotlessApply` run after the last edit and `spotlessCheck` green inside the `check` run.
- [x] 2.12 Run the manual 120-character check over the change's whole changed set
      (`git status --porcelain --untracked-files=all`), with the recipe's known positive control hit first. **Done**:
      the control flags `gradle/libs.versions.toml`'s pre-existing lines; over the changed set the over-long lines are
      the tsv record rows and the `Workspace:` record (one line by contract), the toml's pre-existing entries, and
      markdown the formatter owns.

## 3. Review and the manual passes

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`, seeded by
      `scripts/claim-scan.py`, with every candidate resolved into a row or a class-level dismissal. **Done**: the table
      and its class-level dismissal are in `design.md`, re-derived after each retarget edit;
      `claim-scan.py --base     origin/main` was re-run over the final tree.
- [x] 3.2 Run the proposal `review-quick` pass: record the hand-over at
      `docs/units/bump-kps-chart-2026-11-09/review-handover.md`, pass it through
      `python3 scripts/commit-hygiene.py --handover bump-kps-chart-2026-11-09:<round>`, append the round's row and its
      instance file, and repeat until a round reports nothing new.
- [x] 3.3 Ask the owner for the manual review pass of the retargeted proposal, and end the turn there. **Done**: the
      owner approved the retargeted proposal on 2026-11-09, after rounds 11–16 closed clean.
- [x] 3.4 Run the implementation `review-quick` pass over the diff and the tasks, appending each round's row and
      instance file and repeating until a round reports nothing new. **Done**: rounds 17–19, closing clean at 19;
      `--expect-clean` green.
- [x] 3.5 Derive the deep-review routing outcome with `scripts/review-routing.py`, record it with its signals in
      `docs/units/bump-kps-chart-2026-11-09/deep-review.md`, decide at or above the floor, and run `/review-thorough`
      when the outcome requires it. **Done**: the derivation reports `RECOMMENDED` on `[repeated-class]`; the owner
      resolved to run on 2026-11-09, and the deep review completed — pass 1 raised one should-fix finding and pass 2
      verified its fix, `nothing remains`; both passes are recorded in that file.
- [x] 3.6 Write `docs/units/bump-kps-chart-2026-11-09/process-defects.md` as defects are observed, or state that none
      was observed. **Done**: two records — the inline-code whitespace run and the reviewer invocation delegated without
      its hand-over and packs.
- [x] 3.7 Ask the owner for the manual review pass of the implementation, and end the turn there. **Done**: the owner
      approved the implementation and requested delivery on 2026-11-09, after the clean implementation loop (rounds
      17–19), the completed deep review (passes 1–2) and the process-defect record.

## 4. Upstream report

- [x] 4.1 Confirm the existing upstream report (`prometheus-operator/prometheus-operator#8953`, "v0.95.1 image index
      only contains linux/amd64 manifests") covers the defect, extend it with what its body leaves unchecked — the
      `prometheus-config-reloader` and `admission-webhook` indexes, and the `kube-prometheus-stack` 93.x downstream
      failure — run the comment past `review-quick`, fix its findings, and post it; the owner authorized the report on
      2026-11-09, and a duplicate issue is the wrong form. **Done**: the issue is open (2026-11-06); three screens fixed
      eleven findings (the pod state, the image set across every record, the missing claims rows, the over-claims, and
      the query's substitution trap), and the comment is posted at
      `https://github.com/prometheus-operator/prometheus-operator/issues/8953#issuecomment-6729610410`.
- [x] 4.2 Record `prometheus-operator/prometheus-operator#8953` and its `retires when` clause in the parked
      `docs/ideas.md` entry, so the hold's return condition and the report retire together; `docs/ideas.md` is in the
      corpus `upstreamReferences` scans. **Done**: the entry names `#8953` and states that it retires when the issue
      closes with multi-arch images republished.

## Workflow follow-up

- The change's archive commit rides this PR, after the `build` check is green.
- Issue #40 stays open and carries no closing keyword: the `helm-updates` workflow rewrites the open issue's body on its
  next run, and the held `93.x` row is expected in it.
