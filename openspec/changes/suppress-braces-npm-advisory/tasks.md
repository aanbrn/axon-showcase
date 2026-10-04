# Tasks

## 1. Suppression rules (build-logic)

- [x] 1.1 Add `build-logic/src/main/kotlin/NpmAuditRules.kt`: an object that parses the suppression file and the
      `npm audit --json` report, returns the unsuppressed high-severity findings and the applied suppressions, treats an
      unexpired listed advisory's transitive dependents (walked downward through each package's `via` names) as
      suppressed, and fails on a malformed entry (missing `id`/`reason`/`expires`), a listed `id` matching no advisory
      in the report (fail-closed on a stale or mistyped entry), or an unparseable report — mirroring
      `DashboardJsonRules`' shape. Verify with `./gradlew :build-logic:compileKotlin`. _Done: compiles clean
      (`--rerun-tasks`); uses Jackson's non-deprecated `properties()`._
- [x] 1.2 Add `build-logic/src/test/kotlin/NpmAuditRulesTests.kt` with `@DisplayName`s covering: a suppressed advisory's
      closure is removed (the `braces`/`micromatch`/`@boundaries/elements`/`eslint-plugin-boundaries` chain); an
      unsuppressed high-severity finding is returned; an expired suppression is treated as absent; a malformed entry
      fails naming it; a listed `id` matching no advisory fails naming it; an unparseable report fails. Verify
      `./gradlew :build-logic:test` passes. _Done: 7 tests, 0 skipped, 0 failures._

## 2. Task wiring

- [x] 2.1 Rework the `npmAudit` task in `build-logic/src/main/kotlin/frontend-conventions.gradle.kts` to run
      `npm audit --json`, feed the body to `NpmAuditRules`, fail when unsuppressed high-severity findings remain, and
      log each applied suppression with its expiry (ignoring npm's exit code). Verify
      `./gradlew :showcase-web-ui:npmAudit` still fails on the current tree **before** the ignore file exists (the
      unsuppressed `braces` chain), proving the fail path. _Done: reworked to run `npm run audit:report`
      (`showcase-web-ui/scripts/audit-report.sh` captures the raw JSON) + a `doLast` calling `NpmAuditRules.evaluate`;
      proven to fail with an empty ignore file (all four chain packages listed as unsuppressed)._
- [x] 2.2 Add `showcase-web-ui/npm-audit-ignores.json` suppressing `GHSA-vfj7-8cjw-p6xm` with a reason naming the
      advisory, the dev-only lint-tool chain, the absence of a patched `braces`, and a short expiry (a few weeks out).
      Verify `./gradlew :showcase-web-ui:npmAudit` now succeeds and reports the suppression and its expiry. _Done:
      passes, logging `Suppressed GHSA-vfj7-8cjw-p6xm until 2026-11-16`._

## 3. Documentation

- [x] 3.1 Update ADR-0014 for the reworked mechanism, sweeping **both** the Decision and the Consequences: in the
      Decision, restate the `npmAudit` invocation as `npm audit --json` with severity filtering in `NpmAuditRules` (it
      no longer runs `npm audit --audit-level=high`), and reconcile the "`npm audit` without `--audit-level` … rejected"
      clause — its rejection was about low/moderate noise, which the in-task high-and-above filter still excludes, so
      say so rather than leaving it reading as unaddressed; in the Consequences, replace "npm has no `.snyk` analogue"
      with the landed mechanism (`showcase-web-ui/npm-audit-ignores.json` + `NpmAuditRules`) and its expiry discipline.
      Verify no ADR-0014 line still states the old invocation or reads as a pending follow-on. _Done: Decision bullet
      now names `npm audit --json` + `NpmAuditRules`; the `--audit-level` alternative reconciled; Consequences carry the
      landed mechanism and the `braces` upstream reference._
- [x] 3.2 Record the upstream reference in `docs/adr/0014-web-ui-npm-dependency-checks.md` (a file `upstreamReferences`
      scans), naming the close-out condition. As specified this was "a comment on `micromatch/braces#70`", but #70 is
      **locked** (comments disabled — a security-advisory thread) so the comment could not be posted; the actionable
      upstream fact is instead the **open fix PR `micromatch/braces#72`** ("prevent stack overflow from deeply nested
      patterns (CVE-2026-93687)"), so the reference points there and the close-out is "when #72 merges and a `braces`
      release carries it". _Done: ADR-0014 Consequences bullet records `#72`; the drafted comment was reviewed
      (`review-quick`) but not posted (no venue)._ For the manual-review note: the task's specified "comment on #70" is
      unachievable as written — #70 is locked; this is a deliberate, surfaced deviation, not a silent skip.

## 4. Verification

- [x] 4.1 Run `./gradlew spotlessApply`, `./gradlew spotlessCheck`, `./gradlew :build-logic:check`, and
      `openspec validate --all`; confirm all pass. _Done: `:build-logic:check` green; `openspec validate --all` 26/26;
      `spotlessCheck` re-run green after the last edits to `tasks.md` (the earlier ticks were written after a formatter
      run, as the quick review caught — re-run at the end)._
- [x] 4.2 Prove the check fails on a known-bad input, in two parts: (a) temporarily remove the suppression entry and
      confirm `:showcase-web-ui:npmAudit` fails on the `braces` advisory, then restore it; (b) temporarily point an
      entry at a non-matching id and confirm it fails naming that entry (the fail-closed path), then restore it. Also
      confirm a synthetic unsuppressed fixable high-severity finding fails through `NpmAuditRules`. _Done: (a) with an
      empty ignore file the task failed, listing `@boundaries/elements`, `braces`, `eslint-plugin-boundaries`,
      `micromatch`; (b) with a non-matching id it failed naming the entry; both restored; `NpmAuditRulesTests` covers
      the rules-level unsuppressed case._
- [ ] 4.3 Dispatch `gh workflow run dependency-security.yml` against the change branch and confirm the `web-ui-audit`
      job passes with the suppression in place.

## 5. Capture and delivery

- [x] 5.1 Run the `lesson-capture` subagent over this change's diff and review findings; apply its durable `AGENTS.md`
      proposals (or record its "nothing durable" verdict) and the applied net `AGENTS.md` delta. _Done: 2 proposals
      applied — a new Gotchas bullet (a verdict task must not be cacheable on a re-usable output; the `npmAudit`
      stale-expiry bug) and a folded clause in the upstream-reference bullet (verify a posting venue is writable; a
      locked advisory thread). Applied net `AGENTS.md` delta: **+12 lines** (15 insertions, 3 deletions); markers valid,
      Spotless green._
- [ ] 5.2 Push the branch, open the PR with the change dir, confirm `build` is green, then archive the change
      (`openspec archive suppress-braces-npm-advisory`) as an additional commit in the same PR — staging **both**
      `openspec/changes` and `openspec/specs` — and merge once the owner approves.
