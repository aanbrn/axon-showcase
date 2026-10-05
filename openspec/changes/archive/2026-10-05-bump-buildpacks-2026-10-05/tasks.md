# Tasks

## 1. Apply the bump

- [x] 1.1 Confirm the new builder bundles the reported buildpack versions:
      `pack builder inspect paketobuildpacks/builder-jammy-base:0.4.653` must list `paketo-buildpacks/nginx@1.3.0` and
      `paketo-buildpacks/procfile@5.15.2` (the matched pair). Done: the inspect lists `nginx@1.3.0` and
      `procfile@5.15.2` (the old builder 0.4.649 bundled `nginx@1.2.2` / `procfile@5.15.1` — so all three pins move
      together).
- [x] 1.2 Bump the three Paketo pins in `gradle/libs.versions.toml` together: `paketo-builder-jammy-base` 0.4.653,
      `paketo-nginx` 1.3.0, `paketo-procfile` 5.15.2. Verify by reading the catalog. Done: the three `[versions]` edits.
- [x] 1.3 Refresh the `AGENTS.md` sites that spell out the pins: the buildpack list (`nginx@1.3.0`, `procfile@5.15.2`),
      the builder pin (`builder-jammy-base:0.4.653`), and the alias-tag semver example. Verify by reading the sites.
      Done: the buildpack list and builder pin refreshed; the alias-tag example now reads `1.3` / `1.3.0`, and the
      mismatched-pair example is made version-agnostic ("holding a buildpack one line behind the builder").

## 2. Verification

- [x] 2.1 Build the web UI image (`./gradlew :showcase-web-ui:dockerBuildImage`) and **run** it: `docker run` the image,
      `GET /` and confirm the served page, and check the `nginx` binary's architecture inside the container — a
      buildpack pin can build cleanly (and carry a correct metadata label) while shipping the wrong-architecture binary.
      Done: built `aanbrn/axon-showcase-web-ui:0.2.0-SNAPSHOT` (linux/amd64) with the `pack` command line carrying the
      builder `paketobuildpacks/builder-jammy-base:0.4.653` and the buildpacks `paketo-buildpacks/nginx@1.3.0` and
      `paketo-buildpacks/procfile@5.15.2`. Ran it: `HTTP 200`, `<title>Showcase</title>`; `uname -m` = `x86_64`; the
      running nginx binary's ELF header is `7f 45 4c 46 … 03 00 3e 00` — `e_machine` `3e00` = **EM_X86_64** (an aarch64
      slice would be `b7 00`), so the pair is matched.
- [x] 2.2 Run `./gradlew buildpackUpdates` and confirm it reports no updates (the check that triggered the bump is now
      clean). Done: the report reads "No buildpack updates available."
- [x] 2.3 Run the implementation `review-quick` loop over the diff; fix its findings and re-run until it reports nothing
      new. Rounds: 1 found a >120 inline-code line, an imprecise old-builder version claim, and a proposal
      under-description; 3 found the corrected Docker-29 claim's residue in the change's own `design.md` and at
      `AGENTS.md:700`; all fixed, round 4 clean.
- [x] 2.4 Run the per-unit `lesson-capture` over this change and apply its durable proposals; record the applied net
      `AGENTS.md` delta. Done: two captures applied — (1) the buildpack-pin gotcha gains "establish the matched pair
      before choosing the pin" (`pack builder inspect`); (2) the Docker-29 claim is corrected from "a Docker 29 daemon
      needs the `overlay2` driver" to "the store alone is not the trigger" (the build succeeded on Docker 29.5.2 +
      `overlayfs` locally), swept across `.github/workflows/deployment-smoke.yml` and `AGENTS.md:700`. Net `AGENTS.md`
      delta: +6 lines (the capture branched beyond `AGENTS.md` to correct the restatement).
- [x] 2.5 Run `./gradlew spotlessApply` after the last edit and confirm `spotlessCheck` passes; run the manual
      120-character check over the lines this change introduces in `gradle/libs.versions.toml`, `AGENTS.md`, the change
      dir's `.openspec.yaml`, and the change dir's markdown (Prettier cannot reflow inside an inline code span, so a
      long span there survives the gate). Done (final pass after the task ticks).
- [x] 2.6 Request the user's manual review pass — the step before committing. Done: the user approved the
      implementation.
