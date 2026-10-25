# Tasks

Workspace: unit=bump-dependencies-2026-10-25; path=/Users/afanasyev/Development/axon-showcase;
branch=bump-dependencies-2026-10-25; writer=ses_e8d4a9bbeffeOGf9vlyJH9zD70.

## 1. The bumps

- [x] 1.1 Bump the fourteen catalog keys in `gradle/libs.versions.toml` to the versions the re-resolved
      `dependencyUpdates` report names: `lz4-java` `1.12.0`→`1.12.1`, `jackson2-bom` `2.22.3`→`2.22.4`, `jackson3-bom`
      `3.2.3`→`3.2.4`, `handlebars` `4.5.5`→`4.5.6`, `zstd-jni` `1.5.7-23`→`1.5.7-24`, `errorprone` `2.50.0`→`2.51.0`,
      `nullaway` `0.14.2`→`0.14.3`, `grpc` `1.85.0`→`1.85.1`, `micrometer-bom` `1.17.1`→`1.17.2`,
      `micrometer-tracing-bom` `1.7.1`→`1.7.2`, `reactor-bom` `2025.0.7`→`2025.0.8`, `reactor-blockhound`
      `1.0.17.RELEASE`→`1.0.18.RELEASE`, `springdoc-openapi-starter` `2.9.1`→`2.10.0`, `swagger-ui` `5.33.2`→`5.34.0`.
      `reactor-bom` moves only in its service-release segment, so it stays inside the `2025.0` train and is a minor by
      the report's calendar-version rule. **Done**: fourteen `[versions]` lines changed, nothing else in the file.
- [x] 1.2 Run the web UI's npm update (`./gradlew :showcase-web-ui:npm_update`) and describe the lockfile diff by what
      moved; the report's in-range rows are `react-hook-form` and `eslint-plugin-boundaries`, while
      `@hookform/resolvers` `6.0.0` is a manifest major and stays out (it is a migration with its own decision, as the
      2026-10-22 npm unit recorded). **Done**: 13 packages moved, 5 removed (the `micromatch` chain `braces`→
      `to-regex-range`), 0 added, and two entries flip `dev`→`devOptional` (`fast-deep-equal`, `require-from-string`);
      `package.json` unchanged; `npm audit` reported `found 0 vulnerabilities`. `npm_update` is the npm plugin's dynamic
      `npm_<command>` pattern task — it runs `npm update`, and `:showcase-web-ui:tasks --all` lists only the pattern
      line, so its absence from that list is not evidence it does not exist (`--dry-run` resolves it).
- [x] 1.3 Bump the wrapper to `9.8.2` with two `./gradlew wrapper --gradle-version 9.8.2 --distribution-type all` runs
      (the second executes under the new version and regenerates the jar), then diff all four wrapper files and confirm
      the distribution stays `-all`. **Done**: only `gradle-wrapper.properties` changed; `./gradlew --version` reports
      `Gradle 9.8.2`; `distributionUrl` still ends `-all.zip`; the jar's SHA-256 equals the 9.8.2 distribution's own
      `gradle-wrapper-main-9.8.2.jar` member, so the regeneration produced an identical jar rather than a stale one.
- [x] 1.4 Probe whether Error Prone `2.51.0` still emits the `UnrecognisedJavadocTag` false positive on the Lombok
      classes: remove their `@SuppressWarnings` and compile. **Done**: it no longer fires — 18 warning lines at `2.50.0`
      against 0 at `2.51.0` on the same five files with the suppressions removed, with a deliberate `@Nullable`
      dereference as the positive control that Error Prone ran. The five suppressions are deleted and the
      `jvm-development` pack's upstream reference retires with them.

## 2. Verification

