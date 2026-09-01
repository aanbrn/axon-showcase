## 1. Task cacheability

- [x] 1.1 Declare the infra values files as `@InputFiles` on `VerifyInfraImageVersionsTask` (path-sensitive), and
      verify a changed values file invalidates the task (re-runs on change, skipped when unchanged)
- [x] 1.2 Add an `@OutputFile` result marker to `VerifyInfraImageVersionsTask` (e.g. under `build/verification/`),
      written by the action, and verify the task reports `UP-TO-DATE` on a second run with unchanged inputs and
      `FROM-CACHE` when the build cache is primed

## 2. Repo setup in the action

- [x] 2.1 Move the `helm repo add` + `helm repo update` steps into the task action (before the `helm show values`
      resolution), using the plugin-managed client, and remove `dependsOn("helmUpdateRepositories")` from the task
      registration in `build.gradle.kts`
- [x] 2.2 Verify the full flow on a clean state: the task resolves all three charts and reports them consistent; a
      re-run with unchanged inputs is `UP-TO-DATE`; changing a coordinate re-runs and (for a deliberate mismatch)
      fails the build

## 3. Docs and full verification

- [x] 3.1 Update `AGENTS.md` noting that `verifyInfraImageVersions` is build-cacheable on its inputs (pinned
      coordinates + values files), so unchanged pins skip the Helm resolution in CI, and verify added lines stay within
      the 120-character limit
- [x] 3.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`, confirming the
      cacheable gate, all module checks, and spec validation pass