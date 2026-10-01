# Design

## Context

See `proposal.md` — Why. Facts behind the approach (measured 2026-10-01):

- The failure (`gh run view 36794836897`): `helmInstallAxonShowcaseOsViewsToCi` →
  `StatefulSet/axon-showcase/axon-showcase-os-views-{data,master} not ready. Ready: 0/1`, then
  `helmInstallIngressNginxToCi` → `Deployment/ingress-nginx/ingress-nginx-controller not ready. Available: 0/1`. The
  diagnostics printed OpenSearch `PodInitializing` (`copy-default-plugins`) and the ingress controller
  `ContainerCreating`, both with **0 restarts** — a readiness wait, not a crash loop.
- `build.gradle.kts`'s `helm { releases { all { wait = true; waitForJobs = true } } }` uses Helm's **default
  five-minute** install wait.
- The plugin does expose the wait's length: `HelmServerOperationOptions.getRemoteTimeout()` (a `Duration`, settable as
  the `helm.remoteTimeout` project property), which `HelmServerOperationOptionsApplier` emits as the `--timeout` flag on
  **server operations** (`install`, `upgrade`, `uninstall`, `test`) — not on `helm repo add`/`search`. The release
  targets extend the installation options (`ConfigurableHelmInstallationOptions`), and this repo declares them in
  `helm { releaseTargets { … } }`, where the `ci` target already holds its kube context and a comment tying it to the
  workflow's cluster name.
- The `ci` values already trim requests (`os-views` master/data at `100m` CPU) and pin the probes, so the resources are
  not the lever; the wait is.

## Goals / Non-Goals

**Goals:**

- Let a slow-but-healthy release finish within the install, deterministically, without weakening the readiness
  assertion.

**Non-Goals:**

- Retrying the install, or rewriting the wait in the workflow (`kubectl wait`).
- Changing the chart, the `ci` values, the plugin pin, or the smoke's observational status.
- A per-release timeout distinction: one target-level value covers the smoke's installs.

## Decisions

- **Set `remoteTimeout` on the `ci` target rather than retrying or passing a workflow property.** The target is the
  smoke's own scope and already holds its runner-specific settings (kube context, the cluster-name sync comment), so the
  tolerance lives where it applies and no workflow step changes. Options considered: a bounded retry of the install step
  (rejected — it treats a symptom and doubles a failing run's duration); `-Phelm.remoteTimeout=15m` on the workflow step
  (rejected — the same value, hidden in the workflow and applied to any target the command happens to use);
  `extraArgs = ["--timeout", …]` (rejected — that list is applied by `AbstractHelmCommandTask` to _every_ Helm command,
  and `helm search repo` rejects `--timeout`).
- **Fifteen minutes.** The observed need is a few minutes past five. Because the releases install with `--wait`, a
  slow-but-healthy release is waited for rather than cut off; the budget check is that the independent infrastructure
  releases install in parallel (`org.gradle.parallel`), so the worst case is roughly one fifteen-minute wait for
  `os-views` plus one for the application release that installs after it — inside the job's 60-minute `timeout-minutes`.
- **Rejected: `-Phelm.wait=false` plus explicit `kubectl wait`.** It would have to re-implement the plugin's ordering,
  hook and job handling (`waitForJobs` covers the app chart's post-install jobs).
- **Rejected for now: trimming the `ci` values further.** The wait, not the resources, is the constraint observed; a
  resource change is a hypothesis one dispatch cannot settle, and the values are the spec's "requests fit the runner"
  surface.

## Risks / Trade-offs

- **A genuinely broken release now takes the longer wait to fail.** → Bounded by the 15-minute value and by the job's
  60-minute `timeout-minutes`; the _Diagnose a failed smoke_ step still prints the pods, events, and logs.
- **A target-level value also lengthens `upgrade`/`uninstall` waits on that target.** → Acceptable: the `ci` target
  exists only for the throwaway kind cluster, and longer waits there are the point.
- **The timeout's effect is not directly visible in a green run.** → The change's tasks verify the resolved property
  reaches the install task (an init-script probe) and dispatch the smoke on the branch, so both the setting and the
  end-to-end step are observed.