- [x] 2.1 Run the full `./gradlew check` (integration tests on Testcontainers, the coverage gate enabled) — the tier the
      runtime-path coordinates need — and `./gradlew :showcase-web-ui:build` for the bundle path. **Done**: `check`
      green under `9.8.2` (409 tasks, 4m19s, the integration tier executing for the four services and the coverage gate
      enabled); the web UI `build` green and `build/dist` emitted with the moved `react-hook-form`. The first attempt
      failed at `:helm:chart:helmUpdateMainChartDependencies` (the Helm CLI's repository fetch exiting 1); the task ran
      green on a bare re-run, so it was a transient repository fetch rather than the bump — the run is recorded here
      rather than silently retried.
- [x] 2.2 Run the frontend `check` and `npmAudit`: the lint gate covers the moved ESLint plugins, and `npmAudit` passing
      is the evidence that no `npm-audit-ignores.json` entry went stale. **Done**: both green — the lint gate accepted
      `eslint-plugin-boundaries` `7.3.0`'s rules unchanged, and the ignore list stays empty with no stale entry to fail
      on.
- [x] 2.3 Re-run the reports and confirm the bumped rows are gone: `dependencyUpdates` (its wrapper line reads
      `UP-TO-DATE`) and `:showcase-web-ui:npmOutdated` (only the deferred `@hookform/resolvers` major remains).
      **Done**: the catalog report carries no "later release versions" section and its wrapper line reads
      `Gradle: [9.8.2: UP-TO-DATE]`; the npm report lists `@hookform/resolvers` alone.
- [x] 2.4 Sweep the documents for each bumped version: update the live current-value mentions (`AGENTS.md`'s and
      `openspec/config.yaml`'s Gradle version, `README.md`'s wrapper pin, the `reactor-bom` example in `README.md` and
      the `gradle-build` pack, and the `docs/ideas.md` NullAway parenthetical), retarget ADR-0014's `braces` consequence
      to the chain's removal, and leave the dated records (`docs/audits/*`, the Snyk fix `3.2.3` mention) as recorded.
      **Done**: seven sites in this task — `AGENTS.md`, `openspec/config.yaml`, `README.md` twice, the `gradle-build`
      pack, `docs/ideas.md` and ADR-0014 — with the `jvm-development` pack's site belonging to 1.4; each was read
      against the version it names, and `docs/guidance-evidence/`'s `braces` record stays accurate because its
      references' statuses did not move.
- [x] 2.5 Sweep the suppressions: `.snyk`'s ignores are all Spring and Validation coordinates, and the npm side is
      covered by 2.2's `npmAudit` pass. **Done**: no `.snyk` entry names a moved coordinate; the npm ignore list is
      empty.
- [x] 2.6 Run `./gradlew spotlessApply`, then `spotlessCheck`, and the 120-character check over the change's whole
      changed set, after the last edit to any gated file. **Done**: `spotlessApply` then a standalone `spotlessCheck`
      green after the last edit to this file (the first attempt went red on this file's own re-wrap — the tick that
      claims the gate is the edit that falsifies it), and the 120-character check's only over-length lines are markdown
      table rows (the claims table, `AGENTS.md`'s pack table, `README.md`'s tables) and the catalog's long `group:name`
      lines, none of which the formatter can wrap.

- [x] 2.7 Run the opt-in e2e tiers the change can reach, at the owner's request. **Done**:
      `:showcase-api-gateway:e2eTest` green — 29 tests across six nested suites, 0 failures and 0 errors, exercising the
      full command → Kafka → projection → query pipeline over the rebuilt images; `:showcase-web-ui:e2eTest` green
      against the compose stack (Playwright with `retries: 0`, so any failing case fails the task; its
      `test-results/.last-run.json` reads `"status": "passed"` with an empty `failedTests`, and no trace was retained
      because nothing failed). This closes the residual question the technical advisor left open — whether the
      cross-service chain works on the new versions — and covers the bundle path the moved `react-hook-form` reaches.

- [x] 2.9 Investigate the CI-only wedge the bumped tree hit in the gateway's two `@WebFluxTest` slices. **Done**: the
      bumped tree failed twice in three CI runs (38 `ShowcaseRestControllerCT` plus 7 `TelemetryControllerCT`
      `TimeoutException`s, 45 dumps, none with a thread in the request path) while the same suites are green locally six
      times and three more under CPU saturation. A CI bisect put `reactor-blockhound` `1.0.18.RELEASE` under suspicion
      and a hold was applied and carried through a green control run (2.10); the owner then withdrew the hold: each cell
      is one to three runs, and the unit lands the in-flight dump (2.11) instead, so the latest BlockHound ships and the
      next stall names its own mechanism.
- [x] 2.10 Run the control CI run of the then-held configuration before the unit finished, and stop on a repeat.
      **Done**: `51dad975` (with `reactor-blockhound` at 1.0.17) green in 12m29s with the gateway component tests
      executed — the run the hold rested on before the owner withdrew it in 2.9.
- [x] 2.11 Land the diagnostic the next occurrence needs, since the failure-time dump shows only the aftermath.
      **Done**: `ThreadDumpWatchdog` (component-test source set, registered by autodetection beside
      `ThreadDumpOnFailure`) snapshots every thread while a test runs — at 3 s, then every 7 — and emits nothing for a
      test that finishes sooner, so the suites stay quiet on green. The next failing CI run's
      `=== thread dump while running: … ===` marker is the data that names what the request waited for; reading it stays
      with the parked defect, which this unit leaves open. **Controls**: a temporary probe (not committed) emitted one
      marker for a 5 s plain `@Test` and one for a 5 s `@ParameterizedTest` case, and none for a 20 ms case; the three
      suites run with the watchdog registered emit no marker and stay green — the passing-run case; cancellation rests
      on the code read (`cancel` + `shutdownNow` in a `finally`), not on an assertion.
- [ ] 2.12 Publish the upstream report only on the technical advisor's verdict. The draft is complete and its
      attribution corrected, but publication is an outward-facing act: the owner requires the advisor's final verdict
      (publish or not) before the issue is filed, and the issue number is then cited here and in `docs/ideas.md`.
- [x] 2.8 Refresh the branch onto the advanced `origin/main` and re-run the gates. **Done**: `origin/main` had moved
      four commits (`7f76216b`: buildpack pins, the kube-prometheus-stack bump, the closure-disposition report, the
      tooling pins), so the work was stashed, the branch fast-forwarded `837aaa52`→`7f76216b`, and the stash re-applied
      as a 3-way merge. `docs/review-rounds.tsv` conflicted (both sides append rows) and was resolved by keeping both
      blocks; everything else auto-merged, including the catalog's 14 keys against main's 6 pin lines. The refreshed
      tree was then re-gated (`spotlessCheck` and the full `check`), since main changed `build-logic` and the catalog. A
      second refresh followed the merge of `stage-new-files-before-manual-review` (`3456bcb1`), by the same mechanism
      with the ledger the only conflict; the unit's new files are staged per the review gate's §5 with its tracked edits
      left unstaged, and the gates were re-run on that base.

## 3. Review gate

- [x] 3.1 Record the pre-write derivation pass: the claims table is in `design.md`, seeded with `scripts/claim-scan.py`
      and re-derived after the last append to it.
- [x] 3.2 Run the `review-quick` loop over the proposal and the implementation, repeating until it reports no new
      observations, with each round's row appended to `docs/review-rounds.tsv`. **Done**: ten rounds, the last clean;
      the rows are in `docs/review-rounds.tsv` and both ledger checks pass.
- [x] 3.3 Derive the deep-review routing with `scripts/review-routing.py`, record its outcome and signals here, and run
      the routed pass when it is `REQUIRED` (or a `RECOMMENDED` outcome the owner resolves to run). **Routing**:
      `REQUIRED`, signal `public-contract` on `showcase-projection-model/.../ShowcaseEntity.java` (the annotation
      deletion). **Pass 1** (initial, session `ses_e8d0b734dffemc4qQRSblHrLNY`) raised four findings: 1. _should-fix_ —
      the Goal claimed the tiers of all five BlockHound suites while the gateway e2e tier was not run. Status: `FIXED`
      (the Goal names the four tiers `check` runs and the gateway e2e harness as the separate opt-in tier the owner
      later asked to run — task 2.7; the proposal's Impact bullet said so from the start). 2. _should-fix_ —
      `openspec/specs/showcase/quality/dependency-management`'s calendar-versioning scenarios still illustrated their
      rule with `reactor-bom` at `2025.0.7`. Status: `FIXED` (a `MODIFIED` delta de-versions the two examples instead of
      swapping numbers; `skip_specs` dropped from `.openspec.yaml`). 3. _nitpick_ — the proposal's "the only non-version
      change" omitted `@napi-rs/wasm-runtime`'s narrowed `@emnapi` peer ranges. Status: `FIXED` (named in the same
      bullet). 4. _nitpick_ — "all sixteen JVM modules" against the repository's 18-module count. Status: `FIXED` (the
      boundary is stated: the sixteen main-build JVM subprojects, the `platform` BOM included, with `build-logic` and
      `helm` excluded). A later step ran both e2e tiers (task 2.7), so the deferred-coverage limitation this finding
      named is closed as well as worded honestly. **Pass 2** (independent verification, same session) confirmed all four
      fixes hold — the delta's header, description and five scenarios are verbatim-preserved with only the two example
      `WHEN` values de-versioned, and `openspec validate --all`/`--changes` are green — and raised two prose findings:
      the proposal's BlockHound bullet said `check` runs "the first three" of five suites, and its spec bullet pointed
      at "the delta above" (no referent) while naming the main-spec path. Both are `FIXED` in the proposal (four of
      five, and the delta path named). No third pass was run: the residual risk is prose-only with no behavioral or
      verification consequence, which is the recorded decision the pass budget asks for at the limit. Status: all six
      deep-review findings `FIXED`.
