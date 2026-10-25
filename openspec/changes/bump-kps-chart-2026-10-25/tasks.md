# Tasks

Workspace: unit=bump-kps-chart-2026-10-25; path=/Users/afanasyev/Development/axon-showcase-bump-kps-chart-2026-10-25;
branch=bump-kps-chart-2026-10-25; writer=ses_e8d43c99fffei7m5iah18dlGpp.

## 1. The bump

- [x] 1.1 Re-resolve the chart with the check's own run (`./gradlew helmUpdates`, which refreshes the chart repositories
      before searching) rather than a hand-run `helm search repo`, whose local index read stale on a previous bump.
      **Done**: `prometheus-community-stack: 92.11.0 -> 92.13.1`, still current, and the same version read from the
      chart repository's published `index.yaml`.
- [x] 1.2 Set the `prometheus-community-stack` coordinate in `gradle/libs.versions.toml` to the resolved version, as a
      concrete pin. **Done**: `92.11.0` → `92.13.1`.
- [x] 1.3 Move the manual `helm install kps … --version` command in `.opencode/skills/deployment-operations/SKILL.md` to
      the same version, and pass `./gradlew verifyInstallCommands`. **Done**: the command quotes `92.13.1`, and
      `verifyInstallCommands` reports BUILD SUCCESSFUL.
- [x] 1.4 Confirm the old pin survives only in dated records: grep `92\.11\.0` across the tracked set. **Done**:
      `git grep -n 92\.11\.0 -- ':!openspec/changes'` exits 1; the only tracked sites are in the archived
      `2026-10-22-bump-kps-chart-2026-10-22` change dir, which records what ran then.

## 2. Verification

- [x] 2.1 Re-run the check that produced the finding (`./gradlew helmUpdates`) and confirm it reports no Helm updates.
      **Done**: "No Helm updates available."
- [x] 2.2 Install the bumped chart live and read the deployed signal, as the `infra-image-versions` scenario requires.
      **Done**: `kps` upgraded to revision 2 on `kube-prometheus-stack-92.13.1`, read from one command each:
  - `./gradlew helmInstallKpsToLocal` — the upgrade itself.
  - `helm list -n monitoring` — revision 2 at chart `92.13.1`.
  - `kubectl get pods -n monitoring` — Grafana 3/3, kube-state-metrics, operator, prometheus-kps-0 2/2 and tempo-0 1/1,
    all Running.
  - Prometheus's `/api/v1/targets?state=active` through the service proxy — 14 active, 14 up, 0 down.
  - A port-forwarded `svc/kps-grafana`'s `/api/datasources` and `/api/search` — Prometheus, Tempo, and the
    `Axon Showcase` dashboard.
  - The chart-repository fetch failed intermittently during this unit and the retry succeeded; `design.md`'s Risks
    carries the evidence and leaves the cause unestablished.
- [x] 2.3 Run `./gradlew spotlessApply`/`spotlessCheck` and `openspec validate --all`; run the manual 120-character
      check over the change's whole changed set. **Done**: all green, re-run after the last edit to a gated file; the
      changed set is derived from `git status --porcelain --untracked-files=all`.

## 3. Review, capture and the manual pass

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`, seeded by
      `scripts/claim-scan.py`, with every candidate resolved into a row or a class-level dismissal. **Done**: the table
      and the class-level dismissal are in `design.md`; every row names the command or artifact that reproduces it.
- [x] 3.2 Run a `review-quick` pass over the proposal and the implementation, appending each round's row to
      `docs/review-rounds.tsv` and running the ledger checkpoint after each append; repeat until a round reports nothing
      new. **Done**: eight rounds in reviewer session `ses_e8d3491e7ffeJ4MTk34fkzCNor`, clean at rounds 3, 5, 6, 7 and 8
      — round 1 raised five findings (`missing-task`, `contradiction`, `claim-unreproducible`, `wording`, `formatting`),
      round 2 one `wording` repeat, round 4 two (`over-claim`, a `claim-unreproducible` repeat) after the capture's
      additions joined the diff, round 6 confirmed the post-round-5 attribution correction the technical-advisor
      consultation prompted, and round 7 re-screened the unit after the branch was refreshed onto `origin/main`'s
      `528fdcce`, and round 8 after a second refresh onto `23d085b6`; each row was appended as its report arrived, with
      both ledger checks run after every append.
- [x] 3.3 Derive the deep-review routing outcome with `scripts/review-routing.py`, decide at or above the floor, and
      record the outcome and its signals here. **Done**: the derivation returns `RECOMMENDED` via
      `[repeated-class] 2 rows repeat a prior class` — rounds 2 and 4 each carry a non-empty repeat column. The earlier
      record here named `NOT_NEEDED`, which was the tool's output while only round 2's repeat existed; the loop's own
      later rows moved the floor, so the recorded outcome follows the current derivation rather than the earlier one.
      The owner resolved to run the deep review, and its first pass (a manual `/review-thorough`, fix verification,
      session `ses_e8d10b57afferNBZuFA5OrL3H1`) reported two findings — **1 (blocking)**, this task's record sat below
      the floor, fixed by this record; **2 (nitpick)**, `docs/review-rounds.tsv` was left half-staged by the branch
      refresh's `stash pop`, fixed by unstaging the set — while independently confirming the other eight fixes and the
      refresh. Pass 2 closed the two-pass budget with `FIXED_AND_VERIFIED` for both findings and no new findings: the
      derivation was re-run and still prints `RECOMMENDED`, 3.3's outcome and signal match it verbatim, and
      `git diff --cached` is empty with all seven ledger rows in place.
- [x] 3.4 Decide the `lesson-capture` run against its skip condition; when it does not hold, run the subagent, apply its
      durable proposals, and record the applied net delta — or record the skip with its reason. **Done**: the skip
      condition fails (round 1 carried `contradiction`, `claim-unreproducible` and `missing-task`, and the diff touches
      the `deployment-operations` pack), so the subagent ran. Applied: a new bullet in the `deployment-operations` pack
      stating that only `AbstractHelmRepositoriesTask`'s tasks retry a chart-repository fetch, so a failed
      `helmInstall<Release>ToLocal` is retried by hand rather than blamed on the pin (+9), and the Workspace bullet's
      `writer` clarified to the writing session's own `ses_…` identifier (`AGENTS.md` +5/−4, net +1 — a precision
      correction inside an existing rule that round 1's `missing-task` finding shows already failed, not a new rule).
      Routed to a check rather than prose: the inline-code-span whitespace mode, added to `docs/ideas.md`'s 2026-10-25
      markdown-integrity entry (+8). No rule was retired; the capture's own finding — the claims table's dismissal
      listing the scan's output lines instead of its four shapes — is corrected in `design.md`.
- [ ] 3.5 Ask the owner for the manual review pass; commit, push, archive and merge only after approval.

## Workflow follow-up

- The change's archive commit rides this PR, after the `build` check is green.
- Issue #40 stays open and carries no closing keyword: the `helm-updates` workflow updates the open issue's body, so its
  next run rewrites it to "No Helm updates available." instead of opening a fresh issue over an orphaned history. The
  dependency, buildpack (#211) and tooling (#308) findings are separate units.
