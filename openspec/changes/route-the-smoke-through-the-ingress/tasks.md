# Tasks

## 1. The ingress in the CI cluster

- [ ] 1.1 Add the ingress controller's chart to the version catalog and a release carrying its own tag to
      `build.gradle.kts`; select it in the `ci` target and exclude its tag from the `local` target's `selectTags`.
      Verify `./gradlew helmInstallToCi --dry-run` includes it and `./gradlew helmInstallToLocal --dry-run` does not.
- [ ] 1.2 Add the kind cluster configuration — the host's 80 and 443 mapped into the node, the node labelled
      `ingress-ready=true` — and point the workflow's cluster creation at it.
- [ ] 1.3 Add the controller's `helm/values/<release>/values-ci.yaml` (the host-port binding and the node selector), and
      enable the gateway's ingress with the deployment's hostname in the app release's `values-ci.yaml`.
- [ ] 1.4 In the workflow, write the hosts entry for that hostname, point both load profiles at it, and delete the
      port-forward step. Verify `./gradlew workflowLint` passes.

## 2. Spec

- [ ] 2.1 Write the `merge-governance` delta: a `MODIFIED` smoke requirement that the profiles run through the ingress,
      carrying every existing scenario in the main spec's order plus one for the ingress path.

## 3. Docs

- [ ] 3.1 Update `AGENTS.md` (the smoke paragraph) and `README.md` for the ingress path; verify each statement against
      the build and the workflow.

## 4. Verification

- [ ] 4.1 `./gradlew spotlessApply`, `spotlessCheck`, `openspec validate --changes`, `workflowLint`, and
      `check -PskipITs -Pcoverage.gate.enabled=false` pass.
