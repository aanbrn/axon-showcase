# Tasks

## 1. Build wiring

- [x] 1.1 Add `showcase-web-ui/scripts/outdated-report.sh` (executable) and an `outdated:report` npm script in
      `showcase-web-ui/package.json` that runs it; the script first creates the module's `build/` directory
      (`mkdir -p build`) and then redirects `npm outdated`'s stdout to `build/npm-outdated.txt`, stderr to
      `build/npm-outdated.err.txt` (diagnostics only), and `$?` to `build/npm-outdated.exit` (so a non-zero npm exit
      does not fail the script). Register `npmOutdated` (group `help`, `dependsOn(npmCi)`, runs that script, and in a
      `doLast` logs the report file and, when the exit file is non-zero, prints `npm-outdated.err.txt` so a local npm
      failure is not a silent empty run) and `npmAudit` (group `verification`, `dependsOn(npmCi)`, runs
      `npm audit --audit-level=high`, failing on findings) in
      `build-logic/src/main/kotlin/frontend-conventions.gradle.kts`, neither wired into `check`. Run
      `./gradlew :showcase-web-ui:npmFormat` after editing `package.json` (it is Prettier-gated, not Spotless-gated).
      Verify `./gradlew :showcase-web-ui:tasks --all` lists both, `./gradlew :showcase-web-ui:npmOutdated` prints the
      report and writes the three files on a fresh checkout (`./gradlew :showcase-web-ui:clean` first — the task
      declares no `build/` outputs), and `./gradlew :showcase-web-ui:check --dry-run` does not include either. — Done:
      tasks list, `npmOutdated` printed 20 rows and wrote the three files (exit `1`, empty stderr), `check --dry-run`
      excludes both.
- [x] 1.2 Prove the checks on known inputs by running the actual tasks and script (not bare npm), and read every scratch
      output used as evidence: (a) with a deliberately older version of one dependency in `showcase-web-ui/package.json`
      (keep `package-lock.json` in sync — both tasks `dependsOn(npmCi)`, and `npm ci` refuses an out-of-sync lockfile),
      `./gradlew :showcase-web-ui:npmOutdated` lists that package with its current/wanted/latest versions and
      `npm-outdated.exit` is non-zero; (b) with a dependency pinned to a version carrying a known high-severity npm
      advisory, `./gradlew :showcase-web-ui:npmAudit` fails and names it; (c) after reverting, the report no longer
      lists that package and `npmAudit` passes; (d) an isolated fixture (a temp dir whose dependency is current) run
      with the same redirection yields an empty report and exit `0` — the "no updates" input; (e) the script run
      directly in `showcase-web-ui` with a poisoned registry
      (`npm_config_registry=http://127.0.0.1:1 npm run outdated:report`) leaves `npm-outdated.txt` empty and
      `npm-outdated.exit` non-zero — the error input. Revert all scratch edits and confirm
      `git diff showcase-web-ui/package-lock.json` is empty and `package.json` shows only the `outdated:report` script
      addition. — Done: `minimist@1.2.0` was listed (`1.2.0`/`1.2.0`/`1.2.8`, exit `1`), `npmAudit` failed naming its
      critical advisory, the revert removed it and `npmAudit` passed, the up-to-date fixture gave an empty report + exit
      `0`, and the poisoned-registry script run gave an empty report + exit `1` with `ECONNREFUSED` on stderr;
      `package-lock.json` is unchanged.

## 2. Workflows

- [x] 2.1 `git mv .github/workflows/snyk.yml .github/workflows/dependency-security.yml`, set
      `name: Dependency Security`, keep the `snyk` job, and add a `web-ui-audit` job (Node/npm caches,
      `./gradlew :showcase-web-ui:npmAudit`, no token). Verify `./gradlew workflowLint` passes and the file references
      the `npmAudit` task. — Done: `workflowLint` passes; the file is `name: Dependency Security` with the `snyk` and
      `web-ui-audit` jobs, the latter running `:showcase-web-ui:npmAudit`.
- [x] 2.2 Repoint the `toolingUpdates` check in `build.gradle.kts` — its `workflowFile = "snyk.yml"` and the
      corresponding `pinFiles` entry become `dependency-security.yml` (after 2.1 creates the file). Verify
      `./gradlew toolingUpdates` still reports the `snyk-cli` pin and does not error on a missing file. — Done:
      `toolingUpdates` reports `snyk-cli: v1.1307.3 -> 1.1307.4`; both references repointed.
- [x] 2.3 Extend `.github/workflows/dependency-updates.yml`: add the `nodejs` include to `gradle-home-cache-includes`
      and the `~/.npm` cache (keyed on `showcase-web-ui/package-lock.json`), run
      `./gradlew :showcase-web-ui:npmOutdated` (which writes `showcase-web-ui/build/npm-outdated.txt`, `...err.txt`, and
      `...exit`), and fold the report into the issue body under a web UI heading. Wire it into the
      actionable/notification logic as one rule: a non-empty `npm-outdated.txt` is actionable (the owner is notified);
      otherwise a non-zero `npm-outdated.exit` fails the step, printing `npm-outdated.err.txt` (npm errored with no
      report); otherwise it is "no web UI updates", silent when there are also no catalog updates. Verify by diffing the
      cache setup against `ci.yml` and re-reading the `awk` extraction so the catalog, Gradle wrapper, and web UI
      sections are each selected. — Done: cache includes match `ci.yml`, the npm step runs, and the branch logic reads
      the report + exit files and prints the error file on failure.

