# Tasks

## 1. The paired bump

- [x] 1.1 Inspect the new builder (`pack builder inspect paketobuildpacks/builder-jammy-base:0.4.662`) and take the
      buildpack pins from **its** bundle list: `paketo-procfile` `5.15.3`→`5.15.4`, with `paketo-nginx` held at `1.3.0`
      (the builder's version, not the report's `1.3.2`).
- [x] 1.2 Set `paketo-builder-jammy-base` `0.4.659`→`0.4.662` in `gradle/libs.versions.toml`.
- [x] 1.3 Move the `AGENTS.md` buildpack-pin gotcha's stated pins (`procfile@5.15.4`, `builder-jammy-base:0.4.662`;
      `nginx@1.3.0` unchanged), which `verifyDocumentedPins` asserts against the catalog.

## 2. Verification

- [x] 2.1 Run `./gradlew buildpackUpdates` and `./gradlew verifyDocumentedPins`; confirm the builder and procfile rows
      are gone from the report (the `paketo-nginx` row remains by design), and the documented pins match. (Both green:
      the report now carries only `paketo-nginx: 1.3.0 -> 1.3.2`, and `verifyDocumentedPins` matched all three pins.)
- [x] 2.2 **Build and run the image**: `./gradlew :showcase-web-ui:dockerBuildImage`, then run the built image, confirm
      `GET /` is served, and read nginx's architecture out of the buildpack's own layer
      (`/layers/paketo-buildpacks_nginx/nginx/sbin/nginx`, decoding the ELF `e_machine`) — a successful build with a
      wrong-architecture binary is the trap this check exists for. Record the result. (Built
      `aanbrn/axon-showcase-web-ui:0.2.0-SNAPSHOT` — the tag the declared version gives — with the new builder;
      `docker run` serves `GET /` -> **200**, the app's title; nginx's ELF `e_machine` read from
      `/layers/paketo-buildpacks_nginx/nginx/sbin/nginx` is **0x3e (x86-64)**; and the image's
      `io.buildpacks.build.metadata` label names `nginx@1.3.0` and `procfile@5.15.4`, the pins — the pinned procfile
      runs (`[detector] pass: paketo-buildpacks/procfile@5.15.4` in a `pack build` with the task's own args), with no
      buildpack fetched from the registry.)
- [x] 2.3 Run `./gradlew spotlessApply` then `spotlessCheck` and `openspec validate --all`; run the manual 120-character
      check over the lines this change introduces; confirm clean.
- [x] 2.4 Run a `review-quick` pass over the proposal and the implementation, repeating until clean: four rounds (3,
      clean, wording, clean). Round 1's `contradiction` was **refuted** — it rested on my own task 2.2 observation,
      which had inspected the wrong image (the `head -1` slip above), and the `AGENTS.md` clause it questioned is true —
      so no rule was softened; its `claim-unreproducible` (the proposal over-crediting `infra-image-versions` with the
      pairing rule) was fixed. Round 3's `wording` was against the capture's own clause, whose placement split the
      bullet's paragraph; repaired, and the stray `A` the lift carried across the boundary fixed on both sides.

## 3. Capture

- [x] 3.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta: **+6 content lines** (the diff reads +10/-2, the rest being the paragraph Prettier re-wrapped
      around the clause), one clause merged into the `gh pr list` bullet's existing `head -1` mode — a slice of a list
      of _objects_ selects a subject, not only a shorter list, so name a built image by the tag the declared version
      gives, never by list position (the multi-tag / per-tag-metadata mechanism, derived and reproduced). It judged the
      `[detector] pass:` probe, the held-nginx row, and the refused `contradiction` already covered by existing rules,
      and proposed no retirements.
- [ ] 3.2 Ask the owner for the manual review pass, after 2.4 is clean and 3.1 is done.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass as a task (every unit owes it). It ran: the claims table is in
      `design.md`, and its rows are derived — the rows from this change's own check run, the bundled versions from
      `pack builder inspect` against both builders, and the pairing mechanism from the `AGENTS.md` gotcha.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The `paketo-nginx` row stays in the report by design; its clearance is a builder that bundles `1.3.2`.
