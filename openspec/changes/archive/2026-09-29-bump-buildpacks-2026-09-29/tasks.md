# Tasks

## 1. Apply the bump

- [x] 1.1 Bump the three Paketo pins in `gradle/libs.versions.toml` together: `paketo-builder-jammy-base` 0.4.649,
      `paketo-nginx` 1.2.2, `paketo-procfile` 5.15.1. Verify by reading the catalog.
- [x] 1.2 Refresh the `AGENTS.md` sites that spell out the pins: the buildpack list (`nginx@1.2.2`, `procfile@5.15.1`),
      the builder pin (`builder-jammy-base:0.4.649`), and the alias-tag semver example (`1.2.2`). Verify by reading the
      sites.

## 2. Verification

- [x] 2.1 Build the web UI image (`./gradlew :showcase-web-ui:dockerBuildImage`) and **run** it: `docker run` the image,
      `GET /` and confirm the served page, and check the `nginx` binary's architecture inside the container — a
      buildpack pin can build cleanly (and carry a correct metadata label) while shipping the wrong-architecture binary.
      Done: `:showcase-web-ui:dockerBuildImage` built `aanbrn/axon-showcase-web-ui:0.1.0-SNAPSHOT` (linux/amd64; builder
      0.4.649, nginx 1.2.2, procfile 5.15.1 detected); `docker run` served `HTTP 200` with `<title>Showcase</title>`,
      and the container's nginx binary is `EM_X86_64` (`7f 45 4c 46 … 03 00 3e 00`), matching the amd64 image (an
      aarch64 binary would be `b7 00`).
- [x] 2.2 Run `./gradlew buildpackUpdates` and confirm it reports no updates (the check that triggered the bump is now
      clean). Done: the report reads "No buildpack updates available." against the bumped pins.
- [x] 2.3 Run the implementation `review-quick` loop over the diff; fix its findings and re-run until it reports nothing
      new.
- [x] 2.4 Run the per-unit `lesson-capture` over this change and apply its durable proposals, recording the applied net
      `AGENTS.md` delta on this change's record. Done: one addition to the update-check bullet (re-run the check after
      bumping), +3 lines.
- [x] 2.5 Run `./gradlew spotlessApply` after the last edit and confirm `spotlessCheck` passes; run the manual
      120-character check over the lines this change introduces in `gradle/libs.versions.toml` and the change dir's
      `.openspec.yaml`.
- [x] 2.6 Request the user's manual review pass — the step before committing.
