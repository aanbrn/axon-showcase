# Tasks

## 1. Single-source the project version

- [x] 1.1 Add `version=0.1.0-SNAPSHOT` to `gradle.properties`.
- [x] 1.2 Remove the `version = "0.1.0-SNAPSHOT"` line from the `allprojects {}` block in `build.gradle.kts`, leaving
      the block's other configuration in place.
- [x] 1.3 Confirm the resolved version is unchanged: `./gradlew -q :showcase-command-service:properties` reports
      `version: 0.1.0-SNAPSHOT`, and `./gradlew -q -Pversion=0.2.0 :showcase-command-service:properties` reports
      `version: 0.2.0` — read both outputs, do not assume them.
- [x] 1.4 Run `./gradlew spotlessCheck` (the moved file is Spotless-owned through the Kotlin DSL target) and the fast
      gate `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`.

## 2. Single-source the OpenAPI version

- [x] 2.1 In `showcase-api-gateway/build.gradle.kts`, add a `springBoot { buildInfo { excludes.set(listOf("time")) } }`
      block, so `build-info.properties` carries no `build.time` and `bootBuildInfo` is cacheable (its default time — the
      build instant — makes it never up-to-date). Run `./gradlew :showcase-api-gateway:bootBuildInfo`, read the
      generated file (it must have `build.version` and no `build.time`), then run it again and read the second run's
      status (it must be `UP-TO-DATE`).
- [x] 2.2 Add an `OpenApiCustomizer` bean (in `showcase-api-gateway/src/main/java/showcase/api/rest/`) that takes the
      `BuildProperties` bean and sets `openApi.getInfo().setVersion(buildProperties.getVersion())` (null-safe: if the
      document or its `info` is null, leave it).
- [x] 2.3 Remove the literal `version = "0.1.0"` from `ShowcaseRestApi.java`'s `@OpenAPIDefinition(info = @Info(...))`
      annotation, so the customizer is the only source of `info.version`.
- [x] 2.4 Add an integration-tier regression assertion: a `GET /v3/api-docs` case in
      `showcase-api-gateway/src/integrationTest/java/showcase/api/ShowcaseApiApplicationIT.java` (whose full-context
      `@SpringBootTest` already binds a `WebTestClient`) asserting the served document's `info.version` equals
      `System.getProperty("project.version")` and is not the literal `0.1.0` — this is the assertion the design's
      decision relies on, so it must exist as a test.
- [x] 2.5 Verify the deployed shape carries the version: build the image
      (`./gradlew :showcase-api-gateway:bootBuildImage`) at the declared version, run it, and assert the served OpenAPI
      document's `info.version` equals **that declared version** (`0.1.0-SNAPSHOT`, the `build-info.properties` value
      baked at build time) — not an injected property and not a stale literal. Build before the follow-up bump (task
      6.4), so the image and the declaration agree; read the output.
- [x] 2.6 Run `./gradlew :showcase-api-gateway:spotlessCheck` and `./gradlew :showcase-api-gateway:integrationTest`
      (Docker required) and confirm 2.4's new case runs and passes.

## 3. Record the policy as an ADR

