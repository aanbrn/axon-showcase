# Tasks

## 1. The fix

- [x] 1.1 Raise the app release's memory limits in `helm/values/axon-showcase/values-ci.yaml` (the services' JVMs need
      headroom under the plateau's burst; the requests stay, so the runner's scheduling budget is untouched). Verify the
      rendered values with `helm template` and that the scheduled requests are unchanged.
- [x] 1.2 Add a step to `.github/workflows/deployment-smoke.yml` that runs only when the load step fails: the pods in
      `axon-showcase` with their restart counts, and the tails of the gateway, query, and projection logs, while the
      cluster still exists. Verify `./gradlew workflowLint` passes.

- [x] 1.3 Note the diagnostics step in `AGENTS.md`'s deployment-smoke paragraph, so the docs and the workflow agree.

## 2. Verification

- [x] 2.1 `./gradlew spotlessApply`, `spotlessCheck`, `openspec validate --changes`, `workflowLint`, and
      `check -PskipITs -Pcoverage.gate.enabled=false` pass.
