# Tasks — bump the pinned Snyk CLI

## 1. The bump

- [x] 1.1 Re-resolve the latest version rather than trusting the report's figure, as the update-check gotcha requires:
      `npm view snyk version` reports `1.1307.3` and `gh api repos/snyk/cli/releases/latest` reports `v1.1307.3`, so the
      report's `v1.1307.2 -> 1.1307.3` held. Done — both resolved to `1.1307.3`.
- [x] 1.2 Move `snyk-version` in `.github/workflows/snyk.yml` from `v1.1307.2` to `v1.1307.3`. Done — one line
      (`snyk-version: v1.1307.3`), and `grep` confirms the pin occurs exactly once.

## 2. Verification

- [x] 2.1 `./gradlew toolingUpdates` after the bump reports no update for `snyk-cli`, which is the check agreeing with
      the new pin. Done — the report is the `No tooling updates available.` sentinel.
- [x] 2.2 `./gradlew workflowLint` green (actionlint lints the changed workflow) and `./gradlew spotlessCheck` green.
      Done — both pass.
- [x] 2.3 `openspec validate --all` with the change validating as `skip_specs`. Done — 23/23.
- [x] 2.4 The new `tooling-updates` workflow exercised end to end: dispatched on `main` while the pin was still stale,
      it ran green and opened the "Tooling updates" issue (#308) with `snyk-cli: v1.1307.2 -> 1.1307.3`. Done — run
      SUCCESS, issue #308 open. The bumped pin's own first execution is the next weekly run, since the pin reaches CI
      only once this merges.
