# Design

## Context

A Paketo builder is not a version bump like a chart pin: the buildpacks are resolved against the builder's own set, and
a pin the builder does not carry is fetched from the registry — resolving to the wrong architecture slice, which builds
cleanly and then fails at run time. So the unit's first step is establishing each pair, not applying the report's rows.

Two builders are pinned here. The web UI image builds on `builder-jammy-base`, whose bundled `nginx` and `procfile`
buildpacks are also pinned explicitly (the web UI's `pack` invocation names them by version); the four JVM services
build on `builder-noble-java-tiny`, whose bundled `java` and `spring-boot` buildpacks are resolved from the builder and
are not pinned separately. The health-checker is not bundled by either builder — the JVM convention pins it as an OCI
image reference (`docker://paketobuildpacks/health-checker:<version>`), so it moves on its own row.

`pack builder inspect` on the new builders establishes the pairs:

- `paketobuildpacks/builder-jammy-base:0.4.675` lists `paketo-buildpacks/nginx` at **1.3.4** and
  `paketo-buildpacks/procfile` at **5.15.6**, over lifecycle `0.21.26` and the run image
  `paketobuildpacks/run-jammy-base:latest` (deliberately floating).
- `paketobuildpacks/builder-noble-java-tiny:0.0.205` lists `paketo-buildpacks/java` at **22.7.3** and
  `paketo-buildpacks/spring-boot` at **5.40.4** over lifecycle `0.21.26`, with the run image
  `ubuntu-noble-run-tiny:0.0.153`; no health-checker entry appears among its buildpacks.

So all five report rows are taken as two matched sets plus the standalone health-checker image.

The web UI image is also not verified by its build: a buildpack release can install a binary of the wrong architecture
and still report a successful build with a metadata label naming the pinned version. The verification runs the image and
reads the binary's ELF header from the buildpack's layer. The JVM image is verified by building it for `linux/amd64`
(the runner's platform) and running it, which exercises the bumped builder, its bundled Java buildpacks, and the
health-checker image.

## Goals / Non-Goals

**Goals:**

- Move both builders and the web UI buildpacks each bundles together, as two matched sets.
- Move the health-checker image pin, which no builder bundles.
- Verify the built web UI image by running it and reading nginx's architecture from the layer, and the JVM image by
  building for `linux/amd64` and running it.

**Non-Goals:**

- The run images (`paketobuildpacks/run-jammy-base:latest`, `ubuntu-noble-run-tiny:0.0.153`): the first is deliberately
  left floating for base-OS security, and the second comes from the builder's metadata.
- The builder-bundled `java` and `spring-boot` buildpacks: they are resolved from the builder and are not catalog pins.
- The `DocumentedPinRulesTests` fixture versions: they are fixtures for the rule's parsing, not catalog statements.

## Decisions

1. **Each builder's own listing decides its buildpack pins.** `pack builder inspect` on the new builder is the
   authority; the update report's rows are candidates, and each candidate is confirmed by it.
2. **Take both builder sets in one change.** Each builder's new bundled versions are the versions this change pins, so
   no pin is left a line behind its builder.
3. **Verify by running the images.** `docker run`, `GET /`, the rendered `config.js`, and the nginx binary's `e_machine`
   read from `/layers/paketo-buildpacks_nginx/nginx/sbin/nginx` (`3e 00` = x86-64); for the JVM image, a `linux/amd64`
   build and a run whose log shows the application and the health-checker starting.

## Risks / Trade-offs

- **A builder bump moves everything else the builder bundles.** The jammy builder's blast radius is the web UI image and
  its two bundled buildpack stages; the noble builder's is the four JVM service images and their Java runtime/Spring
  Boot buildpack stages, which the image build and run exercise.
- **The images are built for the host's daemon and run under emulation on an arm64 machine.** amd64 emulation is checked
  first (`docker run --rm --platform linux/amd64 alpine uname -m` → `x86_64`), because a host failure would mimic a pin
  defect.

## Verification

- `./gradlew buildpackUpdates` after the bump — no row remains.
- `./gradlew verifyDocumentedPins` — the pack's documented builder and buildpack pins match the catalog.
- `./gradlew :showcase-web-ui:dockerBuildImage` — the image builds with the new builder; the built image run answers
  `GET /`, renders `config.js`, reads nginx's ELF header as `3e 00`, and its metadata names nginx `1.3.4` and procfile
  `5.15.6`.
- `./gradlew :showcase-api-gateway:bootBuildImage --imagePlatform=linux/amd64` — the JVM image builds with the new noble
  builder, and a run of it starts the application and the health-checker.
- `./gradlew spotlessApply`/`spotlessCheck`, `openspec validate --all`, and the manual 120-character check over the
  change's whole changed set.

## Claims

| Claim                                                          | Source                                                                                                                                                                                                                                                                                                                                               |
| -------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The check reported five rows before the bump                   | `./gradlew buildpackUpdates` → `paketo-builder-jammy-base: 0.4.669 -> 0.4.675`, `paketo-builder-noble-java-tiny: 0.0.200 -> 0.0.205`, `paketo-health-checker: 2.14.0 -> 2.14.1`, `paketo-nginx: 1.3.3 -> 1.3.4`, `paketo-procfile: 5.15.5 -> 5.15.6`                                                                                                 |
| The catalog pinned those versions at the base                  | `git show f42881ea:gradle/libs.versions.toml` → `paketo-nginx = "1.3.3"`, `paketo-procfile = "5.15.5"`, `paketo-builder-jammy-base = "0.4.669"`, `paketo-builder-noble-java-tiny = "0.0.200"`, `paketo-health-checker = "2.14.0"`                                                                                                                    |
| The new jammy builder bundles nginx 1.3.4 and procfile 5.15.6  | `pack builder inspect paketobuildpacks/builder-jammy-base:0.4.675`, 484 lines read in full: the buildpack table lists nginx `1.3.4` and procfile `5.15.6`                                                                                                                                                                                            |
| The new jammy builder keeps the floating jammy run image       | the same output: lifecycle `0.21.26`, run image `paketobuildpacks/run-jammy-base:latest`                                                                                                                                                                                                                                                             |
| The new noble builder's bundled set and run image              | `pack builder inspect paketobuildpacks/builder-noble-java-tiny:0.0.205`, 111 lines read in full: lifecycle `0.21.26`, run image `ubuntu-noble-run-tiny:0.0.153`, java `22.7.3`, spring-boot `5.40.4`                                                                                                                                                 |
| The noble builder does not bundle the health-checker           | the same output: no `health-checker` entry in its buildpack table                                                                                                                                                                                                                                                                                    |
| Only the web UI consumes the jammy builder, nginx, procfile    | `grep -rn 'paketo.builder.jammy.base\|paketo.nginx\|paketo.procfile' --include='*.kts' --include='*.kt' .` → `frontend-conventions.gradle.kts` sets them on `dockerBuildImage`; `build.gradle.kts` names them only for the update check                                                                                                              |
| Only the JVM convention consumes the noble builder and checker | the same grep → `spring-boot-conventions.gradle.kts` sets the builder and the health-checker image reference; `build.gradle.kts` names them only for the update check                                                                                                                                                                                |
| The live docs stated the old pins at the base                  | `git grep -n -e 'builder-jammy-base:0.4.669' -e 'nginx@1.3.3' -e 'procfile@5.15.5' -e 'health-checker:2.14.0' -e 'builder-noble-java-tiny:0.0.200' f42881ea -- ':!openspec/changes/archive'` → the `deployment-operations` pack's four pin sentences and `DocumentedPinRules`' KDoc example, which this change de-versions rather than re-versioning |
| amd64 emulation works on this host                             | `docker run --rm --platform linux/amd64 alpine uname -m` → `x86_64`; `docker info --format '{{.ServerVersion}} {{.Driver}}'` → `29.5.2 overlayfs`; `pack version` → `0.40.10+git-9b483c2.build-7060`                                                                                                                                                 |
| The check is clear after the bump                              | `./gradlew buildpackUpdates` → no row                                                                                                                                                                                                                                                                                                                |
| The documented pins match the catalog                          | `./gradlew verifyDocumentedPins` → BUILD SUCCESSFUL                                                                                                                                                                                                                                                                                                  |
| The web UI image builds with the new builder                   | `./gradlew :showcase-web-ui:dockerBuildImage` → BUILD SUCCESSFUL, image `aanbrn/axon-showcase-web-ui:0.2.0-SNAPSHOT`                                                                                                                                                                                                                                 |
| The web UI image serves, and its nginx is x86-64               | `docker run` + `GET /` → 200; `config.js` renders the configured API base URL; `od -An -tx1 -j18 -N2 /layers/paketo-buildpacks_nginx/nginx/sbin/nginx` → `3e 00`; the metadata label names nginx `1.3.4` and procfile `5.15.6`                                                                                                                       |
| The JVM image builds for amd64 with the new noble builder      | `./gradlew :showcase-api-gateway:bootBuildImage --imagePlatform=linux/amd64` → BUILD SUCCESSFUL; `docker image inspect` → `linux/amd64`                                                                                                                                                                                                              |
| The JVM image runs with the new builder and health-checker     | `docker run` → the log contains `Started ShowcaseApiApplication`; `docker run --entrypoint /workspace/health-check … --help` prints the `thc` usage and exits `0`; the metadata names spring-boot `5.40.4` and bellsoft-liberica `11.10.4`, the set the previous-builder row contrasts with                                                          |
| The health-checker image pin resolves a published tag          | `docker manifest inspect paketobuildpacks/health-checker:2.14.1` → an OCI image index whose manifests include `linux/amd64`                                                                                                                                                                                                                          |
| The previous noble builder bundled the earlier buildpack set   | `pack builder inspect paketobuildpacks/builder-noble-java-tiny:0.0.200` → spring-boot `5.40.3` and bellsoft-liberica `11.10.1`                                                                                                                                                                                                                       |

**Dismissals.** `scripts/claim-scan.py` fires on shape rather than on claimhood: paths and command invocations, version
numbers, words like `only`/`every`/`never`, and repository-path or `#NNN` references. A candidate is a claim only where
a claims row states it; the rest are pointers a reader resolves against the repository, and the version numbers in
`build-logic/src/test/kotlin/DocumentedPinRulesTests.kt` are fixtures that need not track the catalog.
