## 1. Update check task

- [x] 1.1 Implement `helmUpdates` in build-logic: read the pinned Helm CLI (`helm`) and chart coordinates from the
      catalog, and for each coordinate resolve the latest available version — Helm CLI via its GitHub release feed,
      charts via the plugin-managed client against the registered repos — writing an actionable report; verify the
      task runs locally and lists the known-stale pins (e.g. postgres chart `16.7.27` → `18.x`,
      kps `77.14.0` → `88.x`)
- [x] 1.2 Verify the task is not build-cacheable (it is a live network query) and that a failed query for one
      coordinate is reported as "no data" rather than failing the run
- [x] 1.3 Add a major-disabled mechanism for chart names (`config/helm-updates/major-disabled.properties`) and verify
      a disabled chart reports only same-major updates: postgres/kafka/opensearch major jumps (e.g. postgres 16→18)
      are suppressed while an older same-major pin (16.7.20) still reports its 16.7.27 update

## 2. Workflow and issue

- [x] 2.1 Add `.github/workflows/helm-updates.yml` (weekly schedule + `workflow_dispatch`) that runs `helmUpdates`
      and opens/updates a "Helm updates" issue, reusing the dependency-updates comment hygiene (mention the owner,
      replace the previous bot comment); verify the workflow file is valid YAML and the `permissions` grant
      `issues: write`
- [x] 2.2 Update `AGENTS.md` under the CI section noting the new observational `helm-updates` workflow (not a merge
      gate), keeping lines within 120 characters

## 3. Spec and verification

- [x] 3.1 Sync the merge-governance spec with the new "Helm update report runs on a schedule and on demand"
      requirement and its scenarios, keeping lines within 120 characters
- [x] 3.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`, confirming the
      build, the new task's wiring, and spec validation pass