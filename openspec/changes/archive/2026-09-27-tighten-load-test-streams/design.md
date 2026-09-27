# Design

## Context

See `proposal.md` — Why. Facts established against the sources rather than memory:

- `CoreDsl.stressPeakUsers(int).during(Duration)` is `HeavisideOpenInjection(users, duration)` (`gatling-core` 3.15.1,
  `OpenInjectionSupport.scala`), which spreads that many arrivals over the duration with an erf-shaped profile — a
  **count**, not a rate. The parked reading is therefore right, and worse than it sounds: at `kneeRate` 200 and read
  share 0.75 the spike injects 225 arrivals over 2 minutes (~2/s) instead of ~225 read requests per second.
- The SSE DSL re-arms checks on an open connection: `sse("…").setCheck().await(D).on(checkMessage(…))`
  (`SseSetCheckActionBuilder`), alongside `.get(…).await(…).on(…)` and `.close()`. A re-armed check evaluates only
  messages arriving after it is armed, so the first event cannot satisfy it.
- The gateway's keep-alive is an SSE **comment** — `ServerSentEvent.builder().comment("keep-alive")` in
  `ShowcaseEventStreamController` — so it is not a message a check matches. Only a real showcase event can satisfy a
  further-delivery check, and only a profile with a sustained write stream produces those.
- A check failure is logged under the **check's** name (`showcaseEvent`), not the request's (`ShowcaseEvents`), so it
  neither marks the connection `KO` nor fails a run whose assertions name _requests_: the sustained profiles assert per
  request name, so the check's row is invisible to them. Verified in a quiet run — `showcaseEvent` recorded 1 KO
  (`Check showcaseEvent timeout`) while `ShowcaseEvents` stayed 0 KO. `smoke`'s `global()` assertion does aggregate the
  check's row (verified: a stalled smoke run failed `Global: count of failed events … actual : 1.0`), so the gap is the
  per-name profiles — and the assertion is stated explicitly for every asserting profile, `smoke` included, so the gate
  does not depend on which aggregation a profile happens to use.
- The re-armed check discriminates: it times out (KO) when no live event arrives after it is armed (confirmed in a quiet
  run), and a replay-buffered event satisfies only the connect check, so it is not satisfied by history.
- The read stream guards an empty list deliberately (`jsonPath("$[0].showcaseId").optional()` plus a `contains` test),
  because a detail fetch for a nonexistent showcase would 404 and, under `exitBlockOnFail`, kill the stream.

## Goals / Non-Goals

**Goals:**

- Make the `spike` profile inject the `1.5×` knee-rate burst its name and spec claim.
- Fail an asserted run whose SSE stream stalls after its first event.
- Exercise the detail endpoint on a cold target instead of skipping it.

**Non-Goals:**

