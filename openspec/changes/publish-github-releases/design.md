# Design

## Context

The repository has published no git tag and no GitHub Release: `git tag` is empty and `.github/workflows/` holds no
release workflow, so the versioned images (`aanbrn/axon-showcase-*:${project.version}`) name a version no release ever
declared. The parked idea in `docs/ideas.md` (2026-09-16) asked to decide the version scheme, what a release notes, and
the cadence; the owner chose SemVer starting at `v0.1.0`, an on-demand release, and notes generated from merged pull
requests. This change implements those decisions and moves the version to a single declaration.

Two constraints shape the mechanism:

- The `main` rulesets require a pull request for merges and linear history with no bypass actor for the latter, so a
  workflow token cannot push a version-bump commit to `main`. The workflow may create a tag (tags are not covered by the
  branch rulesets) but not a commit.
- GitHub exposes `workflow_dispatch` for a workflow only once it is on the default branch, so the release workflow's
  first run — and therefore its happy-path verification — happens after this change merges.

## Goals / Non-Goals

**Goals**

- One repeatable, on-demand way to cut a release: a tag plus a GitHub Release.
- The released tag and the version the build reports agree.
- Release notes need no per-release curation.

**Non-Goals**

- Publishing images to a registry — a separate parked idea; this workflow tags the source only.
- Auto-bumping the development version after a release — it needs a push to `main` the rulesets forbid (the workflow
  token is not a bypass actor; `main-required-checks` has no bypass actors at all), and a bot identity admitted to the
  bypass list would loosen the protection that makes `main` trustworthy for a one-line edit at a cadence that fires
  rarely — the manual follow-up PR (task 6.4) costs less than any route to it. The ADR records the reopening signal.
- A committed changelog file — the generated notes are the release record.
- Drafts, prereleases, signing, or SBOMs.
- A mechanically enforced assertion that the released tag's name equals the served OpenAPI `info.version` — both derive
  from the same `gradle.properties` declaration (the release rejects a version that does not match it, and the gateway
  resolves its document from it), but no check compares the tag string to the served document.

## Decisions

### SemVer starting at `v0.1.0`

Tags are `vMAJOR.MINOR.PATCH`, the first being `v0.1.0`. Rejected: starting at `v1.0.0` (declares a stability the
reference app has not committed to) and a calendar version (no compatibility signal for a library-and-service repo). The
current development version `0.1.0-SNAPSHOT` already names the first release, and `0.x` honestly signals a pre-1.0
surface.

### An on-demand release, not per-change or scheduled

The workflow runs only on `workflow_dispatch`. Rejected: per-change (a tag per merge — high volume, little signal) and a
fixed schedule (releases whatever happens to have landed, not a milestone). A release is a deliberate act.

### Notes generated from merged pull requests

`gh release create … --generate-notes` builds the notes from the pull requests merged since the previous release.
Rejected: curating them from archived OpenSpec changes (richer grouping, but a manual step nothing needs yet) and
hand-written notes (toil). Merged PR titles already are the repository's change summaries.

### The OpenAPI version comes from the build info

The gateway enables Spring Boot's `buildInfo()` (`showcase-api-gateway/build.gradle.kts`), which adds a `bootBuildInfo`
task writing `META-INF/build-info.properties`; its `build.version` defaults to the project version, so it is
single-sourced with `gradle.properties` and baked into the artifact — correct in the image, under compose, in the chart,
under `bootRun`, and in tests alike, with no environment injection. A small `OpenApiCustomizer` bean reads the
`BuildProperties` bean and sets the document's `info.version`.

Why a customizer rather than `@Info(version = "${build.version}")`: `build-info.properties` is exposed as a
`BuildProperties` **bean**, not as a Spring `Environment` property source
(`ProjectInfoAutoConfiguration.buildProperties` is a plain `@Bean`, verified in spring-boot-autoconfigure 3.5.16), so a
placeholder would not resolve and would be served literally. The customizer reads the bean directly.

`build.time` is excluded (`excludes.set(listOf("time"))`), so the file carries only `artifact`, `group`, `name`, and
`version`. Its default — the build instant — makes `bootBuildInfo` never up-to-date, so every build (and every test that
depends on `classes`) re-runs it and the build stops being repeatable; the Spring Boot plugin reference names this
trade-off. A fixed timestamp avoids the re-run but writes a value nothing reads and that misstates the build (the
gateway exposes only `health` and `prometheus`, not `info`), so excluding the key is the honest and cacheable choice.
Verified in this repo against spring-boot-gradle-plugin 3.5.16: with the default the task re-ran on every invocation;
with `excludes = ["time"]` the generated file omitted `build.time` and the task reported `UP-TO-DATE`. (The 2.1.7
reference offers `time.set(null)` — that form is a no-op on 3.5.16, which needs the `excludes` set.)

