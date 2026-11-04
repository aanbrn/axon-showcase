# Tasks — projector operating-state panels

The unit's record is `docs/units/add-projector-state-dashboard-panels/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with `python3 scripts/claim-scan.py` over the staged
      artifacts; each candidate is resolved into `design.md`'s claims table or dismissed beside it, which states one
      reason per candidate kind.

## 1. The dashboard panels

- [x] 1.1 `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`: add the three panels at `gridPos.y`
      `104`, each 7 rows high, on ids 84, 85, and 86, with the `DS_PROMETHEUS` datasource variable and the
      `application="$application"` / `service="axon-showcase-projection-service"` selector — **Projector State**
      (`stat`, 6 columns, an instant query with `range` false and `lastNotNull`, the state gauge with
      Stopped/Running/Retrying value mappings and matching threshold colors, and `noValue` "No data" on the neutral base
      color), **In-flight Batches** (timeseries, 6 columns, `showcaseProjector_processing`), **Since Last Resolved
      Batch** (timeseries, 12 columns, `time() - (showcaseProjector_last_progress_seconds{…} > 0)`, unit seconds, no
      threshold, legend "since last resolved batch (idle time included)") — and move the 18 panels at `gridPos.y >= 104`
      down by 7 rows. Verify: the file parses, and the counter of the claims table's layout rows, re-run on the edited
      file, prints 42 non-row panels, 7 rows, and 0 overlaps. Verified: the counter over the edited file prints 42
      non-row panels, 7 rows, 0 overlaps, ids up to 86, and the lowest panel bottom is 170.
- [x] 1.2 Give each new panel a `description` stating what it reports: the state panel's the source's own meaning (the
      projector has reached partition assignment since startup or the last stream failure, not that the broker is
      reachable or that progress continues), and the age panel's the boundary ADR-0023 records — a growing age cannot
      distinguish a stall from healthy idleness. Verify: each panel object carries a non-empty `description` with those
      statements, and the age panel's threshold mode is `off`. Verified: each of the three panels carries a non-empty
      `description`, and the age panel's `custom.thresholdsStyle.mode` is `off`.

## 2. The dashboard gate

- [x] 2.1 `build-logic/src/main/kotlin/DashboardJsonRules.kt`: check the parsed document's panels as `design.md`
      decision 4 states, keeping the file-level messages and the empty-directory failure unchanged. Verify: the rules
      object's public entry point still returns one message per violation, naming the file. Verified: the rules object
      returns a list naming the file and the panel, and the file-level messages for a malformed, empty, or
      trailing-content dashboard are unchanged.
- [x] 2.2 `build-logic/src/test/kotlin/DashboardJsonRulesTests.kt`: one known-bad fixture per condition the delta states
      — a repeated id where one panel sits inside a collapsed row's `panels`, a row panel that no longer meets the panel
      below it, a top-level panel without `gridPos`, a target whose `expr` is empty, a target that omits `expr`, and a
      panel whose first target carries a query beside a second target whose `expr` is blank (the shape an
      `any`-instead-of-`all` implementation would pass) — plus a known-good dashboard whose rows and panels tile the
      grid and whose panels include a `text` panel declaring no target. Verify: each bad fixture yields exactly one
      violation naming the file and the panel, and the known-good one yields none. Verified: 17 tests; removing each
      rule in turn fails only its own cases (1, 5, and 5 tests respectively).
- [x] 2.3 Run the gate and its tests. Verify: `./gradlew verifyDashboardJson` and
      `./gradlew -p build-logic test --tests 'DashboardJsonRulesTests'` pass on the edited dashboard; then, as the
      task-level control, copy the dashboard to a scratch path, duplicate a panel id in the working file, confirm
      `verifyDashboardJson` fails naming it, restore the file from the copy, and diff the two to prove the restore.
      Verified: both pass on the edited dashboard; duplicating id 84 fails the task naming 'Projector State' and
      'In-flight Batches', and the restored file diffs clean against the copy.

## 3. The producer-side name pin

- [x] 3.1
      `showcase-projection-service/src/test/java/showcase/projection/ShowcaseProjectorStateMonitorPrometheusTests.java`:
      scrape a `PrometheusMeterRegistry` after driving the monitor through running, a started batch, and a resolved one,
      and assert the three exposition names and their values, stating in the class comment that the dashboard's panels
      query those names. Verify: the test fails when a gauge's name is changed in the monitor and passes unchanged
      otherwise. Verified: the scrape asserts exactly the three names and the state/processing/progress values; renaming
      the state gauge fails the test.
- [x] 3.2 Run it: `./gradlew :showcase-projection-service:test --tests 'ShowcaseProjectorStateMonitorPrometheusTests'`.
      Verify: the test passes and the scrape it asserts carries `showcaseProjector_state`,
      `showcaseProjector_processing`, and `showcaseProjector_last_progress_seconds`. Verified: 1 test, 0 failures.

## 4. The specification and the docs

- [x] 4.1 Reconcile the delta's conditions with the implemented rules: every condition the `code-quality` requirement
      states maps to a rule in `DashboardJsonRules` and to a case in `DashboardJsonRulesTests`. Verify: a one-line
      mapping read against both files, with no condition left without a case. Verified: the requirement's repeated-id,
      missing-gridPos, overlap, and blank-target conditions each map to a rule and a case, and the malformed and
      directory cases keep their existing ones.