- No change to the other profiles' curves, to the wrapper, or to the knee derivation.
- No new observation of the keep-alive comment (the check is event-based, as the SSE DSL's checks are).
- No change to the write stream's lifecycle or to the ratio, and no change to the assertion-free profiles' policy.

## Decisions

- **D1 — The spike injects a rate.** `constantUsersPerSec(1.5 × kneeRate × share).during(SPIKE_UP)` then
  `rampUsersPerSec(1.5 × kneeRate × share).to(0).during(SPIKE_DOWN)`. A constant rate is the burst (an instantaneous
  jump held for the ramp), and the ramp down mirrors the other profiles. The alternative — keeping a count and computing
  it as rate × duration — would make the burst's shape depend on the duration and still differ from every other
  profile's rate vocabulary; the spec already speaks of the profiles as multiples of the configured knee-rate.
- **D2 — The SSE check is re-armed mid-hold, and its failure is asserted where the profile asserts anything.** After the
  connection's first event, the scenario pauses half the hold, re-arms with
  `setCheck().await(SSE_AWAIT).on(checkMessage(…))`, pauses the remainder, and closes. It is scoped to a profile whose
  write-lifecycle stream runs for the hold — everything but `smoke`, and only with a non-zero write share, since
  `ratio: 1` makes the write stream `constantUsersPerSec(0)`, which Gatling reduces to `NothingFor` — because the
  keep-alive cannot satisfy an event check and `smoke`'s three-user burst is followed by a quiet hold, whose quiet-hold
  scenario stays `smoke`'s. Mid-hold rather than at the end, so a stream that stalls early fails rather than passing on
  one late event. Because a `KO` alone does not fail a run whose assertions name requests (Context), every profile that
  carries assertions also asserts `details("showcaseEvent").failedRequests().count().is(0L)` — the check's own name,
  under which its failures are logged — covering the connect check everywhere (`smoke` included, whose SSE has no
  re-armed check; its `global()` assertion would aggregate the row too, and the explicit one keeps the gate independent
  of the aggregation) and the re-armed one where it is armed; `calibrate`, `spike`, and `breakpoint` carry none by
  design, so there the check surfaces in the report only.
- **D3 — The read stream seeds one showcase on a cold target.** When the list yields no `showcaseId` and the run has not
  seeded yet (a shared one-shot flag), the read chain issues `SeedShowcase` (`POST /showcases`, expecting `201`, saving
  the id) and then fetches its detail for the configured share, so the detail path is exercised. The seed uses the DSL —
  the alternative, a `before()` hook with Java's `HttpClient`, would need ad-hoc JSON parsing and a plain-HTTP error
  path outside the DSL's check/status semantics. The seed request is outside `REQUEST_NAMES` (like the SSE connection),
  so it changes no pass assertion (the SSE assertion is D2's); the seeded showcase is not removed — it is the run's
  precondition, and one per cold run is negligible against a write stream that removes its own.
- **D4 — Four requirements are modified, none added.** Each behavior is described by an existing requirement
  (`Pass assertions`, `read and write streams`, `SSE event stream`, `knee-relative injection profiles`), so each takes a
  `MODIFIED` delta carrying its existing scenarios in the main spec's order. No new capability or requirement is needed.

## Risks / Trade-offs

- **A further-event assertion can flake if writes pause during the hold.** → It applies only to profiles whose write
  stream runs for the hold, only with a non-zero write share, uses the same 30-second await the connect check uses, and
  leaves `smoke`'s quiet hold alone.
- **The seed's showcase is not immediately queryable.** → The read stream refetches the list every iteration, so once
  the projection lands the detail fetch returns `200`; the first fetch may still be a `404`, which the existing check
  tolerates. Verification counts the `200`s, not just the issued requests.
- **The seed writes during a read-only run.** → Once per cold run, on the path that would otherwise skip the detail
  fetch; `ratio: 1` on a cold target is exactly the case it exists for.
- **The seeded showcase persists.** → One per cold target; the requirement text records it rather than hiding it.
- **`smoke`'s SSE coverage stays single-event.** → Deliberate: it has no sustained writes to assert against, and its
  quiet-hold scenario and global assertion are unchanged.

## Verification

- **spike**: `-Pprofile=spike -PkneeRate=20` (the profile's 3-minute curve is fixed) — the burst's request rate is the
  intended order of magnitude (~22/s against the count-based injection's ~0.2/s), with zero failed requests, since the
  profile carries no assertions.
- **SSE pass**: `-Pprofile=baseline -Pduration=PT2M -Prate=20 -PsseConnections=2` (the only asserted profile whose run
  length is configurable) — the re-armed check passes (`showcaseEvent` all OK) and the run exits 0.
- **SSE positive control**: the same run with the re-armed matcher temporarily changed to one that cannot match (writes
  still flowing, so nothing else fails) — `showcaseEvent` records `Check showcaseEvent timeout`, its assertion fails,
  and the run exits non-zero; restore the matcher afterwards.
- **cold start**: empty the target (`GET /showcases`, then `DELETE /showcases/{id}` for each), run
  `-Pprofile=calibrate -Pduration=PT1M -Prate=20 -Pratio=1`, and confirm the run issues `SeedShowcase` once and
  `FetchShowcase` for the configured share, with `200`s among them. An assertion-free profile is required: with
  `ratio: 1` the write request names have no stats, and Gatling's per-name assertions fail on that
  (`Could not find stats matching assertion path`), so `baseline` cannot be used for this run.
