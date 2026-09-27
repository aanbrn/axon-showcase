# Proposal: Trim the deployment smoke's install to fit a runner

## Why

The deployment smoke's first dispatch failed before its load run: `kind load docker-image` looked for a cluster named
`kind` although the cluster was created as `axon-showcase-smoke` (the fix, `--name`, is in this change). Behind that
lies the real blocker — the install cannot fit the runner. The app release's requests alone are 2650m (command 2×500m
from the local replica override, query 500m, projection 100m, `apiGateway` 1 CPU, webUi 50m) and the infra plus
observability add 1760m (postgres 300m, kafka 550m, opensearch 500m, kps 310m, tempo 100m): about 4.4 CPU of requests
against `ubuntu-latest`'s 4, and larger runners are unavailable on a personal account — so a pod would stay Pending and
the install's `wait` would time out. The observability is not needed for a load smoke either, and it is its
ServiceMonitors that pull kps in.

## What Changes

- A `ci` release target in `build.gradle.kts`: it selects the `database`, `kafka`, and `application` tags — no
  `monitoring` — declares its own kube context, and relies on a `values-ci.yaml` per release it installs, so the `local`
  target and values are untouched.
- A `values-ci.yaml` per release the target installs, following the repository's `values-<target>.yaml` convention (the
  plugin applies a release's file only for the target it names, as `values-local.yaml` shows): the app release's holds
  its trims — the ServiceMonitors off, one command replica, the smaller requests — and each infrastructure release's
  holds its identity keys and settings, together bringing the install's requests under the runner's 4 CPU with headroom.
- `.github/workflows/deployment-smoke.yml` installs through the `ci` target's aggregate and keeps the `--name` fix on
  the image load.
- `merge-governance`'s smoke requirement is revised: the smoke installs the `ci` target's releases with their
  `values-ci.yaml` files instead of all six through the documented local path.
- `AGENTS.md` (the smoke paragraph and the Helm section) and `README.md` record the target.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/merge-governance` — the deployment smoke's install scope: the `ci` target's releases and overlay,
  not all six through the local path.

## Impact

- **Build**: `build.gradle.kts` (a release target, and a `values-ci.yaml` per release it installs), the workflow; the
  smoke's requirement; docs.
- **Tests**: verified by dispatching the workflow after it lands — the smoke's own run is the only verification it
  admits.
- **Deployment**: none — the target is CI-only, and the `local` path is unchanged.
