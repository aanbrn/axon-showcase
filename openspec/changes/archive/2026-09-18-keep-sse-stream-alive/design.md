## Context

See `proposal.md` for motivation. The current state that shapes the approach:

- `ShowcaseEventStreamController` maps the hot `Flux<ShowcaseEventDto>` from `ShowcaseEventStreamConfiguration` directly
  onto `ServerSentEvent`s (`ServerSentEvent.builder(event).event("showcase").build()`), so nothing is emitted between
  domain events.
- That source flux is a `Sinks.many().replay().limit(100)` fed eagerly from Kafka: late subscribers receive a replay of
  the last 100 events. The web UI relies on that replay to seed its timeline, which is why a reconnect deliberately
  re-receives the buffer instead of resuming.
- `ShowcaseApiProperties` (`@ConfigurationProperties("showcase.api")`, `@Data`, `@Validated`) is the Java-owned default
  surface ADR-0002 designates for this service, mirrored by `application.yml`'s `${ENV:default}` placeholders and the
  chart's env rendering. The CORS setting follows exactly that path and is the precedent to copy.
- The chart's `apiGateway.annotations` value is already declared and `@param`-documented, and the ingress template
  merges it onto the rendered Ingress — so an operator can already set an ingress-controller-specific annotation with no
  chart change.

## Goals / Non-Goals

**Goals:**

- An open SSE connection always carries bytes, so an intermediary cannot idle it out during a quiet period.
- The interval is deployment-tunable, with a single authoritative default (ADR-0002).
- The keep-alive is invisible to event consumers: no new event type, no payload change.

**Non-Goals:**

- No event `id:` and no change to reconnect semantics; the connect-time replay stays the UI's timeline seed.
- No web-UI change — `EventSource` ignores comment frames.
- No chart default for a proxy read timeout, and no gated test for the deployed ingress path: the e2e suites boot
  compose, not the chart, so that half is verified by a live install instead.

## Decisions

### Keep the heartbeat in the SSE framing layer, not in the shared event flux

The controller merges the mapped event stream with a `Flux.interval(interval)` of comment-only frames built as
`ServerSentEvent.builder().comment("keep-alive").build()`. Both the no-argument `builder()` and `comment(String)` exist
on `spring-web` 6.2.19's `ServerSentEvent`, and comment frames are ignored by `EventSource`, so no client can mistake
one for an event and no client change is needed.

- _Alternative — emit keep-alives from `showcaseEventStream` itself:_ rejected. That bean is typed as the domain DTO
  flux and is consumed as such; putting non-DTO traffic in it would force every consumer to filter it.
- _Alternative — an SSE `retry:` directive:_ rejected. It configures the client's reconnect delay; it does not keep an
  idle connection alive.

### Make the interval a property, declared on every surface its default lives on

A duration under the `showcase.api` prefix, owned by `ShowcaseApiProperties`, mirrored by an `application.yml`
`${ENV:default}` placeholder, by a `BPE_DEFAULT_*` launch-environment default in the gateway's `bootBuildImage`, and by
a chart value rendered as environment. That is four surfaces: the image is one ADR-0002's enumeration did not name until
this change completed it (task 3.4), and it is where `paketo-buildpacks/environment-variables` sets a launch-environment
default for the variable, so a deployment's real environment variable overrides it while it in turn overrides the yml's
fallback — which is exactly the CORS precedent (`BPE_DEFAULT_SHOWCASE_CORS_ALLOWED_ORIGINS=""` is how the image ships
fail-closed). The default is 15 seconds: short enough that an ingress read timeout is unlikely to elapse between frames,
and long enough that the added traffic is negligible. The value is tunable because the safe bound is
deployment-specific.

- _Alternative — a fixed constant:_ rejected. A deployment behind an ingress with a shorter read timeout would have no
  remedy, and the bound would be baked into the specification instead of the deployment.
- The chart env does change a Helm-chart requirement: `showcase/deployment/helm-chart`'s "API gateway runtime tuning"
  enumerates the settings the api-gateway Deployment wires through environment (the query-service URL, the two query
  caches, the resilience4j environment), so the keep-alive interval is folded into that enumeration rather than left as
  an unwired extra. CORS runs the other way — `showcase/gateway/rest-api` requires its origins to be configurable while
  no Helm-chart requirement covers its chart env — which is why this was decided explicitly rather than by following the
  nearest precedent.
- A comment-only frame is invisible to any client decoding the response as something other than `ServerSentEvent`:
  `ServerSentEventHttpMessageReader.buildEvent` returns the decoded data — null when the frame carries none — for a
  non-`ServerSentEvent` target, and the caller maps null to `Mono.empty()` (`spring-web` 6.2.19). The keep-alive
  therefore reaches an `EventSource` (which sees the raw comment line) while a `String`-decoded client never observes
  it, so no existing client or test behavior changes.

### Do not add `id:`, and do not change reconnect

Adding an event `id:` would make browsers resume via `Last-Event-ID` on reconnect. The web UI's timeline is seeded from
the connect-time replay, so a resuming reconnect would deliver only the missed tail and leave the timeline incomplete —
and ignoring the header while sending it invites duplicate delivery. Continuity and reconnect are therefore kept
separate: this change fixes idleness only.

- _Alternative — full SSE semantics (heartbeat + `id:` + true resume):_ rejected as a materially bigger change: it
  reworks the client's initial-state path and needs deltas on both the gateway and web-UI capabilities.
- _Alternative — `id:` for diagnostics while ignoring `Last-Event-ID`:_ rejected. The header would be sent by every
  browser and honored by none, so the mismatch becomes a latent duplicate-delivery bug.

### Document and verify the proxy timeout instead of defaulting it

The read-timeout annotation is ingress-controller-specific — the local target runs Traefik on colima and ingress-nginx
on kind/minikube, and the chart cannot know which — so a baked-in default would be wrong for one of them. Since a
heartbeat interval below the ingress's read timeout makes a raised timeout unnecessary, the chart gains documentation
rather than behavior: the annotation is surfaced where operators look (`values.yaml` and `AGENTS.md`), and the deployed
behavior is established by a live install.

- _Alternative — default the nginx annotation in the chart:_ rejected. It is inert on the Traefik-based local target and
  would be dead configuration there — the same shape of mistake the `*-client` label change reverted.
- The design deliberately does not record any controller's default read timeout: those are third-party values, so the
  live check is what settles whether an interval needs raising at all.

## Risks / Trade-offs

- **A keep-alive on a fixed interval also fires during busy periods** → harmless: a comment carries no event, and
  clients ignore it. An idle-only variant (suppressing the tick while events flow) was considered and rejected as extra
  machinery for no observable difference.
- **An interval set too long for a given ingress lets the connection still be closed** → the property is tunable, the
  relationship is documented in `AGENTS.md`, and the live install task checks an idle connection across the ingress.
- **A heartbeat can mask a stalled upstream as "alive"** → it cannot fabricate events; the stream stays connected and
  emits nothing but comments, exactly as today, and a failed Kafka subscription is still logged as it is now.
- **Constant background traffic per open stream** → each frame is a handful of bytes on an interval of seconds, and the
  replay buffer is unaffected because heartbeats are generated downstream of the sink, not stored in it.
