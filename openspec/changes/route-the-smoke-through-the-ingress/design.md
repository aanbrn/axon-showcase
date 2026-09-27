# Design

> **Parked, 2026-09-28 — the premise below was wrong.** The smoke's failing assertion was not the port-forward: the
> review of this change reproduced run 36354957471 and every one of its 37 `PollShowcase` KOs is a gateway
> `503 Service Unavailable` in a nine-second window at the `baseline` plateau's start, while the port-forward's only
> errors came two minutes later at teardown and caused no failures. I misdiagnosed the failing hop (a log slice hid the
> 503s); the route below is still the faithful way for the smoke to reach the deployment, but it would not have fixed
> the failure. The 503s are diagnosed and fixed in a separate change; this branch stays unmerged until the ingress route
> is reconsidered on its own merits.

## Context

See `proposal.md` — Why. The smoke's second dispatch (run 36354957471): the cluster, the images (loaded with `--name`),
the trimmed `ci` install, and the `smoke` profile all succeeded; the `baseline` plateau failed on 37 `PollShowcase` KOs,
with
`portforward.go:404 "Unhandled Error" err="error copying from local connection to remote stream: … read: connection reset by peer"`
at 22:30:27 — the port-forward giving way under the sustained load and ten long-lived SSE connections. The chart ships
the app's ingress (the local values set the hostnames `axon-showcase-api`, `-ui`, `-grafana`), and `kind` documents
mapping the host's ports into the node for an ingress controller. A release's tags and a target's `selectTags` decide
which releases it installs; the `local` target currently selects `*`.

## Goals / Non-Goals

**Goals:**

- The smoke's load path is the deployment's own access path — the ingress the chart ships.
- Remove the port-forward, the fragile hop that failed the last dispatch.
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
- The dispatch after it lands: the profiles pass through the ingress — no port-forward in the log — and the cluster is
  deleted.
