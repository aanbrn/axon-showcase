# Design

## Context

The pin is one coordinate in the version catalog, but the verification is a live install: the chart is a third-party
observability stack whose templates no local test exercises, and `verifyInfraImageVersions` covers only the Bitnami
infra charts (it gates their preconfigured `image.tag` against the test-surface `*-image-tag` pins, which the
observability charts do not carry). So the catalog pin, the `helmUpdates` report and the documented install command are
the static gates, and the release itself is the real check.

A previous bump recorded that a hand-run `helm search repo` named an older version than the check's fresh run, because
`helm search repo` reads the local index. That is why the target here is taken from `./gradlew helmUpdates`, which
refreshes the repositories before searching, and confirmed a second way from a refreshed index.

The local cluster carried `kps` at revision 1 on chart `92.13.1` (installed 2026-10-26), so this unit's install is an
upgrade of a running release rather than a fresh install.

## Goals / Non-Goals

**Goals:**

- Move the coordinate to the resolved version and keep the documented install command in step with it.
- Verify the bumped chart by installing it and reading the deployed signal: pods ready, Prometheus targets up, Grafana
  datasources wired.

**Non-Goals:**

- Any other chart or coordinate: a row another update check reports is its own unit.
- Freezing the run image, or changing the application chart's own dependency.

## Decisions

1. **Re-resolve with the check, not a hand search.** `helmUpdates` refreshes the repositories before it searches, so its
   row is the current one; a hand-run `helm search repo` reads whatever index is on disk. The row was confirmed a second
   way from the `prometheus-community` repository's refreshed index.
2. **Install the bumped chart into the running local release.** It is an upgrade rather than a fresh install, which is
   what a real deployment does and what makes the smoke meaningful; the release's revision and chart version are
   recorded as the evidence.
3. **Read the deployed signal, not the config.** A green `check` and a correct catalog value prove the configuration;
   the verification reads the pods' readiness, Prometheus's own target list, and Grafana's datasources.

## Risks / Trade-offs

- **A minor chart bump can still change templates.** The live install is what covers that; the static gates cannot.
- **The smoke runs against the local cluster**, so it is not a hermetic check: the release it upgraded was already
  installed there. The recorded revision and chart version are what make the run attributable.

## Verification

- `./gradlew helmUpdates` after the bump — "No Helm updates available."
- `./gradlew verifyInstallCommands` — the documented command matches the catalog pin, with a known-bad control
  (`92.13.1` fails the task with the mismatch message).
- `./gradlew helmInstallKpsToLocal` — the release upgrades to the new chart version.
- The deployed signal: `helm list -n monitoring` (revision and chart), `kubectl get pods -n monitoring` (readiness),
  Prometheus's `/api/v1/targets` (up/down counts), and Grafana's `/api/datasources` and `/api/search`.
- `./gradlew spotlessApply`/`spotlessCheck` and `openspec validate --all`.

## Claims

| Claim                            | Source                                                                     |
| -------------------------------- | -------------------------------------------------------------------------- |
| The chart's resolved target      | `./gradlew helmUpdates` → `92.13.1 -> 92.17.0`, still current              |
| The target is the newest release | refreshed `helm search repo … --versions` lists `92.17.0` first            |
| One chart is reported            | the report prints one `->` line; tempo and the Bitnami charts are absent   |
| The finding is the CI one        | `gh issue view 40` — its 2026-10-31 body carries the same row              |
| The documented command matches   | `./gradlew verifyInstallCommands` → BUILD SUCCESSFUL                       |
| The gate reads the edited file   | the same task fails on `--version 92.13.1`, naming the catalog's `92.17.0` |
| The release upgraded             | `helm list -n monitoring` → `kps` rev 2 at `kube-prometheus-stack-92.17.0` |
| The monitoring pods are ready    | `kubectl get pods -n monitoring` → every pod Running and Ready             |
| Prometheus targets are up        | `/api/v1/targets?state=active` → 14 active, 14 up, 0 down                  |
| Grafana reads its datasources    | `/api/datasources` → Prometheus, Tempo; `/api/search` → `Axon Showcase`    |
| The pre-upgrade release state    | `helm list -n monitoring` before the upgrade → `kps` rev 1 on `92.13.1`    |
| The old pin is in no live doc    | `git grep -n 92\.13\.1 -- . ':!openspec/changes'` exits 1                  |

**Dismissals.** `scripts/claim-scan.py --base origin/main` emits four candidate shapes — `count`, `absolute`,
`cross-ref` and `path` — and each is a lead rather than a claim: a candidate is a claim only where a row states it, and
the rest are pointers a reader resolves against the repository.
