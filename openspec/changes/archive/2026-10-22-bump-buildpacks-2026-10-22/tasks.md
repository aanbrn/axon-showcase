# Tasks

## 1. The pins

- [x] 1.1 Re-resolve the three rows with the check's own run (`./gradlew buildpackUpdates`) rather than the weekly
      tracker issue. **Done**: builder `0.4.662 -> 0.4.667`, nginx `1.3.0 -> 1.3.3`, procfile `5.15.4 -> 5.15.5`.
- [x] 1.2 Establish the builder/buildpack pair before choosing the pins:
      `pack builder inspect paketobuildpacks/builder-jammy-base:0.4.667`. **Done**: the builder carries nginx `1.3.0`
      only and procfile `5.15.1`/`5.15.2`/`5.15.4`/`5.15.5`.
- [x] 1.3 Set the matched set in `gradle/libs.versions.toml`: builder `0.4.667`, procfile `5.15.5`, and nginx **held at
      `1.3.0`** because the builder does not carry `1.3.3`. **Done**: the catalog carries that set.
- [x] 1.4 Move the documented builder and procfile pins in the `deployment-operations` pack. **Done**:
      `./gradlew verifyDocumentedPins` passes; the documented nginx pin is unchanged because the pin is.
- [x] 1.5 Park the held row durably. **Done**: `docs/ideas.md` (2026-10-22) records `paketo-nginx 1.3.3`, why it is
      held, and its retrigger — the next pinned builder whose inspection lists it.

## 2. Verification

- [x] 2.1 Re-run the check that produced the finding. **Done**: `./gradlew buildpackUpdates` reports only the held
      `paketo-nginx: 1.3.0 -> 1.3.3` row.
- [x] 2.2 Establish that amd64 emulation works before reading any image failure as a pin defect. **Done**:
      `docker run --rm --platform linux/amd64 alpine uname -m` prints `x86_64`.
- [x] 2.3 Build the image with the new builder. **Done**: `./gradlew :showcase-web-ui:dockerBuildImage` BUILD SUCCESSFUL
      with `aanbrn/axon-showcase-web-ui:0.2.0-SNAPSHOT`.
- [x] 2.4 Run the built image and read the deployed signal. **Done**: `GET /` answers 200, `config.js` renders the
      configured API base URL, and the nginx binary's ELF `e_machine` read from
      `/layers/paketo-buildpacks_nginx/nginx/sbin/nginx` is `3e 00` (x86-64); the image's build metadata names
      `paketo-buildpacks/nginx 1.3.0` and `paketo-buildpacks/procfile 5.15.5`. The probe container was removed.
- [x] 2.5 Run `./gradlew spotlessApply`/`spotlessCheck` and `openspec validate --all`; run the manual 120-character
      check over the change dir. **Done**: all green.

## 3. Review, capture and the manual pass

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`, seeded by
      `scripts/claim-scan.py`, with every candidate resolved into a row or a class-level dismissal. **Done**: the table
      and the class-level dismissal are in `design.md`.
- [x] 3.2 Run a `review-quick` pass over the proposal and the implementation, appending each round's row to
      `docs/review-rounds.tsv` and running the ledger checkpoint after each append; repeat until a round reports nothing
      new. **Done**: each row was appended as its report arrived, with the checkpoint run after each append; the ledger
      carries the rounds and their outcomes, and the loop is clean.
- [x] 3.3 Derive the deep-review routing outcome, decide at or above the floor, and record the outcome and its signals
      here. **Done**: `NOT_NEEDED`.
- [x] 3.4 Run the `lesson-capture` subagent and record its applied net delta or its skip reason with the reason.
      **Done**: one replacement applied — `AGENTS.md`'s search-scope clause now also requires that a claims row keep the
      claim's own subject outside its exclusion, since the previous wording ("name the scope the claim covers") was
      satisfied by a pathspec that hid the archive the row reported on; `+4/-1` in `AGENTS.md`, no retirement. Its
      authoring-system recommendation — a gate for whitespace runs inside inline code spans, which no formatter or check
      reads — is parked in `docs/ideas.md` rather than built here, since it is a build-logic addition with its own tests
      and review.
- [x] 3.5 Ask the owner for the manual review pass; commit, push, archive and merge only after approval. **Done**: the
      owner approved on 2026-10-22 after the manual review request.
