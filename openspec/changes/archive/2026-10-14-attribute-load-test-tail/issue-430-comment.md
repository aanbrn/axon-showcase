# Draft: issue #430 comment

Posted to #430 only once the implementation review is clean (task 4.4).

## What the measurement found

Measured the recorded operating point on the same stack the reference was recorded on
(`aanbrn/axon-showcase-*:0.2.0-SNAPSHOT`, app pods created 2026-10-08T02:24Z), rate-matched at `CALIBRATE_MAX_RATE=200`
— 127 vs 127 workload units/s, ~239 rps, 144,258 requests over a plateau running 07:44:56–07:54:56Z. Full evidence:
`docs/load-tests/2026-10-14.md` (ships with this change's pull request; not yet on `main`).

**1. The tail did not compound.** Every request whose p99 the 2026-10-08 refresh accepted improved or held:
`PollShowcase` 56 → 22 ms, `StartShowcase` 45 → 24, `ScheduleShowcase` 29 → 22, `RemoveShowcase` 30 → 29. The other
three moved by a millisecond or three: `FetchShowcase` 22 → 25, `FinishShowcase` 27 → 28, the list read 14 → 11. Across
the four recorded runs the write-lifecycle p99 series is poll 12/24/56/22 and start 10/18/45/24 — a spread around a
stable level, not a trend.

**2. About half of the figure is outside the services.** From the Prometheus series over the plateau window, the
gateway's own server-side p99 per route is roughly half the client-side p99, while the means differ by up to ~2 ms:

| route                    | server mean | server p99 | client mean | client p99 |
| ------------------------ | ----------- | ---------- | ----------- | ---------- |
| GET `/showcases`         | 1.2 ms      | 6.7 ms     | 2 ms        | 11 ms      |
| POST `/showcases`        | 4.8 ms      | 14.7 ms    | 7 ms        | 22 ms      |
| GET `/showcases/{id}`    | 3.4 ms      | 10.2 ms    | 3–5 ms      | 22–25 ms   |
| PUT `…/start`            | 3.7 ms      | 11.1 ms    | 5 ms        | 24 ms      |
| PUT `…/finish`           | 4.3 ms      | 13.0 ms    | 6 ms        | 28 ms      |
| DELETE `/showcases/{id}` | 3.9 ms      | 13.0 ms    | 6 ms        | 29 ms      |

The command-service's own handling sits below that (`commandBus_successTimer` p99 8.0 ms, mean 1.8 ms), and the busiest
service's CPU peaked at 5.3% of the node's cores — so the write service is not the source and the cluster is not
CPU-bound.

**3. What it does not establish.** The p99-scope gap lies outside the gateway's handler; the gateway's own transport
path (the metric is handler-scoped), the Gatling client's JVM (which shares this host), the local network, and the
ingress are all candidates, and none is measured here. Process-level stalls do exist — one command-service pod logged a
360 ms p99 GC pause over the plateau (20 ms on its sibling) — and the plateau's extreme tail is a handful of requests,
12 of its ~144,000 exceeding 500 ms against the client-side maximum of 654 ms, so the stalls and the slowest requests
are of the same order. One run cannot separate a scheduling tail from a pause tail.

## What it implies

The drift check compares this client-side figure, so about half of what it tracks is the client and host rather than the
pipeline, and its run-to-run spread is wide (12–56 ms on the poll request across four runs). Which figure it should
track instead — the server side, a lower percentile, or a confirmed regression before failing — is a change to the
`showcase/quality/load-tests` drift requirement; it is parked in `docs/ideas.md` for its own change. On this evidence
nothing in the pipeline needs fixing, which answers this issue.
