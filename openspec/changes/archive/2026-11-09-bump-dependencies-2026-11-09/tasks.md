# Tasks

The unit's record is `docs/units/bump-dependencies-2026-11-09/unit.md`.

## 1. Pre-write derivation

- [x] 1.1 Run the `review-gate` skill's first-stage derivation pass: seed the claims table in `design.md` with
      `scripts/claim-scan.py`, resolve each candidate into a row or the recorded dismissal, and re-run every command a
      row names after the last edit. **Done**: `scripts/claim-scan.py --base origin/main` seeded the table; its path
      candidates are file, command and package names, its `absolute`/`count` candidates prose quantifiers and version
      digits, and its `cross-ref` candidates a product family, a formatter and this repository's tracker issue, so every
      substantive claim is a row and those kinds are the recorded dismissals. Each row was re-derived after the last
      edit to the artifact it names, and the implementation rounds re-derived the rows they changed.

## 2. The bumps

- [x] 2.1 Re-resolve the three reports with the checks' own run —
      `./gradlew dependencyUpdates :showcase-web-ui:npmOutdated buildLogicUpdates` — rather than read the weekly tracker
      issue, and keep the run's report files as the change's evidence. **Done**: run in this tree at the base revision;
      the catalog report names eleven rows, the npm report eight in-range rows and two manifest majors, and
      `buildLogicUpdates` four rows (three distinct pins). The run's report files are copied to
      `$TMPDIR/opencode/bump-2026-11-09/*-report-first.txt`.
- [x] 2.2 Confirm each resolved version at its registry: Maven Central's `maven-metadata.xml` per catalog coordinate and
      per plugin pin. **Done**: each target is the newest stable entry its own metadata serves — `lz4-java` `1.14.0`,
      `elasticsearch-java` `9.5.6`, the Spotless plugin marker `8.11.0`, `jackson-bom` `2.23.0`, `zstd-jni` `1.5.7-26`,
      `error_prone_core` `2.52.0`, `guava` `33.8.0-jre`, `netty-bom` `4.2.20.Final`, `opentelemetry-bom` `1.68.0`,
      `tools.jackson:jackson-bom` `3.3.0` and `protobuf-gradle-plugin` `0.10.1`.
- [x] 2.3 Check the selected releases' compatibility before any edit: LZ4 `1.13`/`1.14`'s release notes, Error Prone
      `2.52.0`'s checks against the pinned NullAway, and Spotless `8.11.0`'s formatter changes. Split a coordinate out,
      recording the split, if its release needs migration-sized source remediation, substantial non-formatting
      remediation, or a weakened check. **Done, no split**: LZ4 `1.13.0` is the upstream lz4 `1.11.0` update with
      tightened input validation, and `1.14.0` aligns the Java implementations with the native ones ("technically a
      breaking change, but unlikely to affect anyone"); this repository uses no LZ4 API directly, only Kafka's codec.
      Spotless `8.11.0`'s only behaviour change is the `google-java-format` default `1.36.1`→`2.0.0` plus two new
      options, and this build configures `palantirJavaFormat()` and `ktfmt()`, so it does not reach the formatter. Error
      Prone `2.52.0` adds `ValueClassEquals` enabled by default beside disabled-by-default checks; every module compiles
      with it.
- [x] 2.4 Set the ten catalog coordinates and the `protobuf-plugin` pin in `gradle/libs.versions.toml`. **Done**: eleven
      `[versions]` lines changed, nothing else in the file.
- [x] 2.5 Run the web UI's npm update and describe the lockfile diff by what moved; confirm `package.json` is unchanged.
      **Done**: 41 packages moved, five added (`@boundaries/config` with `ajv`/`json-schema-traverse`,
      `@boundaries/policies`, and a nested `is-core-module`), none removed, and the lockfile's root block is identical
      to the base's; `package.json` is unchanged. Two transitive majors moved inside the graph the manifests admit:
      `entities` `8.1.0`→`9.0.0` (declared by `parse5` `8.1.1`) and `@vitest/istanbul-lib-coverage`/`-report`
      `1.0.2`→`2.1.0`, declared by `@vitest/coverage-v8` `5.1.0`, not by `vitest`.
- [x] 2.6 Record the excluded majors (`db-scheduler`, `@hookform/resolvers`, `jsdom`), the wrapper, and the
      `helm-plugin` pin another unit owns as Non-Goals rather than acting on them. **Done**: they are `design.md`'s
      Non-Goals, and the unit record states the same set.

## 3. Verification

- [x] 3.1 Run `./gradlew spotlessApply` after the `spotless-plugin` bump and account for every reflowed file. **Done**:
      the bump reflowed no file — the working diff after the run holds only `gradle/libs.versions.toml` and
      `showcase-web-ui/package-lock.json`, matching the preflight's finding that Spotless `8.11.0` changes only the
      `google-java-format` default this build does not configure.
- [x] 3.2 Compile the protobuf-generated sources and the `e2eTest` classpath
      (`./gradlew :showcase-query-proto:compileJava compileE2eTestJava`) so the `protobuf-plugin` bump and the gateway
      e2e suite's netty and JGroups resolution are exercised; `check` includes no `e2eTest` classpath. **Done**: green
      in 2m 13s.
- [x] 3.3 Run the full `./gradlew check` (integration tests on Testcontainers, coverage gate enabled) and the web UI's
      `check`, `build` and `npmAudit`. **Done**: `check` green in 6m 6s (428 tasks; the four services' integration
      tests, every component suite, and `jacocoTestCoverageVerification` executed); the web UI's `check` ran 25 Vitest
      files green, `build` emitted `showcase-web-ui/build/dist/assets`, and `npmAudit` reports a clean scan. A first
      `check` attempt failed in `:helm:chart:fetchMainChartDependencies` on an unreachable Bitnami index
      (`socket is not connected` over IPv6); the retry reached the repository and passed, so the failure was the
      environment, not the bump.
- [x] 3.4 Re-run the three reports and confirm every taken row is gone. **Done**: the catalog section names only the
      deferred `db-scheduler` `17.0.0`, the wrapper line only `9.8.2 -> 9.9.0`, `buildLogicUpdates` only the
      `helm-plugin`/`helm-releases-plugin` `3.3.1` pair another unit owns, and the npm report only the two deferred
      majors.
- [x] 3.5 Sweep the documents for each bumped version string and fix every live mention. **Done**: the sweep retargeted
      the `9.5.5` mentions in `docs/adr/0012-prefer-typed-opensearch-java-client.md` and `docs/ideas.md` to `9.5.6`. The
      remaining hits are not stale claims: the dated `docs/decisions/2026-11-02-markdown-linting.md` record and the
      `jvm-development` pack's historical Error Prone probe line are records of the environment they ran on, and the
      `dependency-security` spec's `guava` `33.7.2-jre` is an "or newer" floor a taken version exceeds.
- [x] 3.6 Run `spotlessApply`, then `spotlessCheck`, `openspec validate --all`, and the 120-character check over the
      change's whole changed set, after the last edit to a gated file. **Done**: `spotlessApply` then `spotlessCheck`
      exit 0 after the last artifact edit; `openspec validate --all` reports 27 passed, 0 failed. The 120-character
      recipe finds no changed line of `gradle/libs.versions.toml` over the limit; its file-level hits are that catalog's
      33 pre-existing lines (the base holds the same set) and the two record files' rows, which are one line by
      contract.
- [x] 3.7 Refresh the branch onto any advanced `origin/main` and re-run the gates. **Done**: the base advanced
      repeatedly while the unit was in flight — seven times, from `2b4c9a00` to `6d630450` — each fast-forwarded with
      the work re-applied (stashed where the incoming commit touched a file this unit changes), with no conflict. The
      catalog carries the base's `helm-plugin`, `kafka-clients` and `paketo-*` pins beside this unit's eleven lines, and
      `buildLogicUpdates` reports no rows. The full `./gradlew check`, the web UI's `check`/`build`/`npmAudit` (25
      Vitest files, a clean audit), `compileE2eTestJava` and the three reports are green (`full-check-9.log`,
      `reports-final-5.log`); of the advances after those runs, `7900ec42` edits `AGENTS.md` with this unit's re-applied
      clause intact and the other two touch no path this unit changes, while the pull request's CI verifies the merged
      base.

