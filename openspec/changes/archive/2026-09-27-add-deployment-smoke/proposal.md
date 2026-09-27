# Proposal: Add a scheduled deployment smoke

## Why

The chart's deployment path is only exercised by hand — a `kind` install during a chart bump (`bump-kps-chart-91`) — and
the load path only against a developer's own cluster. Nothing installs all six releases and drives load at them on a
schedule, so a chart change that installs but serves nothing, or serves with failures, is caught only if someone looks.
The nightly e2e boots the pipeline — one suite through Testcontainers, the other from compose — which exercises the
images but not the chart's values, probes, and resource wiring, the deployment contract a user actually installs.

## What Changes

- A new observational workflow (`.github/workflows/deployment-smoke.yml`), scheduled nightly and dispatchable: create a
  Kubernetes cluster in the runner, build the five images (four services and the web UI) and load them into it, install
  all six releases through the documented local install path, drive the load profile against the deployed gateway, and
  tear the cluster down.
- The smoke asserts zero failed requests and records no performance numbers; it is never a merge gate.
- `merge-governance` gains an ADDED requirement for the smoke's schedule, trigger, method, and non-gating role.
- Docs: `AGENTS.md`'s CI section and `README.md`; the implemented `docs/ideas.md` entry is removed, and the degradation
  gap this reshape surfaced is parked there as a new idea.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/merge-governance` — an ADDED requirement: the deployment smoke runs on a schedule and on demand, is
  separate from the merge gate, and leaves no cluster behind.

## Impact

- **Build**: a new workflow file only; no build-logic or dependency change. The install reuses the existing
  `helmInstall<Release>ToLocal` tasks (whose `wait`/`waitForJobs` already wait for readiness) against the cluster's
  context.
- **Tests**: verified by dispatching the workflow after it lands (its own first real run, per the repository's rule for
  a new `workflow_dispatch` workflow), and locally by running the same steps by hand where a cluster is available.
- **Docs**: `AGENTS.md` (the CI workflow list, the load-test block), `README.md`, `docs/ideas.md`.
- **Deployment**: none — the workflow is observational and provisions its own throwaway cluster.
