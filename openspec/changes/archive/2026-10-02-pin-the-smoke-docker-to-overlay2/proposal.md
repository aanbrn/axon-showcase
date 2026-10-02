# Proposal: Keep the smoke's web UI build working on Docker 29

## Why

The deployment smoke's web UI image — the `pack`-built Paketo NGINX image — fails on runners whose Docker is 29.x, which
is what `ubuntu-26.04` ships (Docker 29.4.2): `:showcase-web-ui:dockerBuildImage` errors with
`failed to fetch base layers: open /tmp/imgutil.local.image.*: no such file or directory`, reproducibly, while the four
JVM images build. The trigger is Docker 29's new default image store (the `overlayfs` snapshotter), whose documented
workaround is to switch the daemon back to `overlay2` (`spring-projects/spring-boot#49251`, whose error text and
workaround commit match). The `ubuntu-latest` label migrates to 26.04 between 2026-10-19 and 2026-11-19, so without this
the nightly smoke starts failing when the label moves.

## What Changes

- `.github/workflows/deployment-smoke.yml` — a step, before the image builds, that writes
  `{"storage-driver":"overlay2"}` to `/etc/docker/daemon.json` on the runner and restarts Docker, with a comment naming
  the reason and the owning reference; the step is **unconditional** — the failure is Docker-29-specific, but detecting
  the version adds a branch that must itself be right, and 24.04 is _expected_ to be `overlay2` already (the
  default-path dispatch verifies that).
- `AGENTS.md` — the buildpack note records the constraint: a Docker 29 daemon needs `overlay2` for the `pack` web UI
  build, and the step retires when `imgutil`/the buildpack fix lands (`buildpacks/pack#2527`); and the `ubuntu-latest`
  bullet stops saying the fix is parked.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. The smoke's requirement is outcome-based (its five scenarios assert that the job creates a
cluster, installs and drives the smoke, is not a merge gate, and deletes the cluster before the job ends); the step
changes the runner's daemon configuration, not any of those outcomes, on either path — the default-path run still
creates the cluster, drives the smoke, and cleans up, exactly as the scenarios state.

## Impact

- **CI**: one workflow gains an early step; other workflows are untouched (only the smoke builds the web UI image — the
  other workflows build no image, or build only via `bootBuildImage`, which this does not affect).
- **Validation**: a dispatch of the smoke on `ubuntu-26.04` (its `runner` input) is the check, and it currently fails —
  this change makes it pass.
- **Deployment**: none.
