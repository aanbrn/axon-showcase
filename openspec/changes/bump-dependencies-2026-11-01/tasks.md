# Tasks

Workspace: unit=bump-dependencies-2026-11-01;
path=/Users/afanasyev/Development/axon-showcase-bump-dependencies-2026-11-01; branch=bump-dependencies-2026-11-01;
writer=ses_e6b1f52c7ffeyEEVfqUEhwOxvr.

## 1. Pre-write derivation

- [x] 1.1 Run the `review-gate` skill's first-stage derivation pass: seed the claims table in `design.md` with
      `scripts/claim-scan.py`, resolve each candidate into a row or the recorded dismissal, and re-run every command a
      row names after the last edit. **Done**: `scripts/claim-scan.py --base origin/main` seeded the table; its
      candidates are the lockfile's integrity hashes and package URLs (npm's serialization, dismissed) and prose words,
      and each substantive claim is a row. The rows were re-derived after the last edit to the artifacts they name.

## 2. The bumps

- [x] 2.1 Re-resolve the three reports with the checks' own run —
      `./gradlew dependencyUpdates :showcase-web-ui:npmOutdated buildLogicUpdates` — rather than read the weekly tracker
      issue. **Done**: the run names the same five catalog rows, eight in-range npm rows plus one major, and five plugin
      report rows (four pins) the issue carries; the wrapper line reads `Gradle: [9.8.2: UP-TO-DATE]`.
- [x] 2.2 Confirm each resolved version at its registry: Maven Central's `maven-metadata.xml` per catalog coordinate and
      the plugin portal's metadata per plugin pin. **Done**: `logback-classic`/`logback-core` `1.6.6`, `handlebars`
      `4.5.8`, `zstd-jni` `1.5.7-25` and `swagger-ui` `5.34.1` are each the last entry of their metadata;
      `gradle-versions-plugin` `0.67.0`, `net.ltgt.errorprone` `5.2.0`, `lombok-plugin` `9.8.3` and `helm-plugin`
      `3.3.0` likewise (the portal lists `3.2.1-beta3` above `3.3.0`, which the stability filter rejects).
- [x] 2.3 Set the four catalog coordinates and the four plugin pins in `gradle/libs.versions.toml`. **Done**: eight
      `[versions]` lines changed, nothing else in the file; `helm-releases-plugin` reaches `3.3.0` through the shared
      `helm-plugin` version ref.
- [x] 2.4 Run the web UI's npm update and describe the lockfile diff by what moved; confirm `package.json` is unchanged.
      **Done**: 72 packages moved, one added (`fdir`, hoisted out of `tinyglobby`) and eleven removed (the
      `unrs-resolver` wasm fallback chain with the `tslib` it carried, plus four nested dedupes); the lockfile's root
      block is identical to `HEAD`'s. The moved set is led by `@tanstack/react-query`/`query-core` `5.105.0`,
      `react-redux` `9.3.1`, `eslint` `10.14.0`, `eslint-plugin-boundaries` `7.4.0`, `@boundaries/elements` `3.3.0` and
      the `typescript-eslint` set `8.72.1`.
- [x] 2.5 Record the excluded major (`@hookform/resolvers`) and the parked `handlebars` constraint as Non-Goals rather
      than acting on them. **Done**: both are in `design.md`'s Non-Goals, and `docs/ideas.md`'s 2026-10-25 entry stays
      the record for the constraint.

## 3. Verification

- [x] 3.1 Re-measure the two `gradle-build` pack claims against `helm-plugin` `3.3.0`: the `Project.getProperties()`
      deprecation with a deprecation trace, and each workflow-invoked helm task's configuration-cache store and reuse,
      plus `helmInstallToCi`'s store. **Done**:
      `./gradlew help --no-configuration-cache --warning-mode all -Dorg.gradle.deprecation.trace=true` still prints the
      warning, twice, from
      `io.github.build.extensions.oss.gradle.plugins.helm.HelmPlugin.createRepositoriesFromProjectProperties`; a run
      over a reused cache entry prints none, so the pack's note is updated to `3.3.0` and gains that condition.
      `:helm:chart:helmPackageMainChart` stores an entry (`/tmp/helm-cache-1.log`), re-stores once because a task
      creates a watched `Chart.yaml` (`/tmp/helm-cache-2.log`), and the third run reuses it (`/tmp/helm-cache-3.log`);
      `helmInstallToCi --dry-run` stores an entry (`/tmp/helm-install-dry.log`), so no task action captures a script
      object and the 3.3.0 `tags` API (`SetProperty`) is configured by the seven `release { }` blocks. The install
      path's action itself needs a cluster and is not run here.
- [x] 3.2 Run the full `./gradlew check` (integration tests on Testcontainers, coverage gate enabled) and the web UI's
      `check`, `build` and `npmAudit`. **Done**: `check` green in 5m 45s (410 tasks; the four services' integration
      tests, every component suite and `jacocoTestCoverageVerification` executed); `:showcase-web-ui:check`, `build` and
      `npmAudit` green — 25 Vitest files and 118 tests, the bundle emitted under `build/dist/assets/`, and no stale
      `npm-audit-ignores.json` entry.
