# Tasks

## 1. The workflow

- [x] 1.1 Add `.github/workflows/deployment-smoke.yml`: a nightly cron plus `workflow_dispatch`, `contents: read`,
      `timeout-minutes: 60`, Temurin JDK 21, the Gradle and npm caches, and the `pack` CLI exactly as `e2e.yml` has
      them. Verify `./gradlew workflowLint` passes.
- [x] 1.2 Create the cluster with `helm/kind-action` under a fixed name, build the four service images and the web UI
      image, and `kind load docker-image` each, so the install's pods can pull them.
- [x] 1.3 Install the six releases through the documented local path —
      `./gradlew helmInstall<Release>ToLocal … -Phelm.local.kubeContext=kind-<cluster>` — relying on the releases' own
      `wait`/`waitForJobs` for readiness.
- [x] 1.4 Port-forward the gateway service and run the load profiles against it (`-Pprofile=smoke`, then
      `-Pprofile=baseline -Pduration=PT2M -Prate=20 -PbaseUrl=http://127.0.0.1:<port>`), failing the job when an
      assertion fails.
- [x] 1.5 Delete the cluster in a step that runs even when an earlier step fails.

## 2. Spec

- [x] 2.1 Write the `merge-governance` delta: the ADDED requirement for the smoke's schedule, trigger, method, and
      non-gating role, with its scenarios.

## 3. Docs

- [x] 3.1 Document the workflow in `AGENTS.md`'s Continuous Integration section — where it sits among the observational
      workflows, what it installs, and that it is never a gate — and name the smoke's invocation in the load-test block.
      Verify each statement against the workflow file.
- [x] 3.2 Surface the capability in `README.md` in experience terms (a nightly deployment smoke against a real cluster),
      preserving the README's shape.
- [x] 3.3 Remove the implemented idea from `docs/ideas.md`, and park the degradation gap the reshape surfaced (nothing
      compares one run to another; the reference is overwritten each run).

## 4. Verification

- [x] 4.1 `./gradlew spotlessApply` then `spotlessCheck` pass; `openspec validate --changes` passes;
      `check -PskipITs -Pcoverage.gate.enabled=false` passes, `workflowLint` included.
