# Tasks

## 1. Pre-write derivation

- [x] 1.1 Run the `review-gate` skill's first-stage derivation pass and re-run every command in `design.md`'s claims
      table, correcting any row whose output differs; completion is the table matching a fresh run of each command, with
      the two Docker Hub queries and the two `pack inspect` commands read in full (not sliced)

## 2. Catalog pins

- [x] 2.1 Add `paketo-builder-noble-java-tiny = "0.0.200"` and `paketo-health-checker = "2.14.0"` to
      `gradle/libs.versions.toml`'s `[versions]` beside the existing `paketo-*` entries; verify with
      `rg -n 'paketo-(builder-noble-java-tiny|health-checker)' gradle/libs.versions.toml`
- [x] 2.2 Set `bootBuildImage`'s builder from the catalog in
      `build-logic/src/main/kotlin/spring-boot-conventions.gradle.kts` (`paketobuildpacks/builder-noble-java-tiny:` +
      the catalog version) and pin the health-checker buildpack entry to the image reference
      `docker://paketobuildpacks/health-checker:` + its catalog version (the `@version` form is rejected by Spring
      Boot's buildpack resolver); verify the build configures with
      `./gradlew :showcase-query-service:bootBuildImage --dry-run`

## 3. Update check

- [x] 3.1 Add a `BuildpackUpdateCheck` for `paketo-builder-noble-java-tiny` (repository
      `paketobuildpacks/builder-noble-java-tiny`) and one for `paketo-health-checker` (repository
      `paketobuildpacks/health-checker`) to `buildpackChecks` in `build.gradle.kts`; verify `./gradlew buildpackUpdates`
      reports no row for either at the pinned versions

## 4. Documented-pin gate

- [x] 4.1 Generalize `DocumentedPinRules`' builder form to `builder-<name>:<version>` (mapping to
      `paketo-builder-<name>`) and add the image-reference buildpack form `docker://<repository>/<name>:<version>`
      (mapping to `paketo-<name>`) in `build-logic/src/main/kotlin/DocumentedPinRules.kt`
- [x] 4.2 Add cases to `build-logic/src/test/kotlin/DocumentedPinRulesTests.kt`: the noble builder statement maps to its
      catalog key and compares; an image-reference buildpack maps to the same key as its CNB id; a mismatch fails naming
      the doc site and both versions; a floating builder statement (`builder-noble-java-tiny:latest`) is not read as a
      pin; verify with `./gradlew -p build-logic test`

## 5. Documentation

- [x] 5.1 In `.opencode/skills/deployment-operations/SKILL.md`'s Docker images section, state
      `builder-noble-java-tiny:0.0.200` and `docker://paketobuildpacks/health-checker:2.14.0`, and record why the JVM
      builder is pinned (Spring Boot derives the build platform from the pulled builder image, so a retagged `latest`
      changes the platform — upstream `paketo-buildpacks/builder-noble-java-tiny#344`) and that the builder's bundled
      `java` and `spring-boot` buildpacks are resolved from the builder rather than pinned separately; verify
      `./gradlew verifyDocumentedPins` passes
- [x] 5.2 Park the stale `-PimagePlatform` claim the change passes by in the pack's Docker Images section in
      `docs/ideas.md` under a `## 2026-10-24` section, since it is a docs defect this change did not cause; verify
      `rg -n 'Correct the documented platform override' docs/ideas.md` matches
- [x] 5.3 Reword the README's buildpack-update sentence to "each pinned Paketo builder and buildpack coordinate", since
      the catalog now pins two builders; verify `rg -n 'each pinned Paketo builder and buildpack coordinate' README.md`

## 6. Verification

- [x] 6.1 Prove `buildpackUpdates` can report both new pins: temporarily set `paketo-health-checker` to `2.13.7` and,
      separately, `paketo-builder-noble-java-tiny` to `0.0.198`; each run of `./gradlew buildpackUpdates` reports the
      matching `: <old> -> <pinned>` row, and restoring the pin removes it
- [x] 6.2 Prove `verifyDocumentedPins` fails on a known-bad doc: change the pack's health-checker pin to a wrong
      version, run `./gradlew verifyDocumentedPins`, confirm the finding names the doc site and both versions, then
      restore and confirm it passes
- [x] 6.3 Build one service's image with the pinned builder on this host
      (`./gradlew :showcase-query-service:bootBuildImage`) and confirm it succeeds and logs the pinned builder digest
- [x] 6.4 Build the same service for the runner's platform
      (`./gradlew :showcase-query-service:bootBuildImage --imagePlatform=linux/amd64`) and confirm it succeeds — this is
      the path that failed; read the log for `for platform 'linux/amd64'`
- [x] 6.5 Run `./gradlew spotlessApply spotlessCheck -PskipITs -Pcoverage.gate.enabled=false` and
      `openspec validate --all`; both green
- [x] 6.6 Run the touched modules' lint and unit gates (`./gradlew -p build-logic test` and
      `./gradlew :showcase-query-service:check -PskipITs -Pcoverage.gate.enabled=false`)

## 7. Review

- [x] 7.1 Run the `review-quick` loop over the change dir and diff, append each round's row to `docs/review-rounds.tsv`,
      and fix findings until clean (the loop ran until a round reported "nothing remains"; the rows are in
      `docs/review-rounds.tsv`)
- [x] 7.2 Derive the deep-review floor with `scripts/review-routing.py`, decide at or above it, and record the outcome
      and the signals it relied on on this task — outcome `NOT_NEEDED`;
      `scripts/review-routing.py --change pin-jvm-buildpack-builder` printed no matched signals
- [x] 7.3 Run the `lesson-capture` subagent when the skip condition does not hold, apply its durable proposals, and
      record the applied net delta or the skip and its reason on this task — capture applied: the
      `deployment-operations` pack gains the buildpack-reference-form rule (`captured: pin-jvm-buildpack-builder`);
      `AGENTS.md` unchanged, since its inline-span authoring rule already covered the split-span defect (proposal
      rejected as already covered); the catalog-vs-`buildpackChecks` assertion is parked in `docs/ideas.md` as a check
      rather than prose

## Workflow follow-up

- Ask the owner for the manual review pass and stop there; do not commit, push, or archive before it is given.
- After approval: push, dispatch `gh workflow run e2e.yml --ref pin-jvm-buildpack-builder` and read the run before the
  archive commit, then archive per `ship-change`. Dispatched as run 40054913083: success, builder `0.0.200` at index
  `7c62dd7e…`, run image `ubuntu-noble-run-tiny:0.0.150` for `linux/amd64`, `Running creator`.
