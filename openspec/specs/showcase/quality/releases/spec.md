# showcase/quality/releases Specification

## Purpose

Defines how the repository publishes a version: the on-demand release workflow, the tag and GitHub Release it creates,
the generated release notes, the service images it publishes to the GitHub Container Registry, and the single version
declaration that the tag, the build, and the served OpenAPI document share.

## Requirements

### Requirement: Release workflow publishes a tagged GitHub release

Releases SHALL be published through a `release` workflow triggered only by manual dispatch. It SHALL run on
`ubuntu-latest` with `GITHUB_TOKEN` granted `contents: write` and `packages: write`, and SHALL NOT be a required check
for merging into `main`. On a valid dispatch that is not a dry run it SHALL create the tag `v<version>` at the head of
`main` and publish a GitHub Release for that tag whose notes GitHub generates from the pull requests merged since the
previous release.

#### Scenario: Manual dispatch publishes a release

- **WHEN** a maintainer dispatches the `release` workflow from `main` with a valid version without the dry-run input
- **THEN** the workflow creates the tag `v<version>` at the head of `main` and publishes a GitHub Release for it whose
  notes are generated from the pull requests merged since the previous release

#### Scenario: A version whose tag already exists is rejected

- **WHEN** a maintainer dispatches the workflow with a version whose tag `v<version>` already exists
- **THEN** the run fails without moving the existing tag or mutating its release

#### Scenario: A dispatch from a ref other than main is rejected

- **WHEN** the workflow is dispatched against a ref other than `main` without the dry-run input
- **THEN** the run fails without creating a tag or a release

#### Scenario: A dry run may be dispatched from another ref

- **WHEN** the workflow is dispatched from a ref other than `main` with the dry-run input
- **THEN** the run proceeds, pushing only the throwaway tag and creating no tag or release

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

### Requirement: The release workflow publishes the service images

On a valid release dispatch that is not a dry run, the `release` workflow SHALL build the five images and publish each
to GitHub Container Registry as `ghcr.io/<owner>/<image>`, tagged with the released version and `latest` for
`linux/amd64`, authenticating with the run's `GITHUB_TOKEN`.

#### Scenario: Manual dispatch publishes the images

- **WHEN** a maintainer dispatches the `release` workflow from `main` with a valid version without the dry-run input
- **THEN** the workflow builds all five images and pushes each to `ghcr.io/<owner>/<image>`

#### Scenario: Each image carries the released version and latest

- **WHEN** the workflow publishes the service images for version `X.Y.Z`
- **THEN** each image is tagged both `X.Y.Z` and `latest`

#### Scenario: The published images target linux/amd64

- **WHEN** the workflow publishes the service images
- **THEN** each published image is a `linux/amd64` image

### Requirement: A release is created only after its images publish

The `release` workflow SHALL create the tag `v<version>` and the GitHub Release only after every service image is
published, so a published release never names an image that failed to publish. A dry-run dispatch SHALL publish the
images under a throwaway tag and create neither the tag nor the Release.

#### Scenario: A failed image publish leaves no tag or release

- **WHEN** the build or push of any image fails
- **THEN** the run fails without creating the tag `v<version>` or a GitHub Release for it

#### Scenario: A dry run publishes no release tags and creates no release

- **WHEN** a maintainer dispatches the workflow with the dry-run input set
- **THEN** the workflow builds and pushes the images under a throwaway tag, and creates neither the `latest` tag nor the
  tag `v<version>` nor a GitHub Release