- [x] 4.2 `README.md`: the Grafana bullet states the panel count and the coverage. Verify:
      `grep -n "panels across" README.md` prints 42 panels and 7 sections, the figure equal to the dashboard file's
      non-row panel count, and the bullet names the projector's operating state. Verified:
      `grep -n "panels across" README.md` prints 42 panels and 7 sections, the count equal to the dashboard file's
      non-row panels.
- [x] 4.3 `.opencode/skills/deployment-operations/SKILL.md`: the "Chart validation" paragraph points at the dashboard
      gate and at the requirement that owns its rules. Verify: the paragraph names `verifyDashboardJson` and the
      capability, and copies no rule from it. Verified: the paragraph names `verifyDashboardJson` and the capability and
      states no rule of its own.
- [x] 4.4 `docs/ideas.md`: remove the parked entry this change implements. Verify:
      `grep -n "Add a Grafana panel for the projector" docs/ideas.md` prints nothing, and the `## 2026-10-24` section
      keeps its other entries. Verified: the grep prints nothing, and the `## 2026-10-24` section keeps its other
      entries.

## 5. Verification

- [x] 5.1 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`. Verify: the build succeeds with the dashboard gate,
      the rules tests, and the projection-service test in it. Verified: the standard `check` succeeds.
- [x] 5.2 Verify the deployed signal: `./gradlew :helm:chart:helmPackageMainChart`, then
      `helm upgrade axon-showcase helm/chart/build/helm/charts/axon-showcase -f helm/values/axon-showcase/values-local.yaml --namespace axon-showcase --wait`,
      then read the provisioned dashboard back from Grafana's API (`design.md`, "Verification"). Verify: the response
      carries 49 panels and the three new titles with the state panel's `instant`/`range`/`calcs`, its three value
      mappings, and its `noValue`; the age panel carries no threshold; the state query returns `1` for the running
      projector (so the panel shows Running rather than the raw value); and the same expression with a selector that
      matches no series returns no series, which is the panel's No-data case rather than `0`. Record that Retrying and
      Stopped were not induced live. Verified: the release is at revision 3 and Grafana returns 49 panels with the three
      titles, the state panel's instant/range/calcs, its mappings, and its `noValue`, and the age panel's threshold off;
      the state query returns 1, and a selector matching no series returns none. Retrying and Stopped were not induced
      live.
- [x] 5.3 `./gradlew spotlessApply` after the last edit, then `openspec validate --all`. Verify: both pass, and the tree
      is clean of formatter changes. Verified: both pass.

## 6. The review gate and the records

- [x] 6.1 The proposal stage's quick review: `review-quick` over the planning artifacts, looping until a round reports
      no new observations, with each round's row and finding instances recorded under
      `docs/units/add-projector-state-dashboard-panels/`. Verify: `--expect-clean` passes and `--session-continuity`
      passes. Verified: rounds 1–8 in `rounds.tsv`, the closing round 8 with no finding class; `--expect-clean`,
      `--session-continuity` and `--finding-instances` pass.

- [x] 6.2 The owner's manual pass over the proposal. Verify: the approval is recorded before any implementation edit.
      Verified: the owner approved the proposal and instructed the implementation before any implementation file was
      edited; the advisor's consultation the approval named ran first.
- [x] 6.3 The implementation stage's quick review: the same loop over the finished unit. Verify: the closing round is
      clean and the ledger checks pass. Verified: rounds 9–13 in `rounds.tsv`, the closing round 13 with no finding
      class; the deep pass's two nitpicks and round 12's three findings are applied, and `--expect-clean`,
      `--session-continuity` and `--finding-instances` pass.

- [x] 6.4 The deep-review routing decision:
      `python3 scripts/review-routing.py --unit add-projector-state-dashboard-panels --change add-projector-state-dashboard-panels`
      on the completed diff. Verify: the outcome and its signals are recorded on this task, and a `RECOMMENDED` outcome
      is put to the owner rather than decided here. Verified: `ROUTING: REQUIRED` — `[infrastructure-or-deployment]` on
      `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`; the deep review runs before completion.

- [x] 6.5 The deep review, when 6.4 selects one (`/review-thorough`), with its findings applied and re-reviewed. Verify:
      the pass's status and its findings' disposition are recorded on this task. Verified: pass 1 (routing-required)
      reported two nitpicks and no blocking finding — the task's class KDoc still described a JSON-only gate, and 6.1's
      box was unticked although its loop had closed — both addressed; the reviewer confirmed the panel queries, the grid
      shift, the rules' discrimination, the delta's clause trace, and the docs. 6.3 closes with the implementation
      loop's last clean round.

- [x] 6.6 The owner's manual pass over the finished unit. Verify: the approval is recorded, and nothing is committed,
      pushed, or merged before it. Verified: the owner reviewed the implementation and instructed delivery; nothing was
      committed before that approval.

- [x] 6.7 `docs/units/add-projector-state-dashboard-panels/process-defects.md`: one record per defect observed while
      working, or the statement that none was. Verify: the file exists before 6.6 is requested. Verified: the file
      exists with three records — a range edit that deleted a section heading, a control's `git checkout --` that
      discarded the uncommitted rules object, and a credential-shaped curl command that the source-secret scan reported
      on the pull request.