## 4. Review gate

- [x] 4.1 Run the `review-quick` loop over the proposal and the implementation, repeating until it reports no new
      observations, with each round recorded in the unit's review-round record and its session row in the agent-session
      register, and both ledger checks run after each round's outcome. **Done (proposal stage)**: rounds 1 to 9,
      recorded in `docs/units/bump-dependencies-2026-11-09/rounds.tsv` with the register rows in `sessions.tsv` and the
      instances in `findings/round-<N>.md`; round 9 reported none and `--expect-clean` exits 0. The implementation stage
      runs the loop again over the diff.
- [x] 4.2 Derive the deep-review routing with `scripts/review-routing.py`, record its outcome in the unit's deep-review
      record, and run the routed pass when it is `REQUIRED` (or a `RECOMMENDED` outcome the owner resolves to run).
      **Done**: `RECOMMENDED` on the `repeated-class` signal, recorded with its signals in
      `docs/units/bump-dependencies-2026-11-09/deep-review.md`; the owner answered **skip** on 2026-11-09, so no
      `review-thorough` session was created.
- [x] 4.3 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net delta
      here. **Done**: two merges applied — the `jvm-testing` pack's Integration bullet now states what a bump's
      integration run reaches (resolution and boot versus code path, naming the configuration a behavioral claim depends
      on), and `AGENTS.md`'s version-bump clause now says a not-taken row carries no release number while a taken row
      states current → wanted. Applied net delta, measured with `git diff --numstat` after `spotlessApply`:
      `.opencode/skills/jvm-testing/SKILL.md` +5/−1, `AGENTS.md` +4/−2, no retirements; `verifyCapturedMarkers` passes.
      Its third proposal — a `/consult` step that completes the hand-over section with the returned report — is parked
      in `docs/ideas.md`'s 2026-11-09 section, because applying it edits a command definition whose behaviour the
      `agent-skills` spec describes and this unit carries `skip_specs`.
- [x] 4.4 Ask the owner for the manual review pass, after 4.1 is clean and 4.3 is done. **Requested** at the end of the
      implementation turn, with the tree staged; nothing is committed, pushed, archived, or merged before the answer.

## 5. Process defects

- [x] 5.1 Write `docs/units/bump-dependencies-2026-11-09/process-defects.md`, recording what the unit observed or
      stating that it observed none. **Done**: two records — a self-record figure its own run contradicted, costing five
      review rounds; and a consultation's report recorded only after a round asked for it.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The Helm (#40), buildpack (#211), and tooling (#308) findings remain separate units, and the wrapper bump is
  `/gradle-update`'s.
