## Why

The gateway's SSE endpoint is specified to "stay connected and deliver events continuously while the gateway runs", but
it emits nothing while no domain event occurs: `ShowcaseEventStreamController` maps the domain-event flux straight onto
`ServerSentEvent`s with no idle keep-alive, and the chart sets no proxy read timeout for the endpoint. A quiet period
therefore leaves an open connection carrying no bytes, so an intermediary — the ingress in the deployed environment —
can close it, silently ending the live timeline the web UI renders. Every existing test feeds an event immediately, so
the gap is invisible until a deployed stream sits idle.

## What Changes

- `ShowcaseEventStreamController` (with `ShowcaseEventStreamConfiguration`) keeps an idle stream alive by emitting a
  periodic Server-Sent-Events comment, so an open connection always carries bytes and an intermediary does not idle it
  out. Comment frames are ignored by `EventSource`, so no client change is needed.
- The keep-alive interval becomes a configuration property with a default — the Java `@ConfigurationProperties`, the
  `application.yml` placeholder, the `BPE_DEFAULT_*` launch-environment default the gateway's `bootBuildImage` bakes
  into its image, and a chart value rendered as environment — so a deployment can align it with its ingress's read
  timeout instead of editing code.
- `AGENTS.md` gains the heartbeat-versus-proxy-timeout relationship, and the chart's existing `apiGateway.annotations`
  value keeps carrying the read-timeout annotation where an operator needs one. No controller-specific default is set in
  the chart: the annotation name depends on the ingress controller (the local target runs Traefik on colima and
  ingress-nginx on kind/minikube), and a keep-alive interval below the ingress's read timeout makes a raised timeout
  unnecessary in the first place.
- Verification: a unit test asserting the keep-alive on an idle stream, and a live `helmInstallToLocal` check that an
  idle SSE connection survives the deployed ingress — the open question the parked idea recorded.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/gateway/live-events`: the "Stream lifecycle and failures" requirement gains idle continuity — the stream
  must not go silent long enough for an intermediary to close it — plus a scenario covering an idle connection, and the
  keep-alive interval becomes configurable.
- `showcase/deployment/helm-chart`: the "API gateway runtime tuning" requirement gains the live event stream's
  keep-alive interval among the settings the api-gateway Deployment wires through environment.

## Impact

- **Code**: `showcase-api-gateway` production code (the event stream controller and its configuration) and its component
  test; the gateway's `build.gradle.kts` (one `BPE_DEFAULT_*` image default); `helm/chart` values and the api-gateway
  Deployment template (one new environment variable).
- **Behavior**: an idle open stream now receives periodic comment frames. The SSE payload contract is unchanged — no new
  event type, no `id:` field, and the deliberate connect-time replay is untouched, so the web UI's reconciliation path
  is unaffected.
- **Build/tests**: no new dependency and no build-logic change; `componentTest` covers the keep-alive, with the deployed
  path checked by a manual Helm install rather than a gated test (the e2e suites boot compose, not the chart).
- **Deployment**: the chart's api-gateway Deployment gains one environment variable for the keep-alive interval; an
  operator may still raise a proxy read timeout through the existing `apiGateway.annotations` value.
