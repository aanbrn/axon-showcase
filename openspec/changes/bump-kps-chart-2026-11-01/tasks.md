# Tasks

Workspace: unit=bump-kps-chart-2026-11-01; path=/Users/afanasyev/Development/axon-showcase-bump-kps-chart-2026-11-01;
branch=bump-kps-chart-2026-11-01; writer=ses_e6b3f9701ffewWWmCA4CSRHN9L.

## 1. The bump

- [x] 1.1 Re-resolve the chart with the check's own run (`./gradlew helmUpdates`, which refreshes the chart repositories
      before searching) rather than a hand-run `helm search repo`, whose local index read stale on a previous bump.
      **Done**: `prometheus-community-stack: 92.13.1 -> 92.17.0` — one row, still current — and a refreshed
      `helm search repo prometheus-community/kube-prometheus-stack --versions` lists `92.17.0` first.
- [x] 1.2 Set the `prometheus-community-stack` coordinate in `gradle/libs.versions.toml` to the resolved version, as a
      concrete pin. **Done**: `92.13.1` → `92.17.0`.
- [x] 1.3 Move the manual `helm install kps … --version` command in `.opencode/skills/deployment-operations/SKILL.md` to
      the same version, and pass `./gradlew verifyInstallCommands`. **Done**: the command quotes `92.17.0` and the task
      reports BUILD SUCCESSFUL; the same task run against `92.13.1` fails with "installs chart
      'prometheus-community/kube-prometheus-stack' at '92.13.1', but the catalog pins '92.17.0'", so the gate reads the
      edited file rather than passing vacuously.
- [x] 1.4 Confirm the old pin survives only in dated records: grep `92\.13\.1` across the tracked set. **Done**:
      `git grep -n 92\.13\.1 -- . ':!openspec/changes'` exits 1, so no tracked file outside `openspec/changes/` carries
      the old pin; the occurrences that remain are the archived `2026-10-25-bump-kps-chart-2026-10-25` record and this
      unit's own artifacts, both of which record the from-version.

## 2. Verification

- [x] 2.1 Re-run the check that produced the finding (`./gradlew helmUpdates`) and confirm it reports no Helm updates.
      **Done**: "No Helm updates available.", BUILD SUCCESSFUL.
- [x] 2.2 Install the bumped chart live and read the deployed signal, as the `infra-image-versions` scenario requires.
      **Done**: `kps` upgraded to revision 2 on `kube-prometheus-stack-92.17.0` (app `v0.94.2`), read from one command
      each:
  - `./gradlew helmInstallKpsToLocal` — the upgrade itself, BUILD SUCCESSFUL.
  - `helm list -n monitoring` — `kps` revision 2, `deployed`, chart `kube-prometheus-stack-92.17.0`.
  - `kubectl get pods -n monitoring` — Grafana 3/3, kube-state-metrics 1/1, operator 1/1, prometheus-kps-0 2/2 and
    tempo-0 1/1, all Running with no restarts. The Prometheus pod rolled with the upgrade and read 1/2 while the
    StatefulSet restarted it, then 2/2; the targets below were read after it became ready.
  - Prometheus's `/api/v1/targets?state=active` through the API server's service proxy — 14 active, 14 up, 0 down.
  - Grafana's `/api/datasources` and `/api/search?query=Axon`, over a port-forward — Prometheus and Tempo, and the
    `Axon Showcase` dashboard.
- [x] 2.3 Run `./gradlew spotlessApply`/`spotlessCheck` and `openspec validate --all`; run the manual 120-character
      check over the change's whole changed set. **Done**: `openspec validate --all` reports 27 passed, 0 failed;
      `spotlessApply` then `spotlessCheck` report BUILD SUCCESSFUL after the last edit to a gated file; the manual check
      — over the tracked diff's added lines and every untracked file, with the recipe's known positive control hit first
      — finds no line over 120. `gradle/libs.versions.toml` carries pre-existing over-long dependency lines this change
      does not touch.

## 3. Review, capture and the manual pass

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`, seeded by
      `scripts/claim-scan.py`, with every candidate resolved into a row or a class-level dismissal. **Done**: the table
      and the class-level dismissal are in `design.md`; `python3 scripts/claim-scan.py --base origin/main` was run over
      the unit's diff, and each candidate resolves to a row or falls under the dismissal `design.md` records.
- [x] 3.2 Run a `review-quick` pass over the proposal and the implementation, appending each round's row to
      `docs/review-rounds.tsv` and running the ledger checkpoint after each append; repeat until a round reports nothing
      new. **Done**: the loop ran in reviewer session `ses_e6b37762fffesMjyE4IPZbl6Ep`, each round's row appended as its
      report arrived with `--unique-rounds --expect <unit>:<round>` and `--session-continuity` run after every append;
      that ledger is the record of the rounds and their classes, and `--unique-rounds --expect-clean <unit>` is the
      boundary verdict.
- [x] 3.3 Derive the deep-review routing outcome with `scripts/review-routing.py`, decide at or above the floor, and
      record the outcome and its signals here. **Done**: the derivation
      `python3 scripts/review-routing.py --unit bump-kps-chart-2026-11-01 --change bump-kps-chart-2026-11-01` reports
      `ROUTING: RECOMMENDED` on its `[repeated-class]` signal — rounds of this unit's own ledger repeat a class — so
      this record follows the current derivation rather than the `NOT_NEEDED` an earlier round produced. The owner
      resolved to skip the deep review, so no `/review-thorough` session was created.
- [x] 3.4 Decide the `lesson-capture` run against its skip condition; when it does not hold, run the subagent, apply its
      durable proposals, and record the applied net delta — or record the skip with its reason. **Done**: skipped, net
      delta 0. The unit's record states no process defect observed, and the `agent-skills` requirement makes
      generalization its own unit on already-merged records ("the product unit's gate is the hand-off, not the
      analysis", with generalization not blocking the unit), so no capture runs here.
- [ ] 3.5 Ask the owner for the manual review pass; commit, push, archive and merge only after approval.

## Workflow follow-up

- The change's archive commit rides this PR, after the `build` check is green.
- Issue #40 stays open and carries no closing keyword: the `helm-updates` workflow updates the open issue's body, so its
  next run rewrites it to "No Helm updates available." instead of opening a fresh issue over an orphaned history.
