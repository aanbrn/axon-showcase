# Tasks

The unit's record is `docs/units/bump-buildpacks-2026-11-09/unit.md`.

## 1. Pre-write derivation

- [x] 1.1 Run the `review-gate` skill's first-stage derivation pass: seed the claims table in `design.md` with
      `scripts/claim-scan.py`, resolve each candidate into a row or the recorded dismissal, and re-run every command a
      row names after the last edit. **Done**: `python3 scripts/claim-scan.py --base origin/main` seeded the table;
      every candidate is a row or the recorded dismissal; the post-bump rows were added and every command a row names
      re-run after the last edit.

## 2. The pins

- [x] 2.1 Re-resolve the rows with the check's own run (`./gradlew buildpackUpdates`) rather than the weekly tracker
      issue, and read the whole report rather than a slice of it. **Done**: five rows, read from the whole report:
      `paketo-builder-jammy-base` `0.4.675 -> 0.4.681`, `paketo-builder-noble-java-tiny` `0.0.205 -> 0.0.211`,
      `paketo-health-checker` `2.14.1 -> 2.14.2`, `paketo-nginx` `1.3.4 -> 1.3.5`, `paketo-procfile` `5.15.6 -> 5.15.7`.
- [x] 2.2 Establish each builder/buildpack pair before choosing the pins: `pack builder inspect` on
      `paketobuildpacks/builder-jammy-base:0.4.681` and on `paketobuildpacks/builder-noble-java-tiny:0.0.211`, read in
      full; confirm the health-checker image tag exists, since no builder bundles it. **Done**: jammy `0.4.681` lists
      nginx `1.3.5` and procfile `5.15.7`; noble `0.0.211` lists java `22.8.0`, spring-boot `5.40.5`, and
      bellsoft-liberica `11.10.5` over run image `ubuntu-noble-run-tiny:0.0.157`, with no health-checker entry;
      `paketobuildpacks/health-checker:2.14.2` resolves an OCI index.
- [x] 2.3 Set the matched sets in `gradle/libs.versions.toml`: jammy builder `0.4.681`, nginx `1.3.5`, procfile
      `5.15.7`; noble builder `0.0.211`, health-checker `2.14.2`. **Done**: the catalog carries the two matched sets
      plus health-checker `2.14.2`.
- [x] 2.4 Move the documented pins in the `deployment-operations` pack. Sweep the live docs for the old strings and
      confirm no site outside the pack states them. **Done**: the pack's four pin sentences moved; the base `git grep`
      found no site outside it; `verifyDocumentedPins` is green in 3.2.

## 3. Verification

- [x] 3.1 Re-run the check that produced the finding: `./gradlew buildpackUpdates` reports no row. **Done**:
      `No buildpack updates available.`
- [x] 3.2 `./gradlew verifyDocumentedPins` passes. **Done**: BUILD SUCCESSFUL.
- [x] 3.3 Establish that amd64 emulation works before reading any image failure as a pin defect, then build the web UI
      image and run it: `GET /`, the rendered `config.js`, nginx's ELF `e_machine` from the buildpack layer, and the
      build metadata label's nginx and procfile versions. **Done**: the emulation probe prints `x86_64` on daemon
      `29.5.2`; `:showcase-web-ui:dockerBuildImage` succeeded with `aanbrn/axon-showcase-web-ui:0.2.0-SNAPSHOT`; the run
      answered `GET /` → 200, rendered `window.__API_BASE_URL__ = "http://localhost:8080";`, read nginx's `e_machine` as
      `3e 00`, and its metadata names nginx `1.3.5` and procfile `5.15.7`.
- [x] 3.4 Build a JVM service image for `linux/amd64` with the new noble builder and run it: the log shows the pinned
      builder, the platform, and the application starting, and the bumped health-checker succeeds against the running
      application — run `/workspace/health-check` with `THC_PATH=/actuator/health`, as `docker-compose.yml` does, and
      require a zero exit, since startup alone does not establish that the probe performs its job. **Done**:
      `:showcase-api-gateway:bootBuildImage --imagePlatform=linux/amd64` succeeded (`linux/amd64`); the run logged
      `Started ShowcaseApiApplication`; `/workspace/health-check` with `THC_PATH=/actuator/health` exited `0` against
      the running application; the metadata names spring-boot `5.40.5`, bellsoft-liberica `11.10.5`, and health-checker
      `2.14.2`.
- [x] 3.5 Run `./gradlew spotlessApply`/`spotlessCheck`, `openspec validate --all`, and the manual 120-character check
      over the change's whole changed set, after the last edit to a gated file. **Done**:
      `spotlessApply`/`spotlessCheck` green and `openspec validate --all` 27 passed; the 120-character check over the
      changed set flags the unit's `sessions.tsv` rows (one line by contract), the design's claims-table rows
      (formatter-owned markdown), and pre-existing catalog library declarations.

## 4. Review gate

- [x] 4.1 Run the `review-quick` loop over the proposal and the implementation, repeating until it reports no new
      observations, recording each round in `docs/units/bump-buildpacks-2026-11-09/rounds.tsv`, its instances in
      `findings/round-<N>.md`, its hand-over in `review-handover.md`, and the reviewer's session in `sessions.tsv`; run
      both ledger checks and `--expect-clean` as the gate requires. **Done**: eight rounds, ten findings, all fixed and
      re-verified; rounds 3, 6, and 8 clean; the ledger, session, and `--expect-clean` checks pass.
- [x] 4.2 Derive the deep-review routing with `scripts/review-routing.py`, record its outcome and signals in
      `deep-review.md`, and run the routed pass when it is `REQUIRED` (or a `RECOMMENDED` outcome the owner resolves to
      run). **Done**: `RECOMMENDED` on `[repeated-class] 3 rows repeat a prior class`; the owner resolved to run, the
      routed pass ran, and its findings and statuses are in `deep-review.md`.
- [x] 4.3 Ask the owner for the manual review pass, once 4.1 is clean and 4.2 is recorded. **Done**: asked at the end of
      the implementation turn; nothing is committed, pushed, archived, or merged before the answer.

## 5. Process defects

- [x] 5.1 Write `docs/units/bump-buildpacks-2026-11-09/process-defects.md`, recording what the unit observed or stating
      that it observed none. **Done**: two records — the hand-over readiness line falsified by its own append, and the
      task 2.1 Done note citing a comparison the artifacts do not carry.

## Workflow follow-up

- Archive the change on the owner's delivery request, after the implementation approval; its archive commit rides this
  PR.
- The tracker issue's five rows are cleared by this bump; the next scheduled check run is what reports the issue's new
  body.
