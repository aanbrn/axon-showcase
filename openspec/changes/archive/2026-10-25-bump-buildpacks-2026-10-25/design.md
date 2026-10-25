# Design

## Context

A Paketo builder is not a version bump like a chart pin: the buildpacks are resolved against the builder's own set, and
a pin the builder does not carry is fetched from the registry — resolving to the wrong architecture slice, which builds
cleanly and then fails at run time. So the unit's first step is establishing the pair, not applying the report's rows.

The previous bump (`bump-buildpacks-2026-10-22`) took builder `0.4.667` and held `paketo-nginx` at `1.3.0`, because that
builder listed nginx `1.3.0` only; it parked the hold in `docs/ideas.md` with the revisit condition
"`pack builder inspect` on the pinned builder lists `nginx 1.3.3`".

`pack builder inspect paketobuildpacks/builder-jammy-base:0.4.669` lists `paketo-buildpacks/nginx` at **1.3.3** (one
entry, not a choice of versions) and `paketo-buildpacks/procfile` at **5.15.5**, over lifecycle `0.21.26` and the run
image `paketobuildpacks/run-jammy-base:latest`. So the matched set is builder `0.4.669`, nginx `1.3.3` and procfile
`5.15.5` (already pinned): both report rows are taken, and the parked hold's condition is met.

The image is also not verified by its build: a buildpack release can install a binary of the wrong architecture and
still report a successful build with a metadata label naming the pinned version. The verification runs the image and
reads the binary's ELF header from the buildpack's layer.

## Goals / Non-Goals

**Goals:**

- Move the builder and the nginx buildpack it bundles together, in one matched set.
- Retire the parked hold now that its stated condition holds.
- Verify the built image by running it and reading nginx's architecture from the layer.

**Non-Goals:**

- `paketo-procfile`: the builder bundles `5.15.5`, which is the pin, so no row is reported and none is owed.
- The run image (`paketobuildpacks/run-jammy-base:latest`), which is deliberately left floating for base-OS security.
- The JVM builder (`paketo-builder-noble-java-tiny`) and the health-checker buildpack: the check reports no update for
  either.

## Decisions

1. **The builder's own listing decides the pins.** `pack builder inspect` on the new builder is the authority; the
   update report's rows are candidates, and here both candidates are confirmed by it.
2. **Take both rows together, and retire the hold.** With the builder bundling `1.3.3`, pinning it no longer makes
   `pack` add a buildpackage from the registry, so the entry the previous bump parked is removed rather than annotated.
3. **Verify by running the image.** `docker run`, `GET /`, the rendered `config.js`, and the nginx binary's `e_machine`
   read from `/layers/paketo-buildpacks_nginx/nginx/sbin/nginx` (`3e 00` = x86-64), not from the runtime `PATH` and not
   from the metadata label.

## Risks / Trade-offs

- **A builder bump moves everything else the builder bundles.** No other image builds on `builder-jammy-base` (the JVM
  services use `builder-noble-java-tiny`), and the run image stays floating by design, so the blast radius is this image
  and its two bundled buildpack stages.