- [x] 3.3 Re-run the reports and confirm the bumped rows are gone. **Done**: the catalog report has no "later release
      versions" section at all, the wrapper line reads `UP-TO-DATE`, `buildLogicUpdates` reports no rows, and the npm
      report lists only `@hookform/resolvers`. The re-run also proves `gradle-versions-plugin` `0.67.0` still emits the
      two section headers the workflow's `awk` keys on: a temporary `swagger-ui = "5.34.0"` pin reproduced
      `- org.webjars:swagger-ui [5.34.0 -> 5.34.1]` through the workflow's own extraction program before the pin was
      reverted.
- [x] 3.4 Sweep the documents for each bumped version and for the `helm-plugin` claims the pack states. **Done**: the
      `git grep` sweep over every old version finds the `gradle-build` pack's present-tense `3.2.0` claim (updated to
      `3.3.0` with its new condition), its historical "needed 3.2.0" sentence (made `3.2.0` or newer), a test fixture's
      sample versions, ADR-0014's past-tense `eslint-plugin-boundaries` incident (a different package), and the dated
      `docs/guidance-evidence/` probe record, which keeps the environment it ran on.
- [x] 3.5 Run `./gradlew spotlessApply`, then `spotlessCheck`, `openspec validate --all`, and the 120-character check
      over the change's whole changed set, after the last edit to a gated file. **Done**: `spotlessApply` then a
      standalone `spotlessCheck` exit 0, `openspec validate --all` reports `27 passed, 0 failed`, and the 120-character
      recipe over the changed set flags only the catalog's `group:name` declarations and the claims table's rows — the
      two shapes the formatter cannot wrap. Re-run after the closing edits.
- [x] 3.6 Refresh the branch onto the advanced `origin/main` and re-run the gates. **Done**: `origin/main` had moved one
      commit (`5718c778`, the kps chart bump), so the tracked edits were stashed, the branch fast-forwarded and the
      stash re-applied; that commit's `prometheus-community-stack` line and this unit's eight keys both survive the
      catalog merge, `docs/review-rounds.tsv` carries both units' rows, and `spotlessCheck`, the three verify tasks,
      `openspec validate --all`, the ledger checks and the full `./gradlew check` are green on the refreshed base.

## 4. Review gate

- [x] 4.1 Run the `review-quick` loop over the proposal and the implementation, repeating until it reports no new
      observations, with each round's row appended to `docs/review-rounds.tsv` and both ledger checks run after each
      append. **Done**: the rounds' classes and states are `docs/review-rounds.tsv`'s rows for this unit, which carry
      the reviewer session's identifier; both ledger checks are run after each append, and `--expect-clean` at the
      boundary.
- [x] 4.2 Derive the deep-review routing with `scripts/review-routing.py`, record its outcome and signals here, and run
      the routed pass when it is `REQUIRED` (or a `RECOMMENDED` outcome the owner resolves to run). **Done**:
      `python3 scripts/review-routing.py --unit bump-dependencies-2026-11-01 --change bump-dependencies-2026-11-01` →
      `NOT_NEEDED`, with no matched signal (catalog pins, a lockfile reconciliation, one subject-pack note); no
      deep-review session was created.
- [x] 4.3 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net delta
      here. **Done**: two proposals applied, both merges into rules that already own the subject. The `gradle-build`
      pack's configuration-cache bullet now states that a reused entry replays configuration rather than running it, so
      a configuration-phase warning is absent from it, and points at the deprecation bullet that owns the probe;
      `AGENTS.md`'s docs-refresh bump clause adds that a sentence which is not a present-tense current value is not
      retargeted — an example is de-versioned, a minimum requirement widened to `"<old> or newer"`. Applied net delta,
      measured with `git diff --numstat`: `AGENTS.md` +2 lines (11 added, 9 removed, the reflow included), the pack +6
      lines (26 added, 20 removed, the single-sourcing trim and the reflow included), no retirements;
      `verifyCapturedMarkers`, `verifyInstallCommands` and `verifyDocumentedPins` pass.
- [x] 4.4 Ask the owner for the manual review pass, after 4.1 is clean and 4.3 is done. **Requested** at the end of the
      implementation turn, with the tree staged; nothing is committed, pushed, archived, or merged before the answer.

## 5. Process defects

- [x] 5.1 Write `docs/process-defects/bump-dependencies-2026-11-01.md`, recording what the unit observed or stating that
      it observed none. **Done**: two records — the full gate ran before the change's markdown was formatted, which cost
      one discarded run; and a gate result was recorded before the next edit to the same file falsified it.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The Helm (#40), buildpack (#211), and tooling (#308) findings are separate units, as before.
