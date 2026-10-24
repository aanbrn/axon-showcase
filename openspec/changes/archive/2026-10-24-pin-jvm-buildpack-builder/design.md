# Design

## Context

See `proposal.md` — Why. The JVM services build their images through Spring Boot's `bootBuildImage`, which resolves its
builder from `BuildRequest.DEFAULT_BUILDER_IMAGE_REF` unless the task's `builder` is set; `spring-boot-conventions` sets
only `buildpacks` and `environment` today, so all four services use the floating `latest`. The web-UI image takes the
other route (`frontend-conventions` sets a catalog-pinned `builder` and versioned buildpacks), which is the pattern this
change applies to the JVM path.

The platform mechanism matters because it decides whether a retagged builder breaks a build: when the request carries no
image platform, `Builder.build` pulls the builder first and then takes the platform **from that image**
(`Builder.java:149-156`), so an arm64-only builder tag silently makes an amd64 host build an arm64 image. An explicit
`imagePlatform` would override it, but nothing in this repository sets one.

### Claims

| Claim                                                                                                                                    | Source (re-run to reproduce)                                                                                                                                                             |
| ---------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The nightly E2E run failed at `bootBuildImage`, all four JVM services, not at a test                                                     | `gh run view 40038057376 --log-failed`                                                                                                                                                   |
| `latest` is arm64-only, digest `771c7c33…`, retagged `2026-10-23T14:50:59Z`                                                              | `curl -s https://hub.docker.com/v2/repositories/paketobuildpacks/builder-noble-java-tiny/tags/latest`                                                                                    |
| `0.0.200` is multi-arch: amd64 `dd2194726b83…`, arm64 `771c7c33710f…`                                                                    | same API, `.../tags/0.0.200`                                                                                                                                                             |
| The previous night's run pulled `latest` as builder `0.0.198` (index `be9118fd…`) with run image `0.0.148` for `linux/amd64`, and passed | `gh run view 39871419881 --log`; Docker Hub `.../tags/0.0.198`'s tag digest                                                                                                              |
| Boot 3.5.16's default builder is `paketobuildpacks/builder-noble-java-tiny:latest`                                                       | `BuildRequest.java:49-51`, `spring-boot-buildpack-platform-3.5.16-sources.jar`                                                                                                           |
| The build platform is derived from the pulled builder image when none is requested                                                       | `Builder.java:149-156`, same jar                                                                                                                                                         |
| `0.0.200` bundles `paketo-buildpacks/java` `22.7.1` and `spring-boot` `5.40.3`, not health-checker                                       | `pack builder inspect paketobuildpacks/builder-noble-java-tiny:0.0.200`                                                                                                                  |
| `paketobuildpacks/health-checker:2.14.0` is the newest tag and is multi-arch                                                             | `curl -s '.../paketobuildpacks/health-checker/tags?page_size=8'`, `pack buildpack inspect`                                                                                               |
| Its CNB id is `paketo-buildpacks/health-checker`, but the builder does not bundle it                                                     | `pack builder inspect` lists no health-checker entry; `pack buildpack inspect` names its id                                                                                              |
| `repo@version` is neither a builder-bundled id nor a valid image reference, so the build fails with `Invalid buildpack reference`        | `BuildpackResolvers.java`/`ImageBuildpack.java`, and a real build's failure message                                                                                                      |
| The upstream retag is already reported, with these digests and its cause                                                                 | `gh api repos/paketo-buildpacks/builder-noble-java-tiny/issues/344` (open, and its comments)                                                                                             |
| `verifyDocumentedPins` requires the pack to state every `buildpackChecks` pin                                                            | `build.gradle.kts`'s `verifyDocumentedPins` registration; `DocumentedPinRules.unstated`                                                                                                  |
| The gate parses only `paketo-buildpacks/<name>@` and `builder-jammy-base:` forms today                                                   | `build-logic/src/main/kotlin/DocumentedPinRules.kt` (`BUILDPACK`, `BUILDER`)                                                                                                             |
| amd64 emulation works on the development host, so the amd64 path is locally verifiable                                                   | `docker run --rm --platform linux/amd64 alpine uname -m` → `x86_64`                                                                                                                      |
| The deployment smoke and release workflows build the same images                                                                         | `.github/workflows/deployment-smoke.yml` and `release.yml` (each runs `bootBuildImage`)                                                                                                  |
| The run image comes from the builder metadata when the task sets none                                                                    | `Builder.java`'s `withRunImageIfNeeded`; the builder image's `io.buildpacks.builder.metadata` label (`ubuntu-noble-run-tiny:0.0.150` for `0.0.200`), and the earlier run log's `0.0.148` |

**Scan dismissals.** The `scripts/claim-scan.py` candidates for this diff fall into four kinds, each dismissed for the
reason named: path and token candidates are identifiers the change writes (catalog coordinates, the `@`/`docker://` pin
forms in the rules and their tests, the lifecycle error string in the pack) rather than facts needing a source;
cross-reference candidates are the catalog and tracker coordinates the rows above verify; absolute-quantifier and count
candidates are prose about the change's own scope, its task numbering, or the table's own text.

## Goals / Non-Goals

**Goals:**

