## Context

See `proposal.md` for the motivation. Today the version catalog pins the Helm CLI (`helm = "4.2.4"`, a plain
coordinate — not a Gradle dependency, so `dependencyUpdates` ignores it) and concrete chart versions (after
`pin-helm-chart-versions`). The Helm plugin (`helm-conventions.gradle.kts`) already registers the bitnami, grafana,
and prometheus-community repositories and downloads a managed client. `verifyInfraImageVersions` proves the helm
client + repo plumbing works from a build task. The dependency-updates workflow (`dependency-updates.yml`) is the
existing template for "observational weekly check → GitHub issue".

## Goals / Non-Goals

**Goals:**
- Surface "your Helm CLI / chart pin is stale" with a concrete newer version, on a schedule and on demand.
- Reuse the existing issue machinery (open/update issue, mention owner, one bot comment) from dependency-updates.

**Non-Goals:**
- Not part of the `build` merge gate or a required check.
- Not auto-bumping anything — the report is advisory.
- Not re-implementing chart-repo logic the Helm plugin already provides.

## Decisions

**Decision: a `helmUpdates` Gradle task producing a report file, consumed by a new workflow.**
The task reads the pinned coordinates from the catalog, queries the latest Helm CLI release and each chart's latest
version via the plugin-managed client, and writes an actionable report (e.g. `build/helm-updates/report.txt`) in the
same shape the dependency-updates workflow parses. Alternatives considered:
- *Extend `dependencyUpdates`* — that task is Gradle-coordinate-scoped; Helm CLI and charts are not Gradle
  dependencies, and mixing them into one report/issue muddies the existing "Dependency updates" issue.
- *Pure shell in the workflow* — loses the catalog access and version-comparison logic that belongs in the build.

**Decision: query the Helm CLI's latest version from its GitHub release feed.**
The Helm CLI is distributed via `helm/helm` GitHub releases; the check compares `helm = "4.2.4"` to the latest
release tag. Rationale: the plugin's `downloadClient` only knows the *pinned* version, not newer ones. Alternatives
considered: a hard-coded endpoint in the task vs a small script — the task keeps it in one place with the catalog.

**Decision: query chart versions via the plugin-managed client against the already-registered repos.**
`helm search repo <chart> --versions` (or `helm show chart`) against bitnami/grafana/prometheus-community, using the
same XDG-dir plumbing the verify task uses. Rationale: the repositories are already registered in
`helm-conventions.gradle.kts`; no new repo wiring. The check is deliberately NOT build-cacheable — it is a live
network query, unlike the deterministic verify task.

**Decision: mirror `dependency-updates.yml` for the workflow and issue.**
A `helm-updates.yml` with a weekly schedule + `workflow_dispatch`, running the task and opening/updating a "Helm
updates" issue with the same comment hygiene (mention owner, replace the previous bot comment). Rationale: proven
pattern, minimal new surface, and the spec (merge-governance) describes this workflow class already.

## Risks / Trade-offs

- [Helm CLI latest-version query depends on GitHub API availability/rate limits] → The check treats a failed query as
  "no data for this coordinate" rather than failing the workflow; the issue simply omits it.
- [Chart repo index staleness: `helm search` uses the cached repo index, so "latest" can lag] → Acceptable for an
  advisory report; a `repo update` before search (as the verify task does) keeps it current.
- [Two issue-reporting workflows (dependency-updates + helm-updates) could drift in behavior] → Both are specified in
  merge-governance; the shared pattern keeps them consistent, and a future consolidation is a separate change.
- [The report could be noisy right after a new pin is chosen] → The issue states "no Helm updates available" when
  current, and only notifies on actionable items — mirroring dependency-updates.

## Migration Plan

1. Implement `helmUpdates` in build-logic (task + report writer), wired to the catalog coordinates.
2. Add `helm-updates.yml` reusing the dependency-updates issue logic, and register the task in the workflow.
3. Run the task locally against the real repos; verify the report lists the known-stale pins (e.g. postgres chart
   `16.7.27` → `18.x`, kps `77.14.0` → `88.x`).
4. Add the merge-governance spec requirement + AGENTS.md note; run `check -PskipITs -Pcoverage.gate.enabled=false`
   and `openspec validate --all`.
5. Rollback: remove the task, workflow, and spec requirement.

## Open Questions

None.