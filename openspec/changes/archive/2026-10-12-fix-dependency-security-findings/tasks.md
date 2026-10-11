# Tasks

## 1. The JVM driver

- [x] 1.1 In `gradle/libs.versions.toml`, bump `postgresql` to `42.7.14`. Verify it resolves
      (`./gradlew :showcase-command-service:dependencies --configuration runtimeClasspath` shows `42.7.14`) and that
      `42.7.14` exists on Maven Central.

## 2. The web UI

- [x] 2.1 In `showcase-web-ui/package.json`, add an `overrides` entry forcing `handlebars` to `4.7.10`, and refresh the
      lock so `source-map-js` reaches `1.2.2` (a transitive of `postcss`/`css-tree`; its `^1.2.1` range resolves it, so
      no override is needed); inspect the lockfile diff by what moved and confirm both resolve.
- [x] 2.2 Run `./gradlew :showcase-web-ui:npmAudit` and confirm the remaining findings are only the suppressed ones; for
      any finding with no fix, add a suppression entry (advisory id + rationale + expiry) and confirm the task passes.
- [x] 2.3 Park the `handlebars` override's re-check in `docs/ideas.md` (a dated entry naming the `overrides` entry and
      its retrigger), since the override carries no expiry. Verify by reading the entry back.

## 3. Verification

- [x] 3.1 Run `./gradlew :showcase-web-ui:check` and `./gradlew :showcase-web-ui:build` (the bumps touch the bundle path
      — `source-map-js` via `vite` → `postcss` — and `check` never runs `vite build`), plus
      `./gradlew spotlessApply spotlessCheck`; confirm the frontend and the touched files are clean.
- [x] 3.2 Run `./gradlew check` with the integration tests (Docker), covering the runtime-path driver bump.
- [x] 3.3 Dispatch the credentialed scan against this branch as part of the merge —
      `gh workflow run dependency-security.yml --ref fix-dependency-security-findings`. Ran (run 38184554382): the two
      `postgresql` findings are gone and `web-ui-audit` passes, but Snyk now fails on two advisories published since the
      2026-10-10 run (`snappy-java` 1.1.10.5, fixable, and `guava` 33.6.0-jre, no fix) — deferred to a follow-up unit
      rather than folded in.
- [x] 3.4 Run a `review-quick` pass over the proposal and implementation, repeating until clean.

## 4. Capture

- [x] 4.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: one new Gotchas bullet (a fixable npm advisory in an exactly-pinned
      transitive is forced with `overrides`, not npm's breaking suggested fix or a suppression, and its re-check is
      parked) — net +8 lines. No retirements were proposed.
- [x] 4.2 Ask the owner for the manual review pass, after 3.4 is clean and 4.1 is done.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied and the owner approves.
- Verify the archived result.
