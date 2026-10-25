# Design

## Context

The pin is one coordinate in the version catalog, but the verification is a live install: the chart is a third-party
observability stack whose templates no local test exercises, and `verifyInfraImageVersions` covers only the Bitnami
infra charts (it gates their preconfigured `image.tag` against the test-surface `*-image-tag` pins, which the
observability charts do not carry). So the catalog pin, the `helmUpdates` report and the documented install command are
the static gates, and the release itself is the real check.

A previous bump recorded that a hand-run `helm search repo` named an older version than the check's fresh run, because
`helm search repo` reads the local index. That is why the target here is taken from `./gradlew helmUpdates`, which
refreshes the repositories before searching.

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
   way, from the chart repository's published `index.yaml`, because the local network path failed intermittently during
   this unit (see Risks).
2. **Install the bumped chart into the running local release.** It is an upgrade rather than a fresh install, which is
   what a real deployment does and what makes the smoke meaningful; the release's revision and chart version are
   recorded as the evidence.
3. **Read the deployed signal, not the config.** A green `check` and a correct catalog value prove the configuration;
   the verification reads the pods' readiness, Prometheus's own target list, Grafana's datasources, and the dashboards
   Grafana provisions.

## Risks / Trade-offs

- **A minor chart bump can still change templates.** The live install is what covers that; the static gates cannot.
- **The smoke runs against the local cluster**, so it is not a hermetic check: the release it upgraded was already
  installed there. The recorded revision and chart version are what make the run attributable.
- **The chart-repository fetch failed intermittently during this unit.** Repository updates failed with a Go-client
  socket error (`write: socket is not connected`) and then with
  `context deadline exceeded (Client.Timeout or context cancellation while reading body)`, and a retry succeeded. `curl`
  reached the same index over IPv4 and IPv6 in between, so the pin was not re-resolved on that evidence — but a working
  `curl` and a successful retry do not localize the cause, which stays unestablished rather than established as host
  state.
- **The monitoring pods carry restarts from before this unit.** Each container's last termination is stamped
  `2026-10-25T11:06:31Z`, ahead of the upgrade at `11:15Z`, and the untouched `tempo` release carries the same instant,
  so they are cluster state rather than the bumped chart.

## Verification

- `./gradlew helmUpdates` after the bump — "No Helm updates available."
- `./gradlew verifyInstallCommands` — the documented command matches the catalog pin.
- `./gradlew helmInstallKpsToLocal` — the release upgrades to the new chart version.
- The deployed signal: `helm list -n monitoring` (revision and chart), `kubectl get pods -n monitoring` (readiness),
  Prometheus's `/api/v1/targets` (up/down counts), and Grafana's `/api/datasources` and `/api/search`.
- `./gradlew spotlessApply`/`spotlessCheck` and `openspec validate --all`.

## Claims

| Claim                            | Source                                                                          |
| -------------------------------- | ------------------------------------------------------------------------------- |
| The chart's resolved target      | `./gradlew helmUpdates` → `92.11.0 -> 92.13.1`, still current                   |
| The target is the newest release | the chart repository's `index.yaml` lists `92.13.1` as newest                   |
| One chart is reported            | one `->` line; `grafana-tempo` and the Bitnami charts are absent                |
| The finding is the CI one        | `gh run view 40130476307 --log`; issue #40's comment says the same              |
| The documented command matches   | `./gradlew verifyInstallCommands` → BUILD SUCCESSFUL                            |
| The release upgraded             | `helm list -n monitoring` → `kps` rev 2 at chart `92.13.1`                      |
| The monitoring pods are ready    | `kubectl get pods -n monitoring` → every pod Running and Ready                  |
| Prometheus targets are up        | `/api/v1/targets?state=active` → 14 active, 14 up, 0 down                       |
| Grafana reads its datasources    | `/api/datasources` → Prometheus, Tempo; `/api/search` → `Axon Showcase`         |
| The restarts predate the upgrade | `finishedAt` `2026-10-25T11:06:31Z` precedes the upgrade, as `tempo-0`'s does   |
| The old pin is in no live doc    | `git grep -n 92\.11\.0 -- ':!openspec/changes'` exits 1                         |
| The install path adds no retry   | `AbstractHelmRepositoriesTask` + 2 subclasses; plugin repo tasks carry none     |
| The span defect recurred         | `AGENTS.md` carries the #359 span; this unit's round 1 found the five-space run |

**Dismissals.** `scripts/claim-scan.py --base origin/main` emits four candidate shapes — `count`, `absolute`,
`cross-ref` and `path` — and each is a lead rather than a claim: a candidate is a claim only where a row states it, and
the rest are pointers a reader resolves against the repository.