## 3. Docs

- [x] 3.1 `README.md`: derive the sites with
      `grep -nE "dependencyUpdates|dependencySecurityCheck|dependency-updates|dependency-security|snyk\.yml" README.md`
      (confirm it hits known positives such as line 662 before trusting it) and update every hit that describes a check,
      workflow, or slash command — the "Dependency Updates and Security" command block (add
      `:showcase-web-ui:npmOutdated` and `:npmAudit`), the two CI workflow descriptions, the `/dependency-updates` and
      `/dependency-security-check` table rows and the `/dependency-updates` prose, the dependency-scan prose, and the
      "In the Process" Snyk bullet — leaving prerequisite and config-path mentions. Verify no stale description remains
      and `grep -rn "snyk.yml" README.md` returns nothing. — Done: all derived sites updated; `snyk.yml` absent.
- [x] 3.2 `AGENTS.md`: derive the sites with
      `grep -nE "dependencyUpdates|dependencySecurityCheck|dependency-updates|dependency-security|snyk\.yml" AGENTS.md`
      (confirm it hits known positives such as line 564) and update every hit that describes a check or workflow — the
      Build & Test command block (add `:showcase-web-ui:npmOutdated` and `:npmAudit`), the CI section's
      dependency-security workflow description (both jobs), the update-check list, the gotcha enumerating what each
      update check covers, and every `snyk.yml` reference (including historical ones) — leaving prerequisite and
      config-path mentions. Verify the `snyk` job and the Snyk tool remain described and `grep -rn "snyk.yml" AGENTS.md`
      returns nothing. — Done: Build & Test block, workflow description, update-check list, gotcha, and the historical
      reference updated; `snyk.yml` absent and the Snyk tool/job still described.
- [x] 3.3 `SECURITY.md`: update the `snyk.yml` reference and extend its dependency-scan wording to name the web UI npm
      audit. `docs/adr/0006-snyk-scan-as-exec-task-not-gradle-plugin.md`: update the `snyk.yml` references and note that
      the workflow now also runs the web UI npm audit. — Done: SECURITY.md names both scans; ADR-0006 points at the
      renamed workflow and the npm audit (via ADR-0014).
- [x] 3.4 Remove the "Dependency updates for the web UI" idea from `docs/ideas.md`. Verify
      `grep -rn "Dependency updates for the web UI" docs/ideas.md` returns nothing. — Done: removed; the `2026-09-04`
      section keeps its other entry.
- [x] 3.5 Record the capability `## Purpose` refreshes owed at archive (a delta cannot carry a Purpose):
      `showcase/quality/dependency-management` (its Purpose scopes the capability to the `dependencyUpdates` report) and
      `showcase/quality/dependency-security` (scopes it to platform-constrained transitives). Apply both in the archive
      commit and name the deferral in the report. — Done: both Purposes refreshed in the archive commit.
- [x] 3.6 Add `docs/adr/0014-web-ui-npm-dependency-checks.md` (Nygard format, `Status: Accepted`) recording the choices
      this change makes — npm `outdated`/`audit` for the web UI rather than Snyk-on-npm, the checks surfaced through the
      existing workflows, and the absent npm suppression mechanism — with the rejected alternatives; index it in
      `docs/adr/README.md` if that file lists the ADRs. — Done: ADR added; `docs/adr/README.md` lists no ADRs, so no
      index row is owed.
- [x] 3.7 `.opencode/commands/dependency-security-check.md` and `.opencode/commands/dependency-updates.md`: extend the
      local security command to also run `./gradlew :showcase-web-ui:npmAudit` and the update command to also run
      `./gradlew :showcase-web-ui:npmOutdated` (update each frontmatter description), so both local entry points cover
      both ecosystems. — Done: both commands run both ecosystems and their descriptions name them.

## 4. Verification

- [x] 4.1 Run `openspec validate --changes` — the three delta specs validate — then `./gradlew spotlessApply` followed
      by `./gradlew spotlessCheck`, and `./gradlew :showcase-web-ui:npmFormat` followed by
      `:showcase-web-ui:npmFormatCheck` (the edited `package.json` is Prettier-gated, not Spotless-gated). All pass. —
      Done: all four pass.
- [x] 4.2 Sweep for stale references; each returns nothing (the change dir itself legitimately still names the old
      file): `grep -rn "snyk\.yml" README.md AGENTS.md SECURITY.md docs/adr build.gradle.kts .github`,
      `grep -rn "snyk\.yml" .opencode openspec/config.yaml`, and
      `grep -rn "Dependency updates for the web UI" docs/ideas.md`. — Done: all three return nothing (ADR-0014's
      historical mention was reworded).
- [ ] 4.3 Live check after merge (owner/user): `gh workflow run dependency-security.yml` and
      `gh workflow run dependency-updates.yml`, then confirm the reruns succeed and the "Dependency updates" issue
      carries the web UI section. If no dispatch is possible, park the follow-up in `docs/ideas.md` and name it in the
      change's report.
