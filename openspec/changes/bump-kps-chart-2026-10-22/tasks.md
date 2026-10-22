# Tasks

## 1. The bump

- [x] 1.1 Re-resolve the chart with the check's own run (`./gradlew helmUpdates`, which refreshes the chart repositories
      before searching) rather than a hand-run `helm search repo`, whose local index read stale on a previous bump.
      **Done**: `prometheus-community-stack: 92.9.0 -> 92.11.0`, still current.
- [x] 1.2 Set the catalog coordinate in `gradle/libs.versions.toml` to `92.11.0`. **Done**: the coordinate is concrete,
      as the catalog's rule requires.
- [x] 1.3 Move the manual `helm install kps … --version` command in the `deployment-operations` pack to `92.11.0`.
      **Done**: `./gradlew verifyInstallCommands` passes, so the documented command matches the catalog pin.
- [x] 1.4 Confirm the old pin survives only in dated records. **Done**: `git grep -n 92\.9\.0 -- ':!openspec/changes'`
      returns the two tracked sites, `AGENTS.md`'s incident narration and `docs/audits/2026-10-19.md`'s observation,
      both of which record what ran then.

## 2. Verification

- [x] 2.1 Re-run the check that produced the finding. **Done**: `./gradlew helmUpdates` reports "No Helm updates
      available."
- [x] 2.2 Install the bumped chart live and read the deployed signal, as the `infra-image-versions` scenario requires.
      **Done**: `./gradlew helmInstallKpsToLocal` upgraded `kps` to revision 3 on `kube-prometheus-stack-92.11.0`; the
      `monitoring` pods are Running/Ready (Grafana 3/3, Prometheus 2/2, operator, kube-state-metrics and Tempo 1/1);
      Prometheus reports 14 active targets, 14 up and 0 down; Grafana lists its Prometheus and Tempo datasources.
- [x] 2.3 Run `./gradlew spotlessApply`/`spotlessCheck` and `openspec validate --all`; run the manual 120-character
      check over the lines this change adds. **Done**: all green.

## 3. Review, capture and the manual pass

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`, seeded by
      `scripts/claim-scan.py`, with every candidate resolved into a row or a class-level dismissal. **Done**: the table
      and the class-level dismissal are in `design.md`.
- [x] 3.2 Run a `review-quick` pass over the proposal and the implementation, appending each round's row to
      `docs/review-rounds.tsv` and running the ledger checkpoint after each append; repeat until a round reports nothing
      new. **Done**: each row was appended as its report arrived, with the checkpoint run after each append; the ledger
      carries the rounds and their outcomes, and the loop is clean.
- [x] 3.3 Derive the deep-review routing outcome, decide at or above the floor, and record the outcome and its signals
      here. **Done**: `NOT_NEEDED`.
- [x] 3.4 Run the `lesson-capture` subagent and record its applied net delta or its skip reason with the reason.
      **Done**: one addition — the search-scope clause merged into `AGENTS.md`'s vacuous-glob rule, naming that
      `grep -r` reads the working tree (untracked generated state included) while `git grep` reads the tracked set
      (`+4/-1` in `AGENTS.md`) — plus a fix its own report named: the claims row's pathspec now excludes
      `openspec/changes` entirely, so the row cannot match its own artifact once committed. Two further dispositions are
      recorded rather than applied: no rule was retired, and the r2 pipe lesson's increment (prefer a pipe-free
      formulation over an escaped pipe for a copyable command) failed the promotion gate: one low-severity round is
      below its evidence threshold, and the rendering premise is unverified — so it stays a note here.
- [x] 3.5 Ask the owner for the manual review pass; commit, push, archive and merge only after approval. **Done**: the
      owner approved on 2026-10-22 after the manual review request.
