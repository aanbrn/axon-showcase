# Tasks

## 1. Spike injection

- [x] 1.1 Change the `spike` profile in `ShowcaseSimulation` to inject `1.5 ×` the knee-rate as a rate
      (`constantUsersPerSec(...).during(SPIKE_UP)` then `rampUsersPerSec(...).to(0).during(SPIKE_DOWN)`), replacing the
      `stressPeakUsers` count. Verify with `-Pprofile=spike -PkneeRate=20` that the burst's request rate is the intended
      order of magnitude (~22/s against the count-based injection's ~0.2/s) with zero failed requests.

## 2. SSE delivery

- [x] 2.1 Re-arm the SSE check mid-hold for a profile whose write-lifecycle stream runs for the hold (all but `smoke`,
      and only with a non-zero write share), checking for a further showcase event with the same 30-second await the
      connect check uses; leave `smoke`'s quiet hold as it is. Verify with `-Pprofile=baseline -Pduration=PT2M` that the
      check passes and the run exits 0.
- [x] 2.2 Assert `details("showcaseEvent").failedRequests().count().is(0L)` — the check's own name, under which its
      failures are logged — for every profile that carries assertions (`average`, `stress`, `soak`, `baseline`, and
      `smoke`, whose SSE has only the connect check). Verify the positive control: repeat the 2.1 run with the re-armed
      matcher temporarily changed so it cannot match, confirm `showcaseEvent` records `Check showcaseEvent timeout` and
      the run fails (non-zero), then restore the matcher.

## 3. Cold-start read path

- [x] 3.1 Make the read stream seed one showcase when the list is empty (a one-shot `SeedShowcase`, outside
      `REQUEST_NAMES`), then fetch its detail for the configured share. Verify against a target emptied with
      `GET /showcases` + `DELETE /showcases/{id}` that a `-Pprofile=calibrate -Pduration=PT1M -Prate=20 -Pratio=1` run
      issues `SeedShowcase` once, issues `FetchShowcase` for the share, and returns `200`s among them (an assertion-free
      profile, because `ratio: 1` leaves the write request names without stats and the per-name assertions fail).

## 4. Docs

- [x] 4.1 Remove the implemented "Minor load-test refinements" entry from `docs/ideas.md`. Sweep `AGENTS.md` and
      `README.md` for any description of the old `spike`-as-users semantics and correct it if present.

## 5. Verification

- [x] 5.1 `./gradlew spotlessApply` then `spotlessCheck` pass; `openspec validate --changes` passes.
- [x] 5.2 Run the three verification runs above against the local cluster and record their outcomes in the change's
      report (they are live-cluster checks, so none may be left as an unticked task at archive).
