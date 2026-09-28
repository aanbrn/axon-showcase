# Tasks

## 1. The ingress in the CI cluster

- [x] 1.1 Add the ingress controller's chart to the version catalog, register its Helm repository in
      `build-logic/src/main/kotlin/helm-conventions.gradle.kts`, and add a release carrying its own tag to
      `build.gradle.kts`; select it in the `ci` target and exclude its tag from the `local` target's `selectTags`.
      Verify `./gradlew helmInstallToCi --dry-run` includes it and `./gradlew helmInstallToLocal --dry-run` does not.
- [x] 1.2 Add the kind cluster configuration — the host's 80 and 443 mapped into the node, the node labelled
      `ingress-ready=true` — and point the workflow's cluster creation at it.
- [x] 1.3 Add the controller's `helm/values/<release>/values-ci.yaml` (the host-port binding and the node selector), and
      enable the gateway's ingress with the deployment's hostname in the app release's `values-ci.yaml`.
- [x] 1.4 In the workflow, write the hosts entry for that hostname, point the smoke profile at it, and delete the
      port-forward step. Verify `./gradlew workflowLint` passes.

## 2. Spec

- [x] 2.1 Write the `merge-governance` delta: a `MODIFIED` smoke requirement that the profiles run through the ingress,
      carrying every existing scenario in the main spec's order plus one for the ingress path.

## 3. Docs

- [x] 3.1 Update `AGENTS.md` (the smoke paragraph) and `README.md` for the ingress path; verify each statement against
      the build and the workflow.

## 4. Verification

- [x] 4.1 `./gradlew spotlessApply`, `spotlessCheck`, `openspec validate --changes`, `workflowLint`, and
      `check -PskipITs -Pcoverage.gate.enabled=false` pass.
- [x] 4.2 Reconcile the rebased workflow with `main`: keep the smoke profile only (no `baseline` invocation), the
      failure diagnostics step, and the kind `config:`; confirm the workflow contains `profile=smoke`, no
      `profile=baseline`, the hosts entry, and `baseUrl=http://axon-showcase-api`, and no `port-forward`.