- Make every JVM image build (local, E2E, deployment smoke, release) resolve the same builder and buildpack versions.
- Make a builder or health-checker bump visible in the existing `buildpackUpdates` report.
- Keep the documented-pin gate's coverage as the pin set grows, rather than documenting a form the build cannot use.

**Non-Goals:**

- Changing the image stack (noble → jammy), the JVM image's entrypoint and environment, or setting `runImage` on any
  task: with none set, Boot takes the run image from the builder metadata, so the builder pin fixes it — a consequence
  this change accepts rather than a goal.
- Setting `imagePlatform` on any task: the pinned builder is multi-arch, so the host's platform stays the right one for
  a local arm64 build and for the amd64 runner.
- Filing a new upstream report: `builder-noble-java-tiny#344` already carries the defect and its cause.

## Decisions

### Pin the builder by version tag, not by index digest

Use `paketobuildpacks/builder-noble-java-tiny:0.0.200`.

- Alternative — the index digest (`sha256:7c62dd7e…`, the workaround suggested in #344's comments): precise, but no
  update check reports a newer digest, so the pin would silently age; a version tag is what `buildpackUpdates` compares.
  For this change, the owner requires version reproducibility, not content immutability, so version-tag pins are
  retained. If content immutability — not only version reproducibility — becomes a requirement, the
  tag-plus-index-digest combination is the option: the tag keeps the check comparable, the digest pins the bytes, and it
  needs its own bump process.
- Alternative — `--imagePlatform=linux/amd64` in the image-building workflows: it fixes the architecture, not the
  versions, so the retagged `latest` still decides which builder and lifecycle the build runs and this failure's cause
  remains; it would also have to be repeated in each of the three workflows.
- Alternative — wait for the upstream `latest` repair: the failure recurs on the next release that retags wrongly, and
  nothing in this repository would record the assumption.

### Pin the health-checker buildpack; leave the builder's bundled buildpacks unpinned

The buildpacks list keeps `paketo-buildpacks/java` and `paketo-buildpacks/spring-boot` unversioned, and gains the image
reference `docker://paketobuildpacks/health-checker:2.14.0`.

- `java` and `spring-boot` are resolved from the builder image (`pack builder inspect` lists both), so the builder pin
  already fixes them; pinning them too would tie the catalog to the builder's bundle and require a lockstep bump.
- health-checker is **not** bundled: it is pulled from Docker Hub at `latest` today, which is the same unpinned-input
  class the builder pin removes.
- Boot resolves a buildpack as a builder-bundled id, a local directory or archive, or an OCI image reference, so a
  pinned, image-published buildpack the builder does not bundle takes a tag with the `docker://` prefix;
  `repository@version` is rejected as an invalid reference (claims table).

### State the builder once, in the shared convention

`spring-boot-conventions.gradle.kts` sets `builder` from the catalog, so the four services — and any service added later
— share one pin; a per-module override stays possible through the task's own `builder` property.

### Widen the documented-pin gate instead of bypassing it

`DocumentedPinRules` gains a general builder form (`builder-<name>:<version>`, which covers `builder-jammy-base` and
`builder-noble-java-tiny`) and an image-reference buildpack form (`docker://<repository>/<name>:<version>`), both
mapping to the same catalog keys the `@`/`:` forms already map to (`paketo-builder-<name>`, `paketo-<name>`).

- Alternative — document health-checker in the CNB-id `@version` form: neither the build nor the builder's bundle can
  consume it, so a reader copying the statement into `buildpacks` would be rejected.
- Alternative — exclude the new pins from the gate: the gate derives its key set from `buildpackChecks`, so an exclusion
  would be a second list that drifts from the catalog.

### One spec delta per affected obligation, no Purpose refresh

`showcase/quality/infra-image-versions` owns which pin forms the gate verifies and which pins the pack must state; both
widen. `showcase/quality/merge-governance` owns the update report's scope, whose wording names one builder. Neither
capability's Purpose describes the Paketo pins (infra-image-versions' Purpose covers the infra components and the chart
drift gate; merge-governance's covers branch protection and CI gates), so neither is falsified and no Purpose edit is
owed.

## Risks / Trade-offs

- A pinned builder delays a base-OS patch until the builder carrying the patched run image is bumped → the
  `buildpackUpdates` report surfaces a newer builder each week, and the builder pin is how a patched run image arrives
  (Boot takes the run image from the builder metadata).
- The JVM images' Java/Spring Boot buildpack versions change with the first pinned build (from `latest`'s bundle to
  `0.0.200`'s) → the change verifies the image build itself (arm64 locally, amd64 through emulation) and the E2E
  workflow is dispatched from the branch before merge.
- `0.0.200` is the release whose retag broke `latest`; if its own manifest were defective the pin would inherit that →
  the arm64 and amd64 local builds of one service are the check, and `0.0.199` is the fallback pin.
- A future bump must edit the catalog **and** the pack's stated pin → that is the gate's purpose: the mismatch fails
  `check`, naming the doc site and both versions.
- Rolling back means reverting the catalog entries and the pack's stated pins together → the documented-pin gate refuses
  a partial revert of those two, while the `builder.set(...)` line in `spring-boot-conventions` is unguarded: removing
  it alone leaves every gate green and returns the build to the floating `latest`.
