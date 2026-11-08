- [claim-unreproducible] `openspec/changes/adopt-plugin-retried-fetches/design.md:75` — the opt-in timeout decision
  claims VerifyInfraImageVersionsTask and HelmUpdatesTask do not fetch the large index, which is false: both add and
  update the Bitnami repository, and verifyInfraImageVersions is in check.
- [over-claim] `openspec/changes/adopt-plugin-retried-fetches/proposal.md:22` — the AbstractHelmRepositoriesTask bullet
  claims the update timeout removes the two-minute default, but helm repo add downloads the same index and keeps it.
- [contradiction] `docs/units/adopt-plugin-retried-fetches/unit.md:8` — it says both workarounds retire, while the
  proposal and design keep FetchChartDependenciesTask.
- [coherence] `openspec/changes/adopt-plugin-retried-fetches/tasks.md:10` — repositoryCacheTtl and repoUpdateTimeout are
  configured on the add tasks and helmAddRepositories, which do not expose them.
- [single-sourcing] `openspec/changes/adopt-plugin-retried-fetches/tasks.md:3` — the Workspace line copies the unit
  record's fields, which the workspace rule forbids.
- [impact-omission] `.opencode/skills/gradle-build/SKILL.md:160` — it names plugin 3.3.0 as the version still calling
  Project.getProperties(), and no task retargets it.
- [impact-omission] `openspec/changes/adopt-plugin-retried-fetches/proposal.md:22` — FetchChartDependenciesTask.kt is
  edited by task 2.1 but not named.
- [claim-missing] `openspec/changes/adopt-plugin-retried-fetches/design.md:61` — the ~27.5 MB figure has no claims-table
  row.
- [claim-unreproducible] `openspec/changes/adopt-plugin-retried-fetches/design.md:116` — the release install path's "not
  specified" needs a search over the install task names.
