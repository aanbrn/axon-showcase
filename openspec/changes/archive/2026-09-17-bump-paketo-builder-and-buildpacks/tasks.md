## 1. Bump the coordinates

- [x] 1.1 Bump `paketo-nginx` to `1.2.1`, `paketo-procfile` to `5.15.0` and `paketo-builder-jammy-base` to `0.4.642` in
      `gradle/libs.versions.toml`. The open buildpack-updates issue (#211) named only the builder, at the already-stale
      `0.4.641`; `./gradlew buildpackUpdates` and Docker Hub's tag API both resolve the three above as current.
- [x] 1.2 Update the versions `AGENTS.md` quotes — both buildpack versions and the builder version in the Docker Images
      section, and the alias-tag example (`1.2` and `5.15` for `1.2.1` and `5.15.0`) in the buildpack-CNB-id gotcha.

## 2. Verify

- [x] 2.1 Rebuild the web UI image with the new pins: `./gradlew :showcase-web-ui:dockerBuildImage` succeeds
      (`BUILD SUCCESSFUL`, 2m 18s).
- [x] 2.2 Read the buildpacks' versions back out of the built image rather than trusting the build log: its
      `io.buildpacks.build.metadata` label reports `paketo-buildpacks/nginx 1.2.1` and
      `paketo-buildpacks/procfile 5.15.0`. The label carries no builder — `0.4.642` is confirmed instead by the build
      log (it pulls `paketobuildpacks/builder-jammy-base:0.4.642`) and by `frontend-conventions` wiring the catalog pin.
- [x] 2.3 Run `./gradlew buildpackUpdates` — the report is "No buildpack updates available."
- [x] 2.4 Run `openspec validate --all` — passes.

## 3. Context for the earlier failures

- [x] 3.1 An earlier round of local `dockerBuildImage` failures during this investigation — a missing buildpackage
      label, an analyzer `panic: could not parse '0.12' as version`, `flate: closed writer`, and a Go GC fault — came
      from the host, not the pins: colima had fallen back to QEMU for amd64 because the macOS 27 upgrade removed
      Rosetta. With Rosetta reinstalled the same pins build, and the nginx digest that "could not be found" earlier is
      the identical digest this successful build used. Recorded because a future reader meets those failures in the PR
      history.
