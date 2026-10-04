# Tasks

## 1. Suppression rules (build-logic)

- [ ] 1.1 Add `build-logic/src/main/kotlin/NpmAuditRules.kt`: an object that parses the suppression file and the
      `npm audit --json` report, returns the unsuppressed high-severity findings and the applied suppressions, treats an
      unexpired listed advisory's transitive dependents (walked downward through each package's `via` names) as
      suppressed, and fails on a malformed entry (missing `id`/`reason`/`expires`), a listed `id` matching no advisory
      in the report (fail-closed on a stale or mistyped entry), or an unparseable report — mirroring
      `DashboardJsonRules`' shape. Verify with `./gradlew :build-logic:compileKotlin`.
- [ ] 1.2 Add `build-logic/src/test/kotlin/NpmAuditRulesTests.kt` with `@DisplayName`s covering: a suppressed advisory's
      closure is removed (the `braces`/`micromatch`/`@boundaries/elements`/`eslint-plugin-boundaries` chain); an
      unsuppressed high-severity finding is returned; an expired suppression is treated as absent; a malformed entry
      fails naming it; a listed `id` matching no advisory fails naming it; an unparseable report fails. Verify
      `./gradlew :build-logic:test` passes.

## 2. Task wiring

- [ ] 2.1 Rework the `npmAudit` task in `build-logic/src/main/kotlin/frontend-conventions.gradle.kts` to run
      `npm audit --json`, feed the body to `NpmAuditRules`, fail when unsuppressed high-severity findings remain, and
      log each applied suppression with its expiry (ignoring npm's exit code). Verify
      `./gradlew :showcase-web-ui:npmAudit` still fails on the current tree **before** the ignore file exists (the
      unsuppressed `braces` chain), proving the fail path.
- [ ] 2.2 Add `showcase-web-ui/npm-audit-ignores.json` suppressing `GHSA-vfj7-8cjw-p6xm` with a reason naming the
      advisory, the dev-only lint-tool chain, the absence of a patched `braces`, and a short expiry (a few weeks out).
      Verify `./gradlew :showcase-web-ui:npmAudit` now succeeds and reports the suppression and its expiry.

## 3. Documentation

- [ ] 3.1 Update ADR-0014 for the reworked mechanism, sweeping **both** the Decision and the Consequences: in the
      Decision, restate the `npmAudit` invocation as `npm audit --json` with severity filtering in `NpmAuditRules` (it
      no longer runs `npm audit --audit-level=high`), and reconcile the "`npm audit` without `--audit-level` … rejected"
      clause — its rejection was about low/moderate noise, which the in-task high-and-above filter still excludes, so
      say so rather than leaving it reading as unaddressed; in the Consequences, replace "npm has no `.snyk` analogue"
      with the landed mechanism (`showcase-web-ui/npm-audit-ignores.json` + `NpmAuditRules`) and its expiry discipline.
      Verify no ADR-0014 line still states the old invocation or reads as a pending follow-on.
- [ ] 3.2 Report the upstream gap — `micromatch/braces` has no release fixing CVE-2026-93687 (advisory
      `GHSA-vfj7-8cjw-p6xm`, patched versions: none; tracked at `micromatch/braces#70`) — as a comment on that issue,
      and record the reference in `docs/adr/0014-web-ui-npm-dependency-checks.md` (a file `upstreamReferences` scans),
      naming the close-out condition: the suppression entry is removed once a patched `braces` release resolves. Run it
      past `review-quick` before posting.

## 4. Verification

- [ ] 4.1 Run `./gradlew spotlessApply`, `./gradlew spotlessCheck`, `./gradlew :build-logic:check`, and
      `openspec validate --all`; confirm all pass.
- [ ] 4.2 Prove the check fails on a known-bad input, in two parts: (a) temporarily remove the suppression entry and
      confirm `:showcase-web-ui:npmAudit` fails on the `braces` advisory, then restore it; (b) temporarily point an
      entry at a non-matching id and confirm it fails naming that entry (the fail-closed path), then restore it. Also
      confirm a synthetic unsuppressed fixable high-severity finding fails through `NpmAuditRules`.
- [ ] 4.3 Dispatch `gh workflow run dependency-security.yml` against the change branch and confirm the `web-ui-audit`
      job passes with the suppression in place.

## 5. Capture and delivery

- [ ] 5.1 Run the `lesson-capture` subagent over this change's diff and review findings; apply its durable `AGENTS.md`
      proposals (or record its "nothing durable" verdict) and the applied net `AGENTS.md` delta.
- [ ] 5.2 Push the branch, open the PR with the change dir, confirm `build` is green, then archive the change
      (`openspec archive suppress-braces-npm-advisory`) as an additional commit in the same PR — staging **both**
      `openspec/changes` and `openspec/specs` — and merge once the owner approves.
