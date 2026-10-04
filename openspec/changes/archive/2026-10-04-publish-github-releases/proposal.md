# Proposal

## Why

The repository ships versioned service images and carries hundreds of merged pull requests, but it has published no git
tag and no GitHub Release. There is no "what shipped, when" surface for a visitor or contributor: nothing marks a
milestone, and no one can see which changes a version contains.

## What Changes

- Add `.github/workflows/release.yml` — an on-demand (`workflow_dispatch`) workflow that validates a requested version,
  creates the tag `v<version>` at the head of `main`, and publishes a GitHub Release whose notes GitHub generates from
  the pull requests merged since the previous release.
- Move the project `version` declaration out of `build.gradle.kts`'s `allprojects {}` block and into
  `gradle.properties`, so the build and the release workflow read one declaration and a release tag cannot name a
  version the build does not.
- Single-source the OpenAPI `info.version` — Spring Boot's `buildInfo()` writes the project version into
  `META-INF/build-info.properties` (baked into the artifact), and a small `OpenApiCustomizer` bean reads the
  `BuildProperties` bean and sets the document's `info.version` from it, so the served document reports the build's
  version in every context — image, compose, chart, `bootRun`, and tests alike.
- Record the release and versioning policy as `ADR-0016`, and the release process as a new capability,
  `showcase/quality/releases`.
- Document the release path in `README.md` and `AGENTS.md`, and retire the parking idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

- `showcase/quality/releases`: how the repository publishes a version — the on-demand release workflow, the tag it
  creates, the generated release notes, and the single version declaration the tag, the build, and the served OpenAPI
  document share.

### Modified Capabilities

None.

## Impact

- **Build**: the project version is declared in `gradle.properties` instead of the root `build.gradle.kts`; the resolved
  version is unchanged (`0.1.0-SNAPSHOT`), and a `-Pversion` override now takes effect. The gateway enables Spring
  Boot's `buildInfo()` (its `build.version` defaults to the project version) and reads it for the OpenAPI
  `info.version`.
- **CI**: a new workflow that needs `contents: write`; it is not part of the `check` task and not a required check for
  merging into `main`.
- **Docs**: the README's Continuous Integration section, `AGENTS.md`'s workflow lists, and a new
  `docs/adr/0016-releases-and-versioning.md` record the release path (`docs/adr/README.md` is a format guide, not an
  index, so it needs no entry).
- **Not in scope**: publishing images (a separate parked idea — the release workflow tags the source only), and a
  changelog file (the generated notes are the release record).
