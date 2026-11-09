# Design

## Context

See `proposal.md` — Why. The evidence is run `42427617536`'s log (read 2026-11-09):

- The gateway and the projection service were killed for a failed startup probe
  (`Killing pod/axon-showcase-api-gateway-…` `Container main failed startup probe, will be restarted`), while the
  command and query services logged startup-probe failures but survived.
- The gateway's second attempt started at 00:05:48 and was up at 00:06:04
  (`Started ShowcaseApiApplication in 14.949 seconds`); `helmInstallToCi` finished at 00:06:13, and the readiness loop's
  first successful `GET /showcases` was at 00:06:14.
- The load test started at 00:06:22, its writes landed at 00:06:24–00:06:26, and the gateway's Kafka consumer joined
  `showcase-live-events` only at 00:06:30,180 — after the broker removed the killed member
  (`Member … has failed, removing it from the group`, 00:06:30,177).
- The consumer had no committed offset and reset to the end
  (`Resetting offset for partition axon-showcase-events-1 to position … offset=3`), so the three published events were
  skipped.
- The ten SSE connections were answered `200` with 72 bytes and held for 65 s each, and Gatling reported
  `showcaseEvent: count of failed events is 0.0 : false (actual : 10.0)`.

Constraints that shape the approach:

- The chart's startup probe defaults are `initialDelaySeconds: 3`, `periodSeconds: 3`, `timeoutSeconds: 1`,
  `failureThreshold: 10` — a 33-second budget — and `helm/values/axon-showcase/values-ci.yaml` overrides no probe.
- The gateway subscribes to Kafka eagerly at bean creation (`ShowcaseApiApplication#showcaseEventReceiver`) and buffers
  the last 100 consumed events for late subscribers; it re-reads nothing published before its consumer is assigned, and
  it replays that buffer to every new SSE connection.
- The simulation's SSE check accepts any event carrying a `showcaseId` and a `type` (`ShowcaseSimulation.java`, the
  `sseScenario` check), so an event the smoke published while waiting would satisfy it for all ten connections; the
  smoke profile runs no second, mid-hold check.
- In Gatling 3.16.0 a message that fails an SSE check's match conditions is logged as non-matching and the await keeps
  waiting (`io/gatling/http/action/sse/fsm/SsePerformingCheckState.scala`), so a match condition filters rather than
  failing — and a pre-run event cannot turn into a `KO` merely by being replayed first.
- The runner has four vCPUs and boots four JVMs plus OpenSearch and Kafka in one install; `AGENTS.md` already records
  that the same cold-start contention starves a time-sensitive test past its default timeout.

## Goals / Non-Goals

**Goals:**

- A service that is slow to start on the runner is waited for, not restarted.
- The smoke's load never publishes before the gateway's live-event stream can deliver, so the SSE assertion measures the
  deployed pipeline rather than a subscription race.

**Non-Goals:**

- Changing the chart's probe defaults or any other target's values.
- Changing the gateway's product behavior (its offsets, readiness, or replay buffer), the chart templates, or the load
  test's assertions.
- Shortening the install or reducing the number of booting services.

## Decisions

- **Raise the startup-probe tolerance in the `ci` values, not in the chart.** `values-ci.yaml` is the target's "fits the
  runner" surface (the same file that trims requests), so a runner-specific tolerance lives where the runner is named;
  the chart's default stays the human-facing one. The override is `timeoutSeconds: 3` and `failureThreshold: 20` for
  `commandService`, `queryService`, `projectionService`, and `apiGateway` — a 63-second budget, roughly twice the
  33-second window that failed and four times the 16-second successful start. `timeoutSeconds` rises too because the
  failing probes logged `context deadline exceeded`, not only connection refusals.
  - Rejected: raising the chart default. The failure is the runner's concurrent cold start; a machine that boots the
    same four JVMs locally has shown no such need, and a chart default is the value every target inherits.
  - Rejected: lowering the services' CPU requests further. The `ci` values set `250m` for the command, query, and
    gateway services and leave the projection service at the chart's `100m` default; a further cut lengthens the cold
    start, and the observed constraint is start time, not placement.
  - Rejected: increasing `initialDelaySeconds`. The probe needs to start early enough to detect readiness; the budget,
    not the start, is short.

