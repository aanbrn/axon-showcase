# Design

## Context

A Paketo builder is not a version bump like a chart pin: the buildpacks are resolved against the builder's own set, and
a pin the builder does not carry is fetched from the registry — resolving to the wrong architecture slice, which builds
cleanly and then fails at run time. So the unit's first step is establishing each pair, not applying the report's rows.

Two builders are pinned here. The web UI image builds on `builder-jammy-base`, whose bundled `nginx` and `procfile`
buildpacks are also pinned explicitly (the web UI's `pack` invocation names them by version); the four JVM services
build on `builder-noble-java-tiny`, whose bundled `java`, `spring-boot`, and `bellsoft-liberica` buildpacks are resolved
from the builder and are not pinned separately. The health-checker is not bundled by either builder — the JVM convention
pins it as an OCI image reference (`docker://paketobuildpacks/health-checker:<version>`), so it moves on its own row.

`pack builder inspect` on the new builders establishes the pairs:

- `paketobuildpacks/builder-jammy-base:0.4.681` lists `paketo-buildpacks/nginx` at **1.3.5** and
  `paketo-buildpacks/procfile` at **5.15.7**, over lifecycle `0.21.28` and the run image
  `paketobuildpacks/run-jammy-base:latest` (deliberately floating).
- `paketobuildpacks/builder-noble-java-tiny:0.0.211` lists `paketo-buildpacks/java` at **22.8.0**,
  `paketo-buildpacks/spring-boot` at **5.40.5**, and `paketo-buildpacks/bellsoft-liberica` at **11.10.5** over lifecycle
  `0.21.28`, with the run image `ubuntu-noble-run-tiny:0.0.157`; no health-checker entry appears among its buildpacks.

So all five report rows are taken as two matched sets plus the standalone health-checker image.

The web UI image is also not verified by its build: a buildpack release can install a binary of the wrong architecture
and still report a successful build with a metadata label naming the pinned version. The verification runs the image and
reads the binary's ELF header from the buildpack's layer. The JVM image is verified by building it for `linux/amd64`
(the runner's platform) and running it, then probing the application's health path with the image's own health-check
binary — the probe `docker-compose.yml` runs — which exercises the bumped builder, its bundled Java buildpacks, and the
health-checker image together.

## Goals / Non-Goals

**Goals:**

- Move both builders and the web UI buildpacks each bundles together, as two matched sets.
- Move the health-checker image pin, which no builder bundles.
- Verify the built web UI image by running it and reading nginx's architecture from the layer, and the JVM image by
  building for `linux/amd64` and running it.

**Non-Goals:**

- The run images (`paketobuildpacks/run-jammy-base:latest`, `ubuntu-noble-run-tiny:0.0.157`): the first is deliberately
  left floating for base-OS security, and the second comes from the builder's metadata.
- The builder-bundled `java`, `spring-boot`, and `bellsoft-liberica` buildpacks: they are resolved from the builder and
  are not catalog pins.
- The `DocumentedPinRulesTests` fixture versions: they are fixtures for the rule's parsing, not catalog statements.

## Decisions

1. **Each builder's own listing decides its buildpack pins.** `pack builder inspect` on the new builder is the
   authority; the update report's rows are candidates, and each candidate is confirmed by it.
2. **Take both builder sets in one change.** Each builder's new bundled versions are the versions this change pins, so
   no pin is left a line behind its builder.
3. **Verify by running the images.** `docker run`, `GET /`, the rendered `config.js`, and the nginx binary's `e_machine`
   read from `/layers/paketo-buildpacks_nginx/nginx/sbin/nginx` (`3e 00` = x86-64); for the JVM image, a `linux/amd64`
   build and a run in which `/workspace/health-check` with `THC_PATH=/actuator/health` exits zero against the running
   application.

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
- `./gradlew :showcase-web-ui:dockerBuildImage` — the image builds with the new builder; the built image answers
  `GET /`, renders `config.js`, reads nginx's ELF header as `3e 00`, and its metadata names nginx `1.3.5` and procfile
  `5.15.7`.
- `./gradlew :showcase-api-gateway:bootBuildImage --imagePlatform=linux/amd64` — the JVM image builds with the new noble
  builder; a run of it starts the application, and `/workspace/health-check` with `THC_PATH=/actuator/health` — the
  probe `docker-compose.yml` runs — exits zero against the running application, so the bumped health-checker performs
  its job rather than merely starting.
- `./gradlew spotlessApply`/`spotlessCheck`, `openspec validate --all`, and the manual 120-character check over the
  change's whole changed set.

## Claims

| Claim                                                               | Source                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| ------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The check reported five rows at the unit's base                     | `./gradlew buildpackUpdates` on the tree at `bfe30a08`, before the catalog edit → `paketo-builder-jammy-base: 0.4.675 -> 0.4.681`, `paketo-builder-noble-java-tiny: 0.0.205 -> 0.0.211`, `paketo-health-checker: 2.14.1 -> 2.14.2`, `paketo-nginx: 1.3.4 -> 1.3.5`, `paketo-procfile: 5.15.6 -> 5.15.7`                                                                                                                                                                                                                                                                  |
| The catalog pinned those versions at the base                       | `git show bfe30a08:gradle/libs.versions.toml` → `paketo-nginx = "1.3.4"`, `paketo-procfile = "5.15.6"`, `paketo-builder-jammy-base = "0.4.675"`, `paketo-builder-noble-java-tiny = "0.0.205"`, `paketo-health-checker = "2.14.1"`                                                                                                                                                                                                                                                                                                                                        |
| The new jammy builder bundles nginx 1.3.5 and procfile 5.15.7       | `pack builder inspect paketobuildpacks/builder-jammy-base:0.4.681`, read in full: the buildpack table lists nginx `1.3.5` and procfile `5.15.7`                                                                                                                                                                                                                                                                                                                                                                                                                          |
| The new jammy builder keeps the floating jammy run image            | the same output: lifecycle `0.21.28`, run image `paketobuildpacks/run-jammy-base:latest`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| The new noble builder's bundled set and run image                   | `pack builder inspect paketobuildpacks/builder-noble-java-tiny:0.0.211`, read in full: lifecycle `0.21.28`, run image `ubuntu-noble-run-tiny:0.0.157`, java `22.8.0`, spring-boot `5.40.5`, bellsoft-liberica `11.10.5`                                                                                                                                                                                                                                                                                                                                                  |
| The noble builder does not bundle the health-checker                | the same output: no `health-checker` entry in its buildpack table                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| The previous noble builder bundled the earlier set                  | `pack builder inspect paketobuildpacks/builder-noble-java-tiny:0.0.205` → lifecycle `0.21.26`, run image `ubuntu-noble-run-tiny:0.0.153`, java `22.7.3`, spring-boot `5.40.4`, bellsoft-liberica `11.10.4`                                                                                                                                                                                                                                                                                                                                                               |
| The health-checker image pin resolves a published tag               | `docker manifest inspect paketobuildpacks/health-checker:2.14.2` → an OCI image index resolved without error                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| Only the web UI consumes the jammy builder, nginx, procfile         | `grep -rn -e 'paketo\.builder\.jammy\.base' -e 'paketo\.nginx' -e 'paketo\.procfile' -e 'paketo\.builder\.noble\.java\.tiny' -e 'paketo\.health\.checker' --include='*.kts' --include='*.kt' .` → `frontend-conventions.gradle.kts` sets the jammy builder, nginx, and procfile on `dockerBuildImage`; `build.gradle.kts` names all five keys only for the update check; `PaketoPinCoverageRulesTests.kt` uses alias keys (`paketo.nginx`) as parsing fixtures, and `PaketoPinCoverageRules.kt`'s KDoc cites `paketo.nginx` as an example alias — neither consumes a pin |
| Only the JVM convention consumes the noble builder and checker      | the same grep → `spring-boot-conventions.gradle.kts` sets the noble builder and the health-checker image reference                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| The live docs stated the old pins at the base                       | `git grep -n -e 'builder-jammy-base:0.4.675' -e 'nginx@1.3.4' -e 'procfile@5.15.6' -e 'health-checker:2.14.1' -e 'builder-noble-java-tiny:0.0.205' bfe30a08 -- ':!openspec/changes/archive'` → four pin sentences in the `deployment-operations` pack, and no hit in `AGENTS.md` or `README.md`                                                                                                                                                                                                                                                                          |
| The compose gateway's healthcheck is the image's own probe          | `docker-compose.yml:68-76` → `test: [ "CMD", "/workspace/health-check" ]` with `THC_PATH=/actuator/health`; the Helm-deployed gateway instead uses `httpGet` probes on the actuator paths (`helm/chart/src/main/helm/templates/api-gateway/deployment.yaml`)                                                                                                                                                                                                                                                                                                             |
| amd64 emulation works on this host                                  | `docker run --rm --platform linux/amd64 alpine uname -m` → `x86_64`; `docker info --format '{{.ServerVersion}} {{.Driver}}'` → `29.5.2 overlayfs`; `pack version` → `0.40.10+git-9b483c2.build-7060`                                                                                                                                                                                                                                                                                                                                                                     |
| The check is clear after the bump                                   | `./gradlew buildpackUpdates` → `No buildpack updates available.`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| The documented pins match the catalog                               | `./gradlew verifyDocumentedPins` → BUILD SUCCESSFUL                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| The web UI image builds with the new builder                        | `./gradlew :showcase-web-ui:dockerBuildImage` → BUILD SUCCESSFUL, image `aanbrn/axon-showcase-web-ui:0.2.0-SNAPSHOT`                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| The web UI image serves, and its nginx is x86-64                    | `docker run` + `GET /` → 200; `config.js` renders the configured API base URL; `od -An -tx1 -j18 -N2 /layers/paketo-buildpacks_nginx/nginx/sbin/nginx` → `3e 00`; the metadata label names nginx `1.3.5` and procfile `5.15.7`                                                                                                                                                                                                                                                                                                                                           |
| The JVM image builds for amd64 with the new noble builder           | `./gradlew :showcase-api-gateway:bootBuildImage --imagePlatform=linux/amd64` → BUILD SUCCESSFUL; `docker image inspect --format '{{.Os}}/{{.Architecture}}' aanbrn/axon-showcase-api-gateway:0.2.0-SNAPSHOT` → `linux/amd64`                                                                                                                                                                                                                                                                                                                                             |
| The JVM image runs, and the bumped health-checker passes against it | `docker run` → the log contains `Started ShowcaseApiApplication`; `/workspace/health-check` with `THC_PATH=/actuator/health` exits `0` against the running application; the metadata names spring-boot `5.40.5`, bellsoft-liberica `11.10.5`, and health-checker `2.14.2`                                                                                                                                                                                                                                                                                                |

**Dismissals.** `scripts/claim-scan.py` fires on shape rather than on claimhood: paths and command invocations, version
numbers, words like `only`/`every`/`never`, and repository-path or `#NNN` references. A candidate is a claim only where
a claims row states it; the rest are pointers a reader resolves against the repository. The version numbers in
`build-logic/src/test/kotlin/DocumentedPinRulesTests.kt` are fixtures for the rules' parsing that need not track the
catalog, and `PaketoPinCoverageRulesTests.kt` exercises the same rules with alias keys (`paketo.nginx`) rather than
versions.
