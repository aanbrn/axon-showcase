# Design

## Context

See `proposal.md` — Why. Facts established against the repository:

- `.github/workflows/e2e.yml` is the pattern for an observational workflow: a nightly cron plus `workflow_dispatch`,
  `contents: read`, its own `timeout-minutes`, `actions/setup-java` with Temurin 21, `gradle/actions/setup-gradle` with
  the `nodejs` cache include, and the `buildpacks/github-actions/setup-pack` step.
- `build.gradle.kts`'s `helm { releases { … } }` declares six releases (kps, tempo, axon-showcase-db-events,
  axon-showcase-kafka, axon-showcase-os-views, axon-showcase) with `valuesDir("helm/values/$name")`, `wait = true` and
  `waitForJobs = true`; the app release additionally `installDependsOn` the five image builds and `mustInstallAfter` the
  other five, so one app install pulls the whole stack.
- `releaseTargets` declares `local`, whose kube context resolves from the `helm.local.kubeContext` Gradle property — the
  hook the workflow uses — and `helm/values/*/values-local.yaml` hold the ephemeral settings
  (`persistence.enabled: false`, the bitnami netpol client labels).
- The chart's images are not published anywhere, so a runner must build them; the web UI image is built by the `pack`
  CLI.
- The load tests were driven against a local cluster over a `kubectl port-forward` (`-PbaseUrl=http://127.0.0.1:<port>`)
  when the non-default-target branch was exercised, so that path is proven.
- A runner-hosted `kind` cluster is proven: `bump-kps-chart-91`'s verification ran `helmInstallKpsToLocal` against one
  and deleted it after.

## Goals / Non-Goals

**Goals:**

- Verify the chart's deployment path and a load path on a schedule, so a break surfaces without a human looking.
- Keep the smoke observational — separate from the merge gate, recording no performance numbers.
- Add no new local prerequisite: the workflow provisions its own cluster.

**Non-Goals:**

- No performance baseline: a shared runner's contended, ephemeral timings are their own, not a developer's host's — the
  baseline reference stays local.
- No image publishing and no registry.
- No change to the merge gate, and no kind/minikube requirement added to the local development docs.

## Decisions

- **D1 — A dedicated workflow, not a job in `e2e.yml`.** The smoke is a distinct signal — the chart plus a load run,
  rather than the e2e suites — with its own timeout, so it follows the same observational pattern in its own file
  (nightly cron + `workflow_dispatch`, `contents: read`, never a required check).
- **D2 — The cluster comes from `helm/kind-action`.** The action installs `kind` and creates the cluster in one step,
  and Dependabot tracks the `uses:` ref, so no manually pinned CLI version joins the `toolingUpdates` check's declared
  list (which exists to detect releases of the versions pinned in workflow files).
- **D3 — The install is the documented local path, unchanged.** `helmInstall<Release>ToLocal` for the six releases with
  `-Phelm.local.kubeContext=kind-<cluster>`, using `helm/values/*/values-local.yaml` as they are. Rejected: a leaner
  CI-only install (no observability, no web UI image) — it needs build-config changes to break the app release's
  `mustInstallAfter` and `installDependsOn` couplings, and a second install path can silently diverge from the one the
  docs tell a person to run.
- **D4 — The images are built and loaded before the install.** `bootBuildImage` for the four services,
  `:showcase-web-ui:dockerBuildImage` (which needs the `pack` step, as `e2e.yml` has), then `kind load docker-image` for
  each. `kind`'s nodes cannot see the host daemon's images, and installing first would leave pods unable to pull.
- **D5 — Readiness comes from the install.** The releases already set `wait = true` and `waitForJobs = true`, so the
  workflow adds no separate rollout wait.
- **D6 — The load runs through a port-forward, not the chart's ingress.** The chart's ingresses need an ingress
  controller (absent from a bare `kind` cluster) and a hosts entry, whereas `kubectl port-forward` to the gateway
  service reaches it directly. The profiles are `smoke` (its zero-failure assertion) and a short `baseline` plateau
  (`-Pduration=PT2M -Prate=20`, whose per-name assertions add latency and failure checks); the fresh store exercises the
  read path's cold-target seed.
- **D7 — An ADDED requirement in `merge-governance`.** The smoke is a new artifact whose schedule, trigger, method, and
  non-gating role no existing requirement covers; `showcase/quality/load-tests` and `deployment/helm-chart` take no
  delta, because the smoke invokes their specified behavior rather than changing it.
- **D8 — The first real run is a dispatch after it lands.** GitHub exposes `workflow_dispatch` only once the file is on
  the default branch, so `gh workflow run deployment-smoke.yml` is the change's own verification, read back as the
  smoke's report.

## Risks / Trade-offs

- **A full stack on a 4 vCPU / 16 GB runner is tight.** The six releases, the `kind` node, and the runner's Docker share
  that memory, and the disk holds the five images. → The install's wait turns a starved cluster into a visible failure,
  and the chart's requests are modest (500m CPU / 0.5Gi per service); the job is observational.
- **~40–50 minutes of runner time nightly, much of it image builds the e2e job also does.** → Accepted: the added signal
  is the chart plus the load path, which compose cannot give; D3 records why the leaner alternative was rejected.
- **A flaky runner can fail the smoke for reasons unrelated to the chart.** → The assertions are the availability gate
  (zero failures, with the plateau's latency thresholds asserted at a rate far below the stack's measured operating
  point — 20 units/s against a ~120-unit/s operating point, so the headroom is large), and a red observational run
  prompts a look rather than blocking anything.
- **The action ref floats with Dependabot's updates.** → As with every `uses:` ref in the repository.

## Verification

- The workflow YAML lints (`./gradlew workflowLint`, part of `check`).
- The first real run is `gh workflow run deployment-smoke.yml` after it lands; I read the run log back and confirm every
  step executed — cluster created, releases ready, the load profiles passed, cluster deleted.
- Locally, the same steps are runnable by hand against any cluster through the local install path, and the port-forward
  invocation was already exercised against the local cluster.
