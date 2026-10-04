# showcase/quality/releases Specification

## Purpose

Defines how the repository publishes a version: the on-demand release workflow, the tag it creates, the generated
release notes, and the single version declaration that the tag, the build, and the served OpenAPI document share.

## Requirements

### Requirement: Release workflow publishes a tagged GitHub release

Releases SHALL be published through a `release` workflow triggered only by manual dispatch. It SHALL run on
`ubuntu-latest` with `GITHUB_TOKEN` granted `contents: write`, and SHALL NOT be a required check for merging into
`main`. On a valid dispatch it SHALL create the tag `v<version>` at the head of `main` and publish a GitHub Release for
that tag whose notes GitHub generates from the pull requests merged since the previous release.

#### Scenario: Manual dispatch publishes a release

- **WHEN** a maintainer dispatches the `release` workflow from `main` with a valid version
- **THEN** the workflow creates the tag `v<version>` at the head of `main` and publishes a GitHub Release for it whose
  notes are generated from the pull requests merged since the previous release

#### Scenario: A version whose tag already exists is rejected

- **WHEN** a maintainer dispatches the workflow with a version whose tag `v<version>` already exists
- **THEN** the run fails without moving the existing tag or mutating its release

#### Scenario: A dispatch from a ref other than main is rejected

- **WHEN** the workflow is dispatched against a ref other than `main`
- **THEN** the run fails without creating a tag or a release

#### Scenario: A malformed version is rejected

- **WHEN** the workflow is dispatched with a version that is not a semantic version of the form `MAJOR.MINOR.PATCH`
- **THEN** the run fails without creating a tag or a release

#### Scenario: The release workflow is not a merge gate

- **WHEN** a pull request or a push to `main` is evaluated for merging
- **THEN** the `release` workflow is not required, because it runs only on manual dispatch and no ruleset requires it

### Requirement: The project version is declared once and released as tagged

The project version SHALL be declared once, in `gradle.properties`, and apply to every project in the build. The version
under development SHALL carry a `-SNAPSHOT` suffix, and the release workflow SHALL accept only a dispatched version that
equals the declared version with that suffix removed, so a release tag names the same version the build reports.

#### Scenario: The build applies one version declaration

- **WHEN** the build resolves the version of any project
- **THEN** it equals the `version` declared in `gradle.properties`, so the declaration is the single source of truth

#### Scenario: The release requires the declared version's base

- **WHEN** a maintainer dispatches the workflow with a version that does not equal the declared development version
  without its `-SNAPSHOT` suffix
- **THEN** the run fails without creating a tag or a release

#### Scenario: A development version carries the snapshot suffix

- **WHEN** `gradle.properties` declares a version without a `-SNAPSHOT` suffix and a release is dispatched
- **THEN** the run fails, because a released version is the snapshot version's base and the declared development version
  is expected to carry the suffix

### Requirement: The OpenAPI document reports the project version

The API gateway's served OpenAPI document SHALL report the same version as the build, resolved from the single version
declaration rather than a literal, so a release does not leave a hard-coded version stale.

#### Scenario: The OpenAPI document reports the build version

- **WHEN** the gateway's OpenAPI document is served
- **THEN** its `info.version` equals the project version the build reports, resolved from the version declaration

#### Scenario: The OpenAPI document does not serve a stale literal

- **WHEN** the project version changes and the gateway is rebuilt
- **THEN** the served `info.version` reflects the new version rather than a literal fixed in a source annotation