- [x] 3.1 Add `docs/adr/0016-releases-and-versioning.md` (the next number after 0015) in the Nygard format the template
      in `docs/adr/README.md` specifies: a `Date: YYYY-MM-DD` line, `Status: Accepted`, a Context (no tag or Release
      today; the version surfaces and their shape), a Decision (SemVer `vMAJOR.MINOR.PATCH` starting at `v0.1.0`, an
      on-demand release, notes generated from merged PRs, one version declaration, the OpenAPI version resolving from
      the build info), Consequences, and a `Revisit when:` line for the deferred auto-bump naming a checkable signal —
      e.g. "more than one release between manual version bumps, or a release cadence that makes the bump recur" (the
      design's Non-Goal; the template makes that line required for a deferral) — matching ADR-0015's shape otherwise.
- [x] 3.2 Confirm `docs/adr/README.md` is a format guide, not an ADR index — it holds no per-ADR list, so no entry is
      added there. (If the file has gained an index since, add `ADR-0016` to it.)
- [x] 3.3 Re-read the ADR against the design's "Decisions" — the two record the same policy, so any divergence is a
      defect in whichever drifted; the ADR is the durable _why_ and the design the change-scoped rationale.

## 4. Add the release workflow

- [x] 4.1 Add `.github/workflows/release.yml`: a `workflow_dispatch` workflow with a required `version` input
      (`type: string`), `permissions: contents: write`, running on `ubuntu-latest`.
- [x] 4.2 First step: fail unless `github.ref` is `refs/heads/main`.
- [x] 4.3 Read the declared version from `gradle.properties`, require it to match `^[0-9]+\.[0-9]+\.[0-9]+-SNAPSHOT$`,
      and require the dispatched `version` to match `^[0-9]+\.[0-9]+\.[0-9]+$` and to equal the declaration without the
      `-SNAPSHOT` suffix; fail otherwise, printing the expected value.
- [x] 4.4 Fail if the tag already exists. `git ls-remote --tags --exit-code …` exits `0` when the ref **exists** and `2`
      when it does not, so write it as
      `if git ls-remote --tags --exit-code origin "refs/tags/v<version>"; then … exit 1; fi` — the `if` suppresses the
      step shell's `set -e`, so a bare `--exit-code` command would abort on a missing tag (the opposite of the intent).
- [x] 4.5 Create the tag and the GitHub Release in one step:
      `gh release create "v<version>" --repo "$GITHUB_REPOSITORY" --target "$GITHUB_SHA" --generate-notes`.
- [x] 4.6 Run `./gradlew workflowLint` and confirm it passes; actionlint parses the YAML and the step shell, not the
      `gh` invocation's arguments, so task 6.3's dispatch is the real verification of the command's quoting.

## 5. Documentation

- [x] 5.1 `README.md`: add the `release` workflow to the Continuous Integration section, describing the on-demand
      release and its generated notes (match the surrounding paragraph style).
- [x] 5.2 `AGENTS.md`: add `.github/workflows/release.yml` to the Continuous Integration per-workflow paragraphs, and
      note the release process — the version declaration in `gradle.properties`, the bump-after-release step, and that a
      dispatch requires the workflow to be on the default branch first. (The per-workflow "What each covers" list is
      left alone: it enumerates only the issue-opening scheduled checks, and the release workflow opens no issue.)
- [x] 5.3 Check `openspec/config.yaml`'s `context:` block — it carries runtime/Spring/Gradle versions and the module
      count, not the project version, so confirm no fact there moves; update it if one does.
- [x] 5.4 Remove the "Publish a first GitHub release and keep tagging" entry from `docs/ideas.md` (the change implements
      it; the removal rides this branch).
- [x] 5.5 Run `./gradlew spotlessApply` and re-run `spotlessCheck` after the last docs edit (a tick or a reword is an
      edit too).

## 6. Verification

- [x] 6.1 Read the final `release.yml` and confirm each failure path in the delta spec has a corresponding guard
      (non-`main` ref, malformed version, mismatched version, existing tag).
- [x] 6.2 Show the check hits a known-bad input: run the workflow's version-validation snippet locally against `0.2.0`
      (mismatched) and confirm it exits non-zero, and against `0.1.0` (matching) and confirm it passes — from the same
      script body the workflow runs.
- [x] 6.3 After the change merges, dispatch the workflow with a mismatched version (e.g. `9.9.9`) from `main` and
      confirm the run fails creating nothing, then dispatch `0.1.0` to cut the first release and verify the tag `v0.1.0`
      and its generated notes. The workflow is new, so `workflow_dispatch` exists only on the default branch — run this
      at the merge and name it in the change's report. **Done at the merge:** the `9.9.9` dispatch failed at "Validate
      the requested version" creating nothing (0 tags, no releases); the `0.1.0` dispatch cut tag `v0.1.0` at the merge
      commit `97382e0` and published the release with generated notes.
- [x] 6.4 Open a follow-up PR bumping `gradle.properties` to the next development version (`0.2.0-SNAPSHOT`), so the
      next release has a base to name. **Done:** PR #491, merged.

## 7. Capture

- [x] 7.1 Run the `lesson-capture` subagent over the implementation diff and the review findings, apply the durable
      proposals, and record the applied net `AGENTS.md` delta on this task. **Applied net `AGENTS.md` delta: +27 lines**
      (2672 vs 2645 at `HEAD`; `git diff --shortstat`: 51 insertions, 24 deletions — the deletions are the reflow of the
      target bullet the second rule merged into, whose distinctive tokens were verified to survive). Two rules: (1) a
      new Gotcha on exit-code-probe inversion (`git ls-remote --tags --exit-code` → 0 when the ref exists, 2 when it
      does not — run it inside an `if`); (2) a clause merged into the existing "A CLI's `--help` and its docs are not
      its contract" bullet, extending it to framework docs (the pinned jar, not the doc's snippet, decides the
      mechanism). No retirements.