- **Gate the load on the live-event stream inside the existing smoke step.** After the current `/showcases` readiness
  loop, the step publishes a showcase, keeps its ID, and reads `GET /events` with a bounded per-attempt timeout until a
  `data:` frame arrives, republishing each attempt; it fails with a named stage when the bound is exhausted or when a
  publish returns no showcase ID. The existing readiness loop stays first because the publish needs a reachable gateway,
  and the load test runs only after the gate passes, with **every** published ID passed as a comma-separated
  `-PwarmUpShowcaseIds`.
  - The gate keys on a `data:` frame, not on any byte: the stream's keep-alive comment carries bytes without proving
    delivery.
  - Re-publishing per attempt covers a gateway restart clearing the replay buffer between attempts, and the step keeps
    each attempt's ID because an earlier attempt's event can still sit in the buffer: a single ID would leave the
    earlier warm-ups unexcluded.
  - Rejected: `kubectl exec` into the Kafka pod and reading the consumer group's assignment. It reads broker state
    rather than the outcome the load asserts, and depends on the Bitnami image's script path.
  - Rejected: waiting for a gateway log line. Log text is not a contract, and a reworded line would fail the step.
  - Rejected: a fixed sleep. It proves nothing and only widens a race whose margin was ~7 seconds.
  - Rejected: making the gateway's readiness include the Kafka receiver. It would remove a gateway that still serves
    REST whenever Kafka is unwell, and the startup probe targets `/actuator/health`, so the restart risk would grow.
  - Rejected: changing the consumer's `auto.offset.reset` or leaving the SSE assertion as it is. Delivering events
    published while subscribed is the product's intent, and the check must keep proving that; the offsets change the
    product's replay semantics instead.

- **Exclude the gate's warm-up showcases from the SSE checks, by ID.** The simulation reads `warmUpShowcaseIds` (default
  empty, comma-separated) and, for each ID, adds `jsonPath("$.data.showcaseId").not(id)` to the match conditions of both
  SSE message checks — the initial one and the mid-hold one a sustained profile builds. The gateway replays its buffer
  to every new subscriber, so without this a warm-up event would satisfy a check for all ten connections and the
  assertion would measure the gate, not the load.
  - A negative match is enough: each check keeps accepting any other event, including the replayed lifecycle events the
    spec already blesses, so the change adds no ordering dependency between the SSE stream and the HTTP responses.
  - Both checks take the exclusion: a sustained profile with the property set could otherwise let a warm-up replay
    satisfy the mid-hold check.
  - Rejected: recording every showcase ID the run creates and matching only those. The write stream may wait up to its
    five-minute projection timeout after the create response before the next lifecycle event, while the SSE await is 30
    seconds, so filtering on the run's own IDs can time out on a healthy run once the first event arrives before the ID
    is registered.
  - Rejected: filtering by the event's `timestamp` at or after the simulation's start. It couples the check to clock
    agreement between the runner and the containers, which the ID does not.
  - Rejected: publishing no warm-up at all. Proving the stream delivers requires an event to deliver.
  - With the property unset, both checks are exactly today's, so a local run without the smoke's gate behaves as before.

- **Bound the gate inside the job budget.** Each attempt bounds the stream read at 5 seconds and pauses 2 seconds; a
  bounded attempt count (30) caps the gate at a few minutes, leaving the 60-minute job budget for the install and the
  load test.

## Risks / Trade-offs

- A cold start longer than 63 seconds still restarts a service → the tolerance is sized to twice the observed failure
  and the `Diagnose a failed smoke` step still prints pods, events, and logs; a recurrence is evidence the runner, not
  the budget, is the next lever.
- A gateway that never attaches its consumer now fails at the gate instead of at the load assertion → the step names the
  live-event stage, so the diagnosis is more direct than a Gatling timeout.
- The gate adds a write to the deployed read model before the load runs → the load's read stream tolerates a non-empty
  list (it seeds one only when the list is empty).
- The gate's `curl` may see keep-alive comments before the first event → the check ignores them and retries within the
  bound.
- An ID that never reaches the simulation restores the hollowed check → the gate fails when a publish yields no ID,
  collects every attempt's ID, and a task reads the resolved `warmUpShowcaseIds` back from the `gatlingRun` task rather
  than trusting the workflow text.
- A showcase created by the gate stays in the read model for the load's duration → the load's read stream tolerates a
  non-empty list (it seeds one only when the list is empty), and the assertions measure latency and failures, not
  counts.
- `timeout` and `grep` are runner basics (`ubuntu-latest` ships coreutils and GNU grep) → the dispatching run is where
  their availability is first exercised, and a missing binary fails the step by name.

## Claims

Each fact this change states about its evidence, with the command that derives it. The rows are re-derived after the
unit's last edit to its own record.

