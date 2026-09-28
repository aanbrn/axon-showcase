# Design

## Context

See `proposal.md` — Why. The smoke's second dispatch (run 36354957471) failed on 37 `PollShowcase` KOs — gateway
`503 Service Unavailable` responses in a nine-second window at the `baseline` plateau's start — not on the port-forward,
whose only errors came two minutes later at teardown and caused no failures. That diagnosis belongs to the plateau,
which this workflow no longer runs (CI drives the `smoke` profile; the performance profiles are local), and its fix
ships separately. The ingress route is therefore a coverage improvement: the chart ships the app's ingress (the local
values set the hostnames `axon-showcase-api`, `-ui`, `-grafana`), the `ci` values leave it off, and the smoke has never
exercised it — so a wrong `ingressClassName`, host, path, or backend would ship unnoticed. `kind` documents mapping the
host's ports into the node for an ingress controller. A release's tags and a target's `selectTags` decide which releases
it installs; the `local` target currently selects `*`.

## Goals / Non-Goals

**Goals:**

- The smoke's load path is the deployment's own access path — the ingress the chart ships.
- The smoke covers the chart's ingress route (class, host, path, backend), which a port-forward bypasses.
- Remove the port-forward, a hop whose only observed error came at teardown and caused no failures.
- Leave the local target, the chart, the profiles, and the trimmed install untouched.

**Non-Goals:**

- No change to the chart's ingress templates, the load profiles, the reference work, or the CI install's scope.
- No attempt to measure performance in CI.

## Decisions

- **D1 — The ingress controller is a catalog-pinned Helm release, `ci`-only.** A new release with its own tag
  (`ci-ingress`), selected by the `ci` target and excluded by the `local` one through its tag expression. Its chart
  version lives in the version catalog, so `helmUpdates` tracks it like every other pinned chart; a raw
  `helm install --version` in the workflow would be invisible to the update checks and drift.
- **D2 — The cluster is created for an ingress controller.** A kind configuration maps the host's 80 and 443 into the
  node and labels it `ingress-ready=true`; the controller's `values-ci.yaml` binds those host ports and selects that
  node label — the documented kind setup, expressed through the chart's values rather than a `kubectl apply` of an
  unpinned manifest.
- **D3 — The profiles target the ingress hostname.** `http://axon-showcase-api`, the same `baseUrl` the local records
  use, resolved by an `/etc/hosts` entry the workflow writes (the runner has passwordless sudo); the port-forward step
  is deleted.
- **D4 — The `ci` values enable the gateway's ingress.** The chart renders the ingress only where a target enables it,
  so the smoke's traffic traverses it — the path a user takes, which the port-forward bypassed.
- **D5 — The requirement is revised.** `merge-governance`'s smoke requirement says the profiles run against the deployed
  gateway over a port-forward; it now says through the ingress the deployment ships. A `MODIFIED` delta carries every
  existing scenario in the main spec's order, with one added for the ingress path.

## Risks / Trade-offs

- **The controller adds footprint to the runner's 4 CPU.** → Its requests are small (~100m) and the trimmed install
  leaves ~2.4 CPU of headroom.
- **The ingress adds a moving part.** → The `ci` install waits for the releases to become ready, and the load run fails
  loudly if the ingress is not serving.
- **The hosts entry mutates the runner.** → Ephemeral by design; a runner is discarded after the job.

## Verification

- `spotlessCheck`, `openspec validate --changes`, `workflowLint`, and the Docker-free `check`.
- The dispatch after it lands: the smoke passes through the ingress — no port-forward in the log — and the cluster is
  deleted.