Rejected: `@Info(version = "${project.version}")` resolved from the environment (verified working via relaxed binding of
a `PROJECT_VERSION` image default, but it resolves only where that env var is present — `bootRun` without it serves the
literal); a `bootBuildInfo`-driven Spring property source (there is none); and leaving the literal and syncing it by
hand (the drift this change removes). The customizer's integration assertion (task 2.4) serves the resolved version, not
the literal, so an unpopulated `BuildProperties` would fail it.

### An ADR records the release and versioning policy

`docs/adr/0016-releases-and-versioning.md` records the policy decided here — SemVer `vMAJOR.MINOR.PATCH` starting at
`v0.1.0`, an on-demand release, notes generated from merged PRs, a single version declaration, and the OpenAPI
`info.version` resolving from the build info — with the rejected alternatives. `docs/adr/README.md` is a format guide,
not an index, so it needs no entry. Rejected: leaving the policy only in this change's design and the new capability
spec (the design is change-scoped, and the ADR convention says to capture a cross-cutting decision when it is made). The
ADR is the durable _why_; the spec holds what the release must do and need not enumerate the SemVer/cadence/notes
choices, so a later policy change edits the ADR and its spec requirement together.

### The version lives in `gradle.properties`

The `version` declaration moves from the root `build.gradle.kts`'s `allprojects {}` block to `gradle.properties`. Gradle
reads it automatically for every project, and the release workflow reads the same declaration with a one-line script, so
a tag cannot name a version the build does not. Verified in a scratch build: `version=0.1.0-SNAPSHOT` in
`gradle.properties` resolves as the version of the root and of subprojects, and `-Pversion=0.2.0` overrides it.
Rejected: keeping the declaration in `build.gradle.kts` and parsing it in the workflow (brittle to a line-format
change); deriving the version from the git tag in the build with `git describe --tags --exact-match` (makes a tag
checkout build the exact version, but adds a configuration-time git exec to every build and depends on tags being
fetched — a shallow clone has none); and accepting an explicit version with no cross-check (lets the tag and the build
version diverge).

### Create the tag and the release in one step, after a guard

The workflow guards `if git ls-remote --tags --exit-code origin "refs/tags/v<version>"; then … exit 1; fi` (the
`--exit-code` probe exits `0` when the ref exists and `2` when it does not, so it must sit in an `if` to invert
correctly under the step shell's `set -e`), then runs
`gh release create "v<version>" --repo "$GITHUB_REPOSITORY" --target "$GITHUB_SHA" --generate-notes`, which creates the
tag and the release together. Rejected: creating an annotated tag, pushing it, then creating the release — an extra step
that needs a git identity for no gain, since the release, not the tag object, carries the notes and metadata.

### The workflow only tags `main`

A first step fails unless the ref is `refs/heads/main`, and the release targets that commit. A release therefore always
names a commit on `main`, never a branch head. The follow-up version bump is a normal PR.

## Risks / Trade-offs

- **A release is public and effectively permanent.** Deleting a mistaken tag or release rewrites a public surface. The
  run fails on an existing tag so it cannot silently move one, and a mistaken release can be corrected by deleting the
  release and tag by hand and editing the notes.
- **Generated notes depend on PR-title quality.** The repository already derives titles from substantive agent reports;
  the risk is low and the notes are editable after publication.
- **The happy path cannot be verified before the workflow is on `main`, and a dispatch creates a real release.** The
  failure paths (a mismatched version, a non-`main` ref) are verified with dispatches that create nothing, and the first
  real release is cut deliberately at the merge; `workflowLint` (actionlint) covers the workflow's syntax in CI.
- **Moving the version declaration touches a build file.** A mistake would retag the images. The resolved version is
  unchanged and asserted after the move (the build reports `0.1.0-SNAPSHOT`).

## Migration

After this change merges, dispatch the workflow with `0.1.0` to cut the first release, then bump `gradle.properties` to
`0.2.0-SNAPSHOT` in a follow-up PR. The image built for the release must be built at the declared version (before that
bump), since `bootBuildInfo` bakes whatever the declaration holds when `bootBuildImage` runs. There is no data
migration.
