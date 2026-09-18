## 1. The check itself

- [x] 1.1 Add a `ToolingUpdatesTask` (build-logic) whose declared checks carry a display name, the version source and
      endpoint, and the workflow file plus input key the pin lives in. Its action reads each pin, queries its source
      over `HttpClient` — the npm `dist-tags.latest` entry, or a GitHub release's `tag_name` — normalizes a leading `v`
      off both the pin it read and the tag it received (the Snyk pin carries one, the `pack` pin does not), and writes
      `<name>: <pinned> -> <latest>` for every lagging tool to `build/tooling-updates/report.txt`, or the single line
      `No tooling updates available.` when nothing lags — the sentinel the workflow filters on. A pin that cannot be
      read exactly once fails the task naming the file and key.
- [x] 1.2 Give the task its own version comparison (numeric segments, longer-is-newer) with unit tests beside the
      existing `build-logic` tests covering the equal, longer, out-of-order and `v`-prefixed cases. The two existing
      comparators are left as they are — they disagree (`1.2.0` versus `1.2` is newer for the buildpack check, not the
      Helm one), so neither is shareable. Verify with `./gradlew :build-logic:test`. **Outcome**: four tests added
      (`ToolingUpdatesTaskTests`), passing alongside the existing eight; `:build-logic:test` green.
- [x] 1.3 Register `toolingUpdates` in the root `build.gradle.kts` beside `helmUpdates`/`buildpackUpdates`, mirroring
      their group, description, `reportFile` and `outputs.upToDateWhen { false }`, and declaring the three workflow
      files it reads as inputs so a pin edit re-runs the check. The checks: the OpenSpec CLI (`ci.yml`,
      `@fission-ai/openspec@`), the Snyk CLI (`snyk.yml`, `snyk-version:`) and the `pack` CLI (`e2e.yml`,
      `pack-version:`).
- [x] 1.4 Verify the check in both directions, since it has no clean baseline to lean on: the Snyk pin is already stale
      (`v1.1307.2` against the live `v1.1307.3`), so the first run must report that update; then temporarily set one pin
      _ahead_ of its live tag, re-run, and confirm that tool is absent from the report — a lookup that reports
      everything is not a comparison. Restore both, and record in the change's report that the first workflow run opens
      an actionable issue for the Snyk pin. **Outcome**: `./gradlew toolingUpdates` reports
      `snyk-cli: v1.1307.2 -> 1.1307.3` (the stale pin, with the two current pins correctly absent), and with the Snyk
      pin set to its live version and the `pack` pin set _ahead_ of its live tag the report is exactly
      `No tooling updates available.` — the sentinel the workflow filters on. Both workflow files were restored (clean
      `git diff`). **Correction after review**: the first run's GitHub parse was shape-dependent — the API returns
      pretty-printed JSON (`"tag_name": "vX"`), so the original no-space pattern only matched when a response happened
      to come back compact, making both GitHub-backed checks intermittently vacuous. The patterns now allow whitespace
      around the colon, verified against real API bodies for both repositories and the npm package; the anonymous GitHub
      rate limit (exhausted by this verification) prevents an immediate end-to-end rerun, so the next workflow run is
      the first unthrottled execution.
- [x] 1.5 Fix the Helm CLI release-tag parse in `build-logic/src/main/kotlin/HelmUpdatesTask.kt`, which had never
      matched the API's pretty-printed JSON, by reading it through the shared pattern the new task uses, and add the
      unit test that guards both JSON shapes; verify with `./gradlew :build-logic:test`. **Outcome**: the lookup now
      compares the pinned Helm CLI (`helm = "4.3.0"`, currently equal to the live `v4.3.0`, which is why the
      never-matching parse stayed hidden); six tests green, including the guard the no-space form fails. Both GitHub
      lookups now also authenticate when CI provides `GH_TOKEN` (the token is attached only to GitHub requests), and the
      two update workflows pass `github.token` to their report step.

## 2. The workflow

- [x] 2.1 Add `.github/workflows/tooling-updates.yml` mirroring the existing update workflows: a weekly `schedule` plus
      `workflow_dispatch`, `contents: read` and `issues: write`, checkout plus JDK 21 plus
      `gradle/actions/setup-gradle`, a step running `./gradlew toolingUpdates`, and the same issue open/update block —
      titled "Tooling updates", filtering the report on `No tooling updates available.` and using that line as the
      fallback body, and mentioning the repository owner only when the report is actionable, with the jq filter line it
      copies rewrapped in `buildpack-updates.yml` too so the two files stay identical apart from the intended
      differences; verify with `./gradlew workflowLint`.
- [x] 2.2 Verify it against the existing three rather than by conviction: diff it with
      `.github/workflows/buildpack-updates.yml` and confirm the triggers, permissions and steps match, with only the
      task invocation, the report path, the report sentinel and the issue title differing; the first real run is the
      post-merge `workflow_dispatch`, which the change's report names rather than a task here. **Outcome**: the diff
      differs only in the workflow and job names, the cron (`0 4` against `0 3`), the task invocation and its step name,
      the issue step name, the report path, the issue title, the four sentinel occurrences (the `grep` and the fallback
      body) and the `GH_TOKEN` env on the run step that task 1.5 added. It also caught a missing parenthesis in the
      copied jq filter that `workflowLint` cannot see, since actionlint does not parse jq inside the quoted program.

## 3. Docs and spec

- [x] 3.1 `AGENTS.md`: the manual-pin gotcha is rewritten rather than patched — its claim that pinned workflow tool
      versions are outside every update check, and its enumeration of the three pins, both go false once this check
      exists, so they are replaced by a pointer to the new check's declared list, while the clauses that stay true are
      kept — the deliberate-pin caveat (the `java-version` and opencode `model` inputs) and the note that a Snyk or
      `pack` bump cannot be verified locally. Separately, the update-check count becomes four and the enumeration of the
      workflows gains the new one. **Outcome**: the gotcha's claim and enumeration are replaced by the `toolingUpdates`
      list pointer, the count is four, the workflow enumeration and its per-workflow list both name the new workflow,
      and the deliberate-pin and local-verification clauses are kept.
- [x] 3.2 `README.md`: add the new workflow's paragraph to the CI section, add `./gradlew toolingUpdates` to the
      update-task list, and fix the GitHub Actions bullet, which names dependency and helm updates but omits both the
      buildpack and tooling ones. **Outcome**: the CI section gained the workflow's paragraph, the task list gained
      `toolingUpdates`, and the Actions bullet now names buildpack and tooling as well.
- [x] 3.3 Remove the three implemented ideas from `docs/ideas.md` — the Snyk CLI check, the OpenSpec CLI check, and the
      unify note — so they ride this change's branch, and sweep the counts they leave behind: "the three update-check
      workflows" becomes four in the two places it is stated, and the `## 2026-09-11` heading the removals empty goes
      with them. **Outcome**: the three ideas are removed with the emptied `## 2026-09-11` heading, and both "three
      update-check workflows" counts read four.

## 4. Gates

- [x] 4.1 Run `./gradlew :build-logic:test`, `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (which includes
      `workflowLint`), and `openspec validate --all`; verify each is green before reporting the change done.
      **Outcome**: `:build-logic:test`, `check -PskipITs -Pcoverage.gate.enabled=false` (including `workflowLint`) and
      `openspec validate --all` (23) are all green.
