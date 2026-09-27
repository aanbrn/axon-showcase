# Design

## Context

See `proposal.md` — Why. Facts established against the repository and the first dispatch (run 36327262007):

- The dispatch failed at the image load with `ERROR: no nodes found for cluster "kind"`: `kind`'s CLI defaults to a
  cluster named `kind`, while `helm/kind-action` created `axon-showcase-smoke`. kind's docs state both halves ("By
  default, the cluster will be given the name `kind`"; "If using a named cluster you will need to specify the name of
  the cluster").
- The install's CPU requests, summed from `helm/chart/src/main/helm/values.yaml` with
  `helm/values/axon-showcase/values-local.yaml` (which sets `commandService.replicaCount: 2`) and the infra/monitoring
  local values: app 2650m (command 2×500m, query 500m, projection 100m, `apiGateway` 1 CPU, webUi 50m) plus infra and
  observability 1760m = ~4.4 CPU against `ubuntu-latest`'s 4 vCPU. The repository's owner is a `User`, so GitHub's
  larger runners (an organization/enterprise feature) are unavailable.
- Every release installs with `wait = true` / `waitForJobs = true`, so a Pending pod becomes a helm timeout rather than
  a retry.
- The plugin's target model supports what the trim needs: a target selects the releases to install by tag, and the
  releases carry them — `database`/`db-events`, `database`/`os-views`, `kafka`, `application`, `monitoring`.
- The app chart's ServiceMonitors are per-service toggles (five), gated on the observability metrics export (off by
  default), and the three infrastructure charts' metrics are off by default as well — the local values do the enabling,
  and they do not reach another target. A target without the observability therefore renders no ServiceMonitor at all,
  so the overlay's explicit disables are belt-and-braces rather than a repair for a missing CRD.

## Goals / Non-Goals

**Goals:**

- Make the smoke's install fit a standard runner, so its dispatch passes and the nightly signal is real.
- Keep the `local` target and its values untouched.
- Keep the smoke's method (the same chart, the same releases by tag, the load profiles) apart from the overlay.

**Non-Goals:**

- No change to the chart's own defaults or to the local deployment contract.
- No change to the load profiles, the reference, or the wrapper.
- No attempt to measure performance in CI — the smoke remains availability-only.

## Decisions

- **D1 — A `ci` release target carries the trim.** The target selects the `database`, `kafka`, and `application` tags
  and the releases it installs carry their trimmed `values-ci.yaml` files, so the difference is contained where a
  target's deployment differences belong — the same model the repository already uses for a future staging context.
  Rejected: reducing the chart's own requests (that changes what a user installs) and installing with raw `helm` from
  the packaged chart (it bypasses the Gradle-filtered values and the documented task graph the smoke is meant to
  exercise).
- **D2 — The overlay disables the ServiceMonitors, drops to one command replica, and lowers the requests.** Monitors off
  by the charts' own defaults wherever the observability metrics export is not enabled, with the explicit disables there
  so a future default change cannot render a `ServiceMonitor` whose CRD is absent; one replica and the smaller requests
  because the app's 1 CPU gateway and doubled command service are what break the budget. Each release the target
  installs carries its own `values-ci.yaml` — the plugin's `values-<target>.yaml` rule, the same convention as
  `values-local.yaml` — so the app release's file holds the app's trims and monitor toggles and each infrastructure
  release's holds its identity keys and settings. One shared file could not: the chart-identity keys
  (`fullnameOverride`, the image repositories, the replica counts) collide across releases.
- **D3 — The `ci` target declares its kube context.** `kind-axon-showcase-smoke`, the workflow's fixed cluster name —
  the staging-context model the repository documents, which means the name is stated in two places and the target
  comments name the coupling. Rejected: reusing the `helm.local.kubeContext` property, whose name ties it to the local
  target.
- **D4 — The smoke requirement is revised.** The merged requirement says "install all six Helm releases through the
  documented local install path"; the smoke installs the `ci` target's releases with their `values-ci.yaml` files
  instead. A `MODIFIED` delta carries that change, and the AGENTS.md smoke paragraph and README follow.
- **D5 — The image-load fix rides this change.** `kind load docker-image --name "$CLUSTER_NAME" "$image"` is necessary
  whatever the install shape, and it is unexercised until the install succeeds, so it ships here rather than alone.

## Risks / Trade-offs

- **The trimmed install diverges from the local path.** → The same chart and the same releases by tag, with a small
  overlay; the divergence is what a target exists for, and the local path keeps its own values and tasks.
- **Reduced resources cut the load smoke's headroom.** → The plateau asserts at 20 units/s against a ~120-unit/s
  measured operating point; if the dispatch shows the trimmed install throttling, the rate comes down or the plateau
  gives way to the `smoke` profile's zero-failure gate, which is what the requirement asks for.
- **The cluster name is stated twice.** → The target's context and the workflow's `CLUSTER_NAME`; a comment on each
  names the other, and a mismatch fails loudly at the install rather than silently.

## Verification

- `spotlessCheck`, `openspec validate --changes`, `workflowLint`, and the Docker-free `check` pass.
- The dispatch after it lands is the smoke's own verification: the cluster is created, the images load with the cluster
  named, the `ci` install fits and becomes ready, the load profiles pass, and the cluster is deleted.
