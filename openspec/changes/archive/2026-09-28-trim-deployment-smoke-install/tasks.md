# Tasks

## 1. The CI target

- [x] 1.1 Add a `ci` release target to `build.gradle.kts`: `selectTags` the `database`, `kafka`, and `application` tags
      (no `monitoring`), `kubeContext` `kind-axon-showcase-smoke` with a comment naming the workflow's `CLUSTER_NAME` as
      its other half, and the releases it installs carrying their own `values-ci.yaml` (task 1.2). Verify
      `./gradlew helmInstallToCi --dry-run` selects the application and infrastructure releases only.
- [x] 1.2 Add a `values-ci.yaml` per release the target installs, following the `values-<target>.yaml` convention: the
      app release's for its ServiceMonitors off (explicitly, though the charts' defaults already leave them off here),
      one command replica, and its smaller requests; each infrastructure release's for its identity keys and settings.
      Verify by summing what the target's releases request (the app plus postgres, kafka, and opensearch) against 4 CPU.
- [x] 1.3 Point the workflow's install step at the `ci` target's aggregate and keep the `--name` fix on the image load.
      Verify `./gradlew workflowLint` passes.

## 2. Spec

- [x] 2.1 Write the `merge-governance` delta: a `MODIFIED` requirement for the smoke's install scope — the application
      and infrastructure releases with values that fit the runner, not all six through the local path — carrying every
      existing scenario in the main spec's order.

## 3. Docs

- [x] 3.1 Update `AGENTS.md` (the smoke paragraph, and the Helm section's release-target notes) and `README.md` for the
      `ci` target and the trimmed install; verify each statement against the build and the workflow.

## 4. Verification

- [x] 4.1 `./gradlew spotlessApply`, `spotlessCheck`, `openspec validate --changes`, `workflowLint`, and
      `check -PskipITs -Pcoverage.gate.enabled=false` pass.