- [x] 3.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net delta
      here. **Done**: three proposals applied. `AGENTS.md`'s docs-refresh version-bump clause now greps the whole
      working tree (which is how `openspec/specs/` was missed here) and de-versions an illustrative example instead of
      re-versioning it; the `jvm-development` pack gains a bullet on probing a suppression's redundancy at the
      diagnostic's own severity; and the `gradle-build` pack's "name the mechanism, not the constraint's version" clause
      points at the `AGENTS.md` de-versioning rule instead of restating it. The capture's retirement (the Error Prone
      suppression clause and its `google/error-prone#5855` reference) was already in this diff, and its
      `incomplete-enumeration` and spotless-tick lessons were reported as compliance misses already covered. Net
      `AGENTS.md` delta: **+2 lines** (17 added, 15 removed, the clause's paragraph reflow included). The `README.md`
      and `gradle-build` `reactor-bom` examples were folded into the new de-versioning rule as part of applying it.
- [x] 3.5 Ask the owner for the manual review pass, after 3.2 is clean and 3.4 is done. **Requested**; the owner then
      handed the unit to the `technical-advisor`, whose report recommended retaining all three of its judgement calls
      (the suppression retirement, the spec delta, the e2e deferral) and asking the owner for explicit approval with the
      e2e named first — 0 confirmed problems, 1 open question (whether the cross-service chain works on the new
      versions). The owner chose to run the tests, so task 2.7 answers that question and the approval request stands.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The Helm (#40), buildpack (#211), and tooling (#308) findings are separate units, as before.
- [x] Park the dead `handlebars` `:platform` constraint (no consumer in any service's resolved classpath, no recorded
      rationale) in `docs/ideas.md` as its own unit rather than removing it here. **Done**: the 2026-10-25 entry states
      what was verified and why the removal is the owner's decision.

## Review hand-over

Recorded before each reviewer invocation and updated per round; a replacement uses it as context.

- **Unit**: `bump-dependencies-2026-10-25`. **Base**: `3456bcb1` (`origin/main`; refreshed from `837aaa52` by two
  fast-forwards on 2026-10-25, task 2.8). **Current**: the uncommitted working tree on branch
  `bump-dependencies-2026-10-25`.
- **Working tree**: `/Users/afanasyev/Development/axon-showcase` (primary; writer `ses_e8d4a9bbeffeOGf9vlyJH9zD70`).
- **Artifacts**: the five change files — `proposal.md`, `design.md`, `tasks.md`, `.openspec.yaml`, and the `MODIFIED`
  delta at `specs/showcase/quality/dependency-management/spec.md`; plus the delivered diff — the catalog, the wrapper
  properties, the lockfile, five Java sources losing an annotation, the new `ThreadDumpWatchdog` component-test
  extension and its service-file registration, the doc edits, and the ledger row set.
- **Scope**: the planning artifacts (the first round) and the implementation with every later round's residue, refreshed
  onto `origin/main` twice. The per-round class lists are the ledger's rows for this unit; this block does not restate
  them, so it stays current as the loop advances.
- **Prior findings**: the per-round class lists are `docs/review-rounds.tsv`'s (this unit's rows); this block carries
  only the dispositions the ledger does not. Applied: the suppressed-major claims wording, the `handlebars` evidence and
  its `docs/ideas.md` entry, the BlockHound reach and its claims row (six declarations, five install sites), the LZ4
  substitution attribution, the two dismissal bases, the `dev`→`devOptional` flips and the `@emnapi` peer-range
  narrowing, the Goal's tier pointer, the 2.4 site count, the Goal's e2e statement, the delta's third edit reverted so
  only the two examples move, the doc-site reclassification with the delta added, the module-set wording ("main-build
  JVM subprojects", the `platform` BOM included) applied at every site, and the deep-review record's e2e parenthetical.
  Refuted with evidence: `:showcase-web-ui:npm_update` is the npm plugin's dynamic `npm_<command>` pattern task
  (`--dry-run` resolves it), so the round that raised it was reporting a false positive.
- **Completed checks**: the two reports re-resolved and re-run clean, each version confirmed at its registry, the Error
  Prone A/B with its NullAway control, `check` green under `9.8.2` on the original and both refreshed bases, the web UI
  `build` and `npmAudit`, `:showcase-api-gateway:e2eTest` (29 tests, 0 failures) and `:showcase-web-ui:e2eTest` both
  green, `spotlessCheck` after the last edit, `openspec validate --all` (27 items), both ledger checks, and the CI
  bisect with its control run (2.9/2.10).
- **Known limitations**: the diff is uncommitted. The full `check` and both e2e tiers ran at the delivered versions
  (`reactor-blockhound` `1.0.18.RELEASE`) but **before** the watchdog and the registration control existed; those two
  are covered by the three gateway suites locally (green with the watchdog registered) and by the probe controls in
  2.11, and CI is their first full run. The bisect's held configuration (`51dad975`) is green but is not the delivered
  one. The two refreshes also brought other units' build changes this unit did not author (the builder/nginx and
  kube-prometheus-stack pins, the `build-logic` edits), so "only docs moved" would be the wrong reason to carry earlier
  results forward: they still apply because the API and browser paths this unit touches are unchanged, the web UI suite
  runs against `vite preview` rather than the rebuilt nginx image, and the builder/nginx pair was verified by its own
  change. The cold-run `:helm:chart:helmUpdateMainChartDependencies` fetch failure recurs and passes on a bare retry
  (recorded in 2.1/2.8).
- **Subject packs**: `gradle-build`, `deployment-operations`, `jvm-development`, `jvm-testing`.
