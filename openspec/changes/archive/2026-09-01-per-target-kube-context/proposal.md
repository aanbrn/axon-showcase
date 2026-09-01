## Why

Each Helm release target deploys to a different cluster, and the kube context that identifies the local cluster is
per-developer (colima on macOS, kind/minikube on Linux) while any future remote target (staging) uses a shared, fixed
context. The current setup relies on an undocumented, machine-specific `helm.kubeContext` in
`~/.gradle/gradle.properties` and does not express which target deploys to which context. The gradle-helm-plugin
supports per-target kube contexts natively; we should use that to make deployment targets deterministic and their
context resolution documented.

## What Changes

- Declare per-target kube contexts in `build.gradle.kts` using the plugin's `releaseTargets` DSL.
- The `local` target's kube context resolves from a per-machine property (`helm.local.kubeContext`), falling back to the
  developer's current kube context when unset, so macOS (colima) and Linux (kind/minikube) developers deploy to their
  own local cluster without hard-coding a context name in the repo.
- Document the per-machine property convention and the template for future remote targets (each target declares its own
  shared, fixed kube context).
- Remove the undocumented `helm.kubeContext=colima` override from `~/.gradle/gradle.properties` in favor of the
  per-target mechanism.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/merge-governance`: add a requirement that each Helm release target declares its kube context in the
  build, with the `local` target resolving per-machine and remote targets using a shared fixed context.

## Impact

- `build.gradle.kts` — the `releaseTargets { create("local") { ... } }` block gains kube-context resolution.
- `gradle.properties` (project) — unchanged; the per-machine `helm.local.kubeContext` lives in user-level
  `~/.gradle/gradle.properties` or is passed via `-P`.
- `~/.gradle/gradle.properties` (machine) — `helm.kubeContext=colima` removed, replaced by
  `helm.local.kubeContext=colima`.
- `AGENTS.md` / `README.md` — document the per-target kube-context convention and the per-machine `local` resolution.
- No chart changes; the chart itself is unaffected.