| Claim                                                                                                                                 | Source                                                                                                                                               | Result                                                                                                                                                                                            |
| ------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Run `42427617536` failed on the SSE check with `showcaseEvent: count of failed events is 0.0 : false (actual : 10.0)`                 | `gh run view 42427617536 --log-failed \| grep 'showcaseEvent: count of failed events'`                                                               | the line prints once                                                                                                                                                                              |
| The gateway and the projection service were restarted by a failed startup probe                                                       | `gh run view 42427617536 --log-failed \| grep 'failed startup probe, will be restarted'`                                                             | two `Killing` event lines, one per pod                                                                                                                                                            |
| The gateway's live-events consumer reset to the end of `axon-showcase-events-1`                                                       | `gh run view 42427617536 --log-failed \| grep 'Resetting offset for partition axon-showcase-events-1'`                                               | the reset line, and the `Found no committed offset` line before it                                                                                                                                |
| The four JVM services' startup probes carry `initialDelaySeconds: 3`, `periodSeconds: 3`, `timeoutSeconds: 1`, `failureThreshold: 10` | `grep -A5 'startupProbe:' helm/chart/src/main/helm/values.yaml`                                                                                      | four identical sets; the fifth `startupProbe:` (web UI, `enabled: false`) carries none                                                                                                            |
| `helm/values/axon-showcase/values-ci.yaml` overrides no probe at the unit's base                                                      | `git show $(git merge-base HEAD origin/main):helm/values/axon-showcase/values-ci.yaml \| grep -c startupProbe`                                       | `0`                                                                                                                                                                                               |
| The gateway subscribes to Kafka at bean creation and replays the last 100 consumed events                                             | `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiApplication.java`, `showcaseEventReceiver`                                               | `KafkaReceiver.create(...)` followed by `.subscribe(...)`, and `Sinks.many().replay().limit(100)`                                                                                                 |
| The smoke reads `/showcases` for readiness, then runs `:load-tests:gatlingRun`                                                        | `.github/workflows/deployment-smoke.yml`, the `Smoke the deployed pipeline` step                                                                     | the `/showcases` readiness loop precedes the `gatlingRun` invocation, with the live-event gate between them                                                                                       |
| The load test's read stream seeds a showcase only when the fetched list carries none                                                  | `load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java`, the `READ` chain                                                           | `jsonPath("$[0].showcaseId").optional().saveAs("showcaseId")`, then `doIf(session -> !session.contains("showcaseId") && SEEDED.compareAndSet(false, true))`                                       |
| The simulation's SSE check accepts any event carrying a `showcaseId` and a `type`, and the smoke profile runs no mid-hold check       | `grep -nE 'checkMessage\\(SSE_CHECK\\)\|SSE_SUSTAINED_WRITES =' load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java`              | the `sseMessageCheck` helper matches the id's existence (plus one `.not(id)` per warm-up id) and checks the type's existence; `SSE_SUSTAINED_WRITES` is `!"smoke".equals(PROFILE) && RATIO < 1.0` |
| Gatling ignores an SSE message failing a check's match conditions and keeps waiting                                                   | `unzip -p <gatling-http 3.16.0 sources jar> io/gatling/http/action/sse/fsm/SsePerformingCheckState.scala \| grep -c 'Received non-matching message'` | `1`, in the branch that buffers the event and returns `NextSseState(this)`; only a matching message reaches the checks                                                                            |
| The Java DSL's JSON-path check offers `not(expected)`                                                                                 | `unzip -p <gatling-core-java 3.16.0 sources jar> io/gatling/javaapi/core/CheckBuilder.java \| grep -c 'public @NonNull Final not('`                  | `4` — the plain and session-function forms on the find and validate builders                                                                                                                      |
| The write stream's wait after a create can exceed the SSE await                                                                       | `grep -nE 'POLL_TIMEOUT =\|SSE_AWAIT =' load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java`                                      | `POLL_TIMEOUT = Duration.ofMinutes(5)` and `SSE_AWAIT = Duration.ofSeconds(30)`                                                                                                                   |

Scan candidates dismissed by kind:

- `path` — every candidate names a file, path, or command this change edits, reads, or quotes; each is present in the
  tree or copied from the repository's own surface.
- `count` — every number is quoted from the run log, the chart values, or arithmetic over them, and the row's `Source`
  names the command that emits it.
- `absolute` — the qualifiers state a scope inside their sentence; each that asserts something about the repository is a
  row above, and the rest state a rejected alternative or a decision's own boundary in this design.

## Migration Plan

No released artifact and no data migration: the change is CI-only plus one load-test property whose default keeps
today's behaviour. Rollback is reverting the values file, the workflow step, and the simulation change. The verification
is two local stub controls (a replay of only the warm-up event must fail the SSE check, and one also emitting an event
for a run-created showcase must pass it) plus the smoke's own dispatch on the change's branch
(`gh workflow run deployment-smoke.yml --ref stabilize-deployment-smoke-startup`).
