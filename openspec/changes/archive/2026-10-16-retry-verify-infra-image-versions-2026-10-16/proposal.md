# Proposal

## Why

`verifyInfraImageVersions` resolves each pinned chart's preconfigured `image.tag` over the network, and the shared
`addHelmRepositories` setup (`helm repo add` + `helm repo update`, once per repository) retried nothing, so a
**transient** repository failure failed `check` — the failing repository differing between runs, and the task passing
alone and on a warm cache. The cost is not a red build but a gate people learn to run minus a check: the documented
interim was `check -x verifyInfraImageVersions`, i.e. verifying the rest of the gate with one of its members removed —
and because the task is cacheable on its inputs, the cold path it takes is exactly the one a dependency or chart bump
forces, the shape that most needs the gate. `tolerateFailures = true` is the wrong lever: the repository report
tolerates an unreachable repository by design, but tolerating it here would turn the drift check into a false pass.

## What Changes

- **`build-logic`'s `BoundedRetry`** (new) — a small pure helper that re-runs an operation a bounded number of times
  with a pause between attempts, reporting each transient failure and propagating the last attempt's failure.
- **`AbstractHelmRepositoriesTask`** — the repository add/update pair runs through the retry, so both callers
  (`VerifyInfraImageVersionsTask` and the report's `HelmUpdatesTask`) are covered at the one point the network call
  happens. The toleration path is unchanged, and so is the failure: a repository that stays unreachable still fails the
  check after the bounded attempts.
- **`build-logic`'s `BoundedRetryTests`** (new) — the retry's own evidence: a transient failure succeeds on a later
  attempt (counting attempts and the reported failures), a persistent one reports its last failure, one attempt never
  retries, and fewer than one attempt is rejected.
- **`showcase/quality/infra-image-versions`** — the "Drift between surfaces is caught by the build" requirement gains
  the resilience clause (the setup is retried, and the retry does not weaken the failure) plus a scenario for both
  directions.
- **`docs/ideas.md`** — this change's entry is removed, and so is the stale entry for the actionlint-download retry in
  the merge gate, whose remedy text had outlived its implementation (#463).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/infra-image-versions`: the "Drift between surfaces is caught by the build" requirement states that
  the chart-repository setup is retried a bounded number of times before the verification fails, and that the retry does
  not weaken the failure.

## Impact

- **The documented workaround retires**: `check -x verifyInfraImageVersions` is no longer the interim way to run the
  gate.
- A transient repository failure no longer reddens `check` (or the cold `main` run); a persistent one still does, after
  the bounded attempts.