- **The image is built for the host's daemon and run under emulation on an arm64 machine.** amd64 emulation was checked
  first (`docker run --rm --platform linux/amd64 alpine uname -m` → `x86_64`, with colima's `rosetta: true`), because a
  host failure would mimic a pin defect.

## Verification

- `./gradlew buildpackUpdates` after the bump — no row remains.
- `./gradlew verifyDocumentedPins` — the pack's documented builder and buildpack pins match the catalog.
- `./gradlew :showcase-web-ui:dockerBuildImage` — the image builds with the new builder.
- The built image run: `GET /` answers, `config.js` renders the configured API base URL, and nginx's ELF header from the
  buildpack layer reads `3e 00`; the build metadata names `paketo-buildpacks/nginx 1.3.3` and
  `paketo-buildpacks/procfile 5.15.5`.
- `./gradlew spotlessApply`/`spotlessCheck`, `openspec validate --all`, and the manual 120-character check over the
  change's whole changed set.

## Claims

| Claim                                         | Source                                                                                                                                                                                                                                                                          |
| --------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The check reported two rows before the bump   | `./gradlew buildpackUpdates` → `paketo-builder-jammy-base: 0.4.667 -> 0.4.669`, `paketo-nginx: 1.3.0 -> 1.3.3`, and no procfile, JVM-builder or health-checker row                                                                                                              |
| The catalog pinned those versions at the base | `git show 837aaa52:gradle/libs.versions.toml` → `paketo-nginx = "1.3.0"`, `paketo-procfile = "5.15.5"`, `paketo-builder-jammy-base = "0.4.667"`                                                                                                                                 |
| The new builder bundles nginx 1.3.3, procfile | `pack builder inspect paketobuildpacks/builder-jammy-base:0.4.669`, 485 lines read in full: nginx `1.3.3` (the only nginx version it lists), procfile `5.15.5`                                                                                                                  |
| that version set, and the same run image      | the same output: lifecycle `0.21.26`, run image `paketobuildpacks/run-jammy-base:latest`                                                                                                                                                                                        |
| The previous builder lacked nginx 1.3.3       | `openspec/changes/archive/2026-10-22-bump-buildpacks-2026-10-22/tasks.md` task 1.2 — the builder "carries nginx `1.3.0` only"                                                                                                                                                   |
| The parked hold names this revisit condition  | `docs/ideas.md` (the `paketo-nginx` 1.3.3 entry) — "Revisit when `pack builder inspect` on the pinned builder lists `nginx 1.3.3`"                                                                                                                                              |
| The live docs stated the old pins at the base | `git grep -n -e 'builder-jammy-base:0.4.667' -e 'nginx@1.3.0' 837aaa52 -- . ':!openspec/changes/archive'` → the `deployment-operations` pack's two pin sentences and `DocumentedPinRules`' KDoc example                                                                         |
| amd64 emulation works on this host            | `docker run --rm --platform linux/amd64 alpine uname -m` → `x86_64`; `docker info` → server `29.5.2`, driver `overlayfs`; colima `rosetta: true`                                                                                                                                |
| The Docker daemon was stopped at the start    | `colima status` → "colima is not running" before the first Docker command, `colima start` afterwards                                                                                                                                                                            |
| Only the web UI consumes these three pins     | `grep -rn` over the build scripts → `frontend-conventions.gradle.kts`'s `dockerBuildImage` sets the builder and both buildpacks; `build.gradle.kts` names them only for the update check                                                                                        |
| The check is clear after the bump             | `./gradlew buildpackUpdates` → "No buildpack updates available."                                                                                                                                                                                                                |
| The documented pins match the catalog         | `./gradlew verifyDocumentedPins` → BUILD SUCCESSFUL                                                                                                                                                                                                                             |
| The image builds with the new builder         | `./gradlew :showcase-web-ui:dockerBuildImage` → BUILD SUCCESSFUL, image `aanbrn/axon-showcase-web-ui:0.2.0-SNAPSHOT`                                                                                                                                                            |
| The image serves, and its nginx is x86-64     | `docker run` + `GET /` → 200; `config.js` → `window.__API_BASE_URL__ = "http://localhost:8080";`; `od -An -tx1 -j18 -N2 /layers/paketo-buildpacks_nginx/nginx/sbin/nginx` → `3e 00`; image platform `linux/amd64`; the metadata label names nginx `1.3.3` and procfile `5.15.5` |

**Dismissals.** `scripts/claim-scan.py` fires on shape rather than on claimhood: paths and command invocations, version
numbers, words like `only`/`every`/`never`, and repository-path or `#NNN` references. A candidate is a claim only where
a claims row states it; the rest are pointers a reader resolves against the repository, and the version numbers in
`build-logic/src/test/kotlin/DocumentedPinRulesTests.kt` are fixtures that need not track the catalog.
