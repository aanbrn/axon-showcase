# Design

## Context

See `proposal.md` — Why. The committed reference's write-lifecycle tails are attributed unevenly by its own Notes, and
the per-request history that would test that attribution is recoverable from git rather than from any record. The facts
this change rests on are in the table below, each with the source that derives it.

## Claims table

| Fact                                                                                                                                                                                                                                                                | Stated in   | Source (re-run)                                                                                                                                                                                                                                                                                                                               |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| the committed reference's per-request p99 is not uniform (list read 14 ms against poll 56, start 45, remove 30)                                                                                                                                                     | `design.md` | `grep -n "p99Ms" load-tests/src/gatling/resources/baseline.properties`                                                                                                                                                                                                                                                                        |
| the recorded operating point is 127 units/s, the basis of the rate match                                                                                                                                                                                            | `tasks.md`  | `grep -n "operatingPoint" load-tests/src/gatling/resources/baseline.properties`                                                                                                                                                                                                                                                               |
| the per-request p99 series rises on the write lifecycle across the three recorded runs (poll 12→24→56, start 10→18→45) while the list read does not (15→24→14)                                                                                                      | `design.md` | `for c in c57e8b12 e181fd9a 20585efd; do git show "${c}:load-tests/src/gatling/resources/baseline.properties" \| grep -E "^(FetchShowcases\|PollShowcase\|StartShowcase)\.p99Ms"; done`                                                                                                                                                       |
| the 2026-10-08 record attributes `StartShowcase`'s p99 to "tail noise … within the aggregate p99 range of the prior runs"                                                                                                                                           | `design.md` | `grep -n "tail noise" docs/load-tests/2026-10-08.md`                                                                                                                                                                                                                                                                                          |
| the drift comparison passes a rise up to the tolerance, floored at 5/10/20 ms                                                                                                                                                                                       | `design.md` | `grep -n "FLOOR_MEAN_MS\|FLOOR_P95_MS\|FLOOR_P99_MS" load-tests/src/main/java/showcase/loadtests/BaselineDrift.java`                                                                                                                                                                                                                          |
| the profiles' thresholds are `max(floor, factor x baseline)` with factor 5 and floors 50/100/200 ms, so the factor governs poll (5x56=280) and start (5x45=225) but not the flat requests                                                                           | `design.md` | `grep -n "Math.max\|DEFAULT_FLOOR_MEAN_MS\|DEFAULT_FLOOR_P95_MS\|DEFAULT_FLOOR_P99_MS\|DEFAULT_FACTOR" load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java load-tests/src/gatling/java/showcase/loadtests/BaselineStats.java`                                                                                              |
| a run whose operating point lands outside the matching band is not comparable and overwrites the committed reference                                                                                                                                                | `design.md` | `grep -n "writeReference\|comparable" load-tests/src/main/java/showcase/loadtests/BaselineDrift.java`                                                                                                                                                                                                                                         |
| the request→route mapping is `FetchShowcases`→GET `/showcases`, `ScheduleShowcase`→POST `/showcases`, `StartShowcase`→PUT `/showcases/{showcaseId}/start`, `FinishShowcase`→PUT `/showcases/{showcaseId}/finish`, `RemoveShowcase`→DELETE `/showcases/{showcaseId}` | `tasks.md`  | `grep -nE "@(Get\|Post\|Put\|Delete)Mapping" showcase-api-gateway/src/main/java/showcase/api/rest/ShowcaseRestController.java`; `grep -nE "\.(get\|post\|put\|delete)\(" load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java`                                                                                              |
| two routes are shared, so the server-side series cannot separate those request names: `FetchShowcase`/`PollShowcase` on GET `/showcases/{showcaseId}`, and `ScheduleShowcase`/`SeedShowcase` on POST `/showcases`                                                   | `design.md` | `grep -n 'http("SeedShowcase")\|http("PollShowcase")' load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java`; `curl -s 'http://127.0.0.1:19090/api/v1/label/uri/values' --get --data-urlencode 'match[]=http_server_requests_seconds_count{job="axon-showcase-api-gateway"}'`                                                |
| the deployment exports and retains the gateway's per-route HTTP series, the command-service's `commandBus_*` timers, and the JVM's `jvm_gc_pause_seconds`/`process_cpu_usage`                                                                                       | `design.md` | `curl -s 'http://127.0.0.1:19090/api/v1/label/__name__/values' --get --data-urlencode 'match[]={namespace="axon-showcase"}' \| tr ',' '\n' \| grep -cE 'http_server_requests_seconds\|commandBus_successTimer_seconds\|jvm_gc_pause_seconds\|process_cpu_usage'` (against `kubectl port-forward -n monitoring svc/kps-prometheus 19090:9090`) |
| the measured stack is the one the reference was recorded on (app pods created 2026-10-08T02:24Z; `recordedAt=2026-10-08T03:10:49Z`), and `main` has moved since                                                                                                     | `design.md` | `grep -n "recordedAt" load-tests/src/gatling/resources/baseline.properties`; `kubectl get pods -n axon-showcase -o custom-columns=CREATED:.metadata.creationTimestamp`; `git log --since=2026-10-08 --oneline -- showcase-query-service showcase-api-gateway showcase-projection-service`                                                     |

## Decisions

**The attribution is layered, and the process-wide control carries load.** A client-side p99 is end-to-end, so the
measurement pairs it with the gateway's own server-side time for the same route (separating the client and host from the
service) and the command-service's command bus (separating the gateway from the write service). The process-wide
hypothesis is tested with `jvm_gc_pause_seconds` and `process_cpu_usage` over the same window — a loaded control, unlike
a no-backend route, which the profile drives no traffic to.

**The retained series are the source, not new in-run instrumentation.** Prometheus already scrapes and keeps these
series, so the plateau's window is queried by timestamp after the run and nothing is added to the load-test tooling —
the repository's own direction is to use the tooling rather than extend it.

**The split is per route, not per request name, and that limit is stated.** Two routes are shared, so the record names
which figures are blended rather than implying a separation the series cannot make. The per-request _history_ comes from
git instead, where each committed reference carries its own names.

**The run writes the reference unless a figure regresses, so the committed reference is copied aside and restored.** The
drift comparison writes `baseline.properties` whenever the measurement is not empty and it is either not comparable, not
regressed, or a refresh is intended — a rate-matched run inside the band therefore replaces the recorded reference as
its _normal_ outcome, and a run outside the band writes it too. The reference is copied aside before the run and
restored after it, so attributing the tail cannot silently re-record a baseline against the figures under study.

## Risks / Trade-offs

- The measurement runs on the same shared host that is a suspect, so a quieter second environment is unavailable. The
  loaded process-wide control substitutes for it, and the substitution's limit is stated in the record.
- A p99 rests on the last 1% of a request's own samples, and the writes are a quarter of the load across four routes, so
  a single run cannot separate a trend from a step: the record states what the evidence carries.
- The measured stack predates `main` by the post-2026-10-08 commits listed in the table, so a "real degradation"
  conclusion describes the recorded binary, not today's tree.
