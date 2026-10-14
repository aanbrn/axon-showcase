# Tasks

## 0. The pre-write derivation pass (this change carries its own claims table)

- [x] 0.1 Run the pre-write derivation pass and record its output as the claims table in `design.md`.

## 1. The layered measurement

- [x] 1.1 Record the measured stack's provenance (the app pods' creation time, the images, the node's allocatable
      resources) and what would invalidate a comparison against the reference; note the dependency drift from `main` as
      a stated limit.
- [x] 1.2 Run one rate-matched baseline measurement against the local target —
      `CALIBRATE_MAX_RATE=200 CALIBRATE_RATE=200 ./scripts/load-test-baseline.sh`, with `REFRESH_BASELINE` unset — so
      the operating point lands within the matching band of the recorded 127 units/s; record the plateau's start and end
      timestamps, and copy the committed reference aside first and restore it afterwards — a non-regressed in-band run
      writes it, as does a run outside the band, so the reference under study is never left replaced.
- [x] 1.3 Query the retained series over the run's measured window, using the mapping `FetchShowcases`→GET `/showcases`,
      `ScheduleShowcase`→POST `/showcases`, `StartShowcase`→PUT `/showcases/{showcaseId}/start`, `FinishShowcase`→PUT
      `/showcases/{showcaseId}/finish`, `RemoveShowcase`→DELETE `/showcases/{showcaseId}`, and noting that
      `FetchShowcase`/`PollShowcase` share GET `/showcases/{showcaseId}` and `ScheduleShowcase`/`SeedShowcase` share
      POST `/showcases`, so those server-side figures are blended: - the gateway's server-side p99 per write route, from
      `http_server_requests_seconds_bucket`; - the command-service's `commandBus_successTimer_seconds` p99; - the
      gateway's and command-service's `jvm_gc_pause_seconds` and `process_cpu_usage` over the same window.
- [x] 1.4 Compare the layers against the plateau's client-side per-request figures and state which layer the
      write-lifecycle tail lives in — and whether the evidence supports the pipeline or the environment — with the
      confidence the figures warrant.

## 2. The explanation and its consequence

- [x] 2.1 Annotate the run's record under `docs/load-tests/` with the attribution, the layer evidence, the per-request
      series that corrects the "tail noise" reading, and what the measurement does not establish.
- [x] 2.2 Decide the durable consequence and record the decision: the environment dominates this figure, so the clause
      owed to the drift requirement is parked in `docs/ideas.md` as its own change's subject (this one owes no delta).
- [x] 2.3 Remove the parked idea entry this change answers, and **draft** the finding for issue #430 in the change dir
      now, so the implementation review (4.3) passes over the draft as the outward-facing artifact it is. Publishing it
      rides 4.3's clean verdict (task 4.4).

## 3. Docs the change owns

- [x] 3.1 Sweep `AGENTS.md`, `README.md`, the `showcase/quality/load-tests` spec and `docs/` for claims the finding
      falsifies. Outcome: nothing to correct — the spec specifies the check's behavior and says nothing about the tail's
      cause; `AGENTS.md` names only the mechanism and its floors; `docs/load-tests/2026-10-08.md` is a historical record
      left as recorded and superseded by the new one's attribution. No spec text needs an edit (expected, `skip_specs`).

## 4. Verification

- [x] 4.1 Re-run the claims-table rows with their sources and confirm each reproduces (all eleven re-run at round 5 and
      again before the manual pass; the measured figures are this change's own output and are carried by the record).
- [x] 4.2 Run `./gradlew spotlessApply` then `spotlessCheck`, `openspec validate --all`, and the manual 120-character
      check over the changed non-formatter files.
- [x] 4.3 Run a `review-quick` pass over the implementation, passing the prior round's instances.
- [x] 4.4 Publish the drafted finding (posted: issues/430#issuecomment-6164612280; the comment is left to the owner to
      close the issue) on issue #430, once the review at 4.3 is clean.
- [x] 4.5 Run the per-unit `lesson-capture` and record the applied net `AGENTS.md` delta (**+8 lines**: the
      window-scoping rule merged into the measurement-attribution gotcha, a fifth pre-write claim class for a change's
      own derived figures, and the per-request-series source on the `baselineTrend` note; no rule retired).
- [x] 4.6 Request the user's manual review pass.
