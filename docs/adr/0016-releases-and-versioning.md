# ADR-0016: Publish releases as tagged GitHub releases

Date: 2026-10-04

Status: Accepted

Revisit when: more than one release occurs between manual version bumps, or a release cadence makes the bump recur — the
signal that the manual post-release bump (a follow-up PR) has become a tax worth automating.

## Context

The repository shipped versioned service images (`aanbrn/axon-showcase-*:${project.version}`) and carried hundreds of
merged pull requests, but had published no git tag and no GitHub Release: `git tag` was empty and `.github/workflows/`
held no release workflow. There was no "what shipped, when" surface, so a visitor could not see which changes a version
contains, and the versioned images named a version no release ever declared.

The project version was declared in the root `build.gradle.kts` (`allprojects { version = "0.1.0-SNAPSHOT" }`), and the
gateway's OpenAPI document carried a third, hand-maintained copy (`@Info(version = "0.1.0")`). With no release process,
a decision was needed on the version scheme, the cadence, and where the release notes come from.

## Decision

Publish releases on demand through a `workflow_dispatch` workflow that creates the tag `v<version>` at the head of
`main` and a GitHub Release whose notes GitHub generates from the pull requests merged since the previous release. The
version scheme is SemVer, `vMAJOR.MINOR.PATCH`, starting at `v0.1.0`.

Declare the project version once, in `gradle.properties`, and apply it to every project in the build. A version under
development carries a `-SNAPSHOT` suffix, and the release workflow accepts only a dispatched version equal to the
declared version without that suffix, so a tag cannot name a version the build does not.

The gateway reports the build's version in its OpenAPI document: Spring Boot's `buildInfo()` writes the project version
into `META-INF/build-info.properties` (baked into the artifact), and an `OpenApiCustomizer` bean reads the
`BuildProperties` bean and sets the document's `info.version`. `build.time` is excluded, so the generated file carries
no synthetic timestamp and the task stays cacheable.

Alternatives considered and rejected: starting at `v1.0.0` (declares a stability the reference app has not committed to)
and a calendar version (no compatibility signal for a library-and-service repo); a per-change release (a tag per merge —
high volume, little signal) and a fixed schedule (releases whatever has landed, not a milestone); curating the notes
from archived OpenSpec changes (richer grouping, but a manual step nothing needs yet); keeping the version in
`build.gradle.kts` and parsing it in the workflow (brittle to a line-format change); an env-var-injected
`${project.version}` placeholder in the `@Info` annotation (resolves only where the variable is present — `bootRun`
without it serves the literal); and enabling `<build-info>` with a fixed `build.time` (writes a value nothing reads and
that misstates the build).

Auto-bumping the development version after a release is deferred: it needs a push to `main` that the rulesets withhold
(the workflow token is not a bypass actor, and `main-required-checks` has no bypass actors), and admitting a bot to the
bypass list would loosen the protection that makes `main` trustworthy for a one-line edit at a cadence that fires
rarely. The bump is a follow-up PR for now.

## Consequences

- A release is one dispatch; the tag, the Release, and its notes are produced together, and the notes need no
  per-release curation.
- The tag and the build report the same version, because both read one declaration; the images the release names are
  built from it.
- The served OpenAPI document reports the build's version rather than a literal, so a release cannot leave it stale.
- The dev version must be bumped after a release to give the next one a base; until then the workflow rejects a
  re-release dispatch (the tag exists, and the declared version already names the released one). A `publish_tag`
  re-dispatch that only backfills an existing release's images (ADR-0018) is unaffected.
- The release workflow's first run happens after its merge, since GitHub exposes `workflow_dispatch` only once the file
  is on the default branch.
