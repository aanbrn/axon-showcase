# Design

## Context

See `proposal.md` — Why. `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json` is read by
`helm/chart/src/main/helm/templates/grafana-dashboards/configmaps.yaml` via `$.Files.Get` and emitted verbatim as
ConfigMap data, so Helm treats it as an opaque string. Confirmed: truncating the file's last byte and running
`:helm:chart:helmLintMainChartMinimal` still reports "0 chart(s) failed".

The repository's build checks are Gradle tasks (`verifyInfraImageVersions`, `verifyInstallCommands`,
`verifyModuleDependencies`, `workflowLint`, `verifyCapturedMarkers`, `verifyTrackedIgnoredFiles`,
`verifyConflictMarkers`, `verifyExecutableBits`, `verifyUniqueCronSchedules`), each registered in the root
`build.gradle.kts` and added to the root `check` task; rule logic lives in `build-logic`, and each `verify*` task
carries its class's test in `build-logic`'s test suite (part of `check`). The spec requirement owning the dashboard is
`showcase/deployment/helm-chart` ("Extra deployments and dashboards"), which specifies only the ConfigMap rendering — it
does not describe the dashboard JSON's content, so parsing it is a general build-check concern, not a chart-behavior
one.

## Goals / Non-Goals

**Goals:**

- Fail `check` when any file under `helm/chart/src/main/helm/files/grafana-dashboards/` is not valid JSON, naming the
  file and the parse error.
- Live where the other `verify*` checks live (root `check`, `build-logic` rule + test), so it needs no new dependency
  and no network.

**Non-Goals:**

- Validating the dashboard's Grafana schema, panel structure, or query correctness — beyond JSON-syntax validity, and
  beyond what a gate should own (the panel semantics need a live render, not a build check).
- Replacing `helm lint` — the JSON parse is additive; the chart lint gates are unchanged.
- Walking arbitrary JSON elsewhere in the repo — scoped to the bundled dashboards directory.

## Decisions

### D1: A `verifyDashboardJson` Gradle task in `check`, not a `helm lint` extension

Helm cannot be made to parse the file (it is template data), so the check is a standalone Gradle task, matching the
existing `verify*` pattern and joining the root `check` members. The rule (glob the directory, parse each file, collect
failures) lives in `build-logic`, so it is unit-testable alongside `InstallCommandRules`.

- **Alternative — a `helm template` + external `jq`/`python` assertion:** adds a tool dependency and re-implements the
  `verify*` convention; the Gradle task needs none.
- **Alternative — validate in the chart module's `check`:** the chart module's `check` is not the standard root `check`
  the convention wires all gates into, and the task is repository-scoped (it names a fixed path under the chart source).
- **Alternative — assert the JSON parses via a Gradle `ValidatePlugins`/`json` plugin:** no such core task covers a
  directory of files; a small custom task is the pattern.

### D2: Fail with the file path and the underlying message; scan all files before failing

The task parses **every** file in the directory and collects the failures, then fails once naming each bad file and its
error — so a second broken file is not hidden behind the first (the same "a check must not stop at the first finding"
discipline the other `verify*` tasks follow). It fails when the directory is missing or holds no dashboard, so a renamed
path cannot make the check pass vacuously.

- **Alternative — stop at the first failure:** hides subsequent files.
- **Alternative — a silent pass when the glob matches nothing:** the vacuous-glob failure mode the repository already
  warns about (a renamed directory would otherwise disable the gate without notice).

### D3: Cacheable task with an input file collection and an output result file

The task declares the dashboards as an `@InputFiles` `ConfigurableFileCollection` (the repository's directory-input
precedent — `VerifyInfraImageVersionsTask`, `PackBuildImageTask`; an absent directory yields an empty collection, which
D2 treats as a failure) and writes a result file as its `@OutputFile`, so an unchanged dashboard restores from the build
cache.

### D4: Spec the outcome in `code-quality`, not `helm-chart`

`showcase/deployment/helm-chart` owns the chart's rendered behavior; a malformed dashboard file changes no rendered
behavior a scenario there describes (the ConfigMap is still emitted). The new gate is a build-quality check, so its
requirement belongs in `showcase/quality/code-quality`, which owns the build's non-source gates and their `check`
membership.

## Risks / Trade-offs

- [A JSON schema regression passes a syntax-only check] → accepted and stated as a Non-Goal; syntax is what Helm cannot
  see, and the panel semantics are verified against a live render, not a build check.
- [The directory path drifts (a renamed dashboards dir) and the check passes vacuously] → D2 fails on a missing/empty
  directory, so the drift is caught, not silent.
- [The check adds build time] → it parses a single small JSON file locally; the task is cacheable and negligible.

## Migration Plan

Not applicable — a build-check addition with no data or API migration. Rollback is reverting the task registration.

## Open Questions

None.
