## Why

Three tool pins live in workflow files rather than the version catalog — `@fission-ai/openspec` in `ci.yml` (an
`npm install` argument), and `snyk-version` in `snyk.yml` and `pack-version` in `e2e.yml` (both `with:` inputs) — and no
update check covers them: `dependencyUpdates` reads catalog coordinates, `helmUpdates` the Helm CLI and charts,
`buildpackUpdates` the Paketo builder and buildpacks, and Dependabot only action refs. They go stale silently, and the
1.13.1 OpenSpec release was caught only because a human noticed it. The repo's own parked note rules out a fourth and
fifth near-duplicate check, since the three existing update workflows already duplicate the same
Gradle-task-plus-issue-posting shape.

## What Changes

- `build-logic/src/main/kotlin/ToolingUpdatesTask.kt` (new): one parameterized task over a declared list of pinned
  tools, each naming its version source (the npm registry's `dist-tags.latest` entry, or a GitHub releases API). It
  reports every tool whose pin lags as `<name>: <pinned> -> <latest>` to `build/tooling-updates/report.txt`, and queries
  with the JDK's `HttpClient` and parses with regex, as the existing update tasks do, so it adds no dependency.
- `build.gradle.kts`: register `toolingUpdates` with three checks — the OpenSpec CLI (npm), the Snyk CLI and the `pack`
  CLI (GitHub releases). Each pinned version is read from the workflow that declares it, so the pin stays single-sourced
  where CI consumes it.
- `.github/workflows/tooling-updates.yml` (new): a weekly schedule plus `workflow_dispatch`, running the task and
  opening or updating a "Tooling updates" issue, mentioning the repository owner when something is actionable and
  updating silently when not — the same shape as the existing update workflows, and not a merge gate. The jq filter line
  it copies is rewrapped in all four update workflows — the new one, `buildpack-updates.yml`, the existing
  `helm-updates.yml`, and `dependency-updates.yml` — so the copies stay identical apart from the intended differences
  and none carries a 121-character line.
- `AGENTS.md`: the manual-pin audit list shrinks to what the mechanism cannot reach — with all three tool pins covered
  it becomes a pointer to the mechanism — and the update-check enumeration gains the new workflow.
- `README.md`: the CI section gains the new workflow's paragraph, and the GitHub Actions bullet names it.
- `build-logic/src/main/kotlin/HelmUpdatesTask.kt`: the Helm CLI lookup's release-tag parse never matched the API's
  pretty-printed JSON, so the pinned Helm CLI was silently never compared — it now reads the tag through the same
  whitespace-tolerant pattern the new task uses, guarded by a unit test asserting both JSON shapes. Both GitHub-backed
  lookups also authenticate when CI provides a token (`GH_TOKEN`), because an anonymous lookup is rate limited and a
  throttled response is indistinguishable from a current version.
- `docs/ideas.md`: the three implemented ideas (the Snyk CLI check, the OpenSpec CLI check, and the unify note) are
  removed.

The two existing checks (`helmUpdates`, `buildpackUpdates`) are deliberately not folded in: their version sources differ
(Docker Hub tags, chart repositories and the Helm CLI), and rewriting working checks is a separate risk from adding one.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/merge-governance`: a new requirement that the pinned-tool update report runs on a schedule and is
  manually triggerable, covering the pins that live in workflow files rather than the version catalog, surfacing them
  through a GitHub issue, and staying outside the merge gate.

## Impact

- **Build**: a new `build-logic` task and its registration in the root build, plus one pattern fix in the existing Helm
  update check; no new dependency (JDK `HttpClient` plus regex, as the existing checks) and no change to the merge-gate
  `build` check.
- **Workflows**: one new observational workflow with `contents: read` and `issues: write`, a rewrap of one jq line in
  each of the three existing update workflows, and a step-level `GH_TOKEN` on `helm-updates.yml`; `AGENTS.md`'s claim
  that bare `[versions]` entries are not covered by `helmUpdates` is corrected, since the Helm CLI and chart pins are
  exactly such entries.
- **Docs and specs**: `AGENTS.md`'s manual-pin audit list and update-check enumeration, `README.md`'s CI section, the
  removal of the three implemented `docs/ideas.md` entries, and a `merge-governance` requirement.
- **Behavior**: none in the running services — the change only reports on pinned tools.
