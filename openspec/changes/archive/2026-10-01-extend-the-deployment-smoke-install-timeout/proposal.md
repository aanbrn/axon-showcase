# Proposal: Let the deployment smoke's install wait longer

## Why

The nightly **Deployment Smoke** failed at _Install the releases_ with a Helm `--wait` timeout: the OpenSearch
StatefulSets (`axon-showcase-os-views-data`/`-master`) and the ingress controller were still `0/1` when Helm's default
five-minute wait expired. The diagnostics show them initializing with **zero restarts** — a slow 4-vCPU runner, not a
crash — and the workflow is intermittent (failed 09-27, passed 09-28/09-29, failed 10-01). The Helm Gradle plugin does
expose the lever: `remoteTimeout` (the `helm.remoteTimeout` project property), which its server-operation applier passes
as `--timeout` to `install`/`upgrade`/`uninstall`/`test` — so the smoke's target can wait longer than Helm's default.

## What Changes

- `build.gradle.kts` — the `ci` release target (the deployment smoke's target, which already carries its kube context
  and values) sets `remoteTimeout` well above Helm's five-minute default, so a slow-but-healthy release is waited for
  instead of failing the run.
- `AGENTS.md` — the deployment-smoke note records that its install wait is longer than Helm's default, and why.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. The smoke's contract ("install the releases and wait for them to become ready") is unchanged;
only the wait's length changes, and no scenario's outcome differs.

## Impact

- **Build config**: one property on the `ci` release target. No chart, values, workflow, formatter, or plugin change.
- **Timing**: a slow-but-healthy install completes rather than failing; a genuinely broken release now takes the longer
  wait to fail, still inside the job's 60-minute `timeout-minutes`.
