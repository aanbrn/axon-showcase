# Spec Delta

## ADDED Requirements

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

## MODIFIED Requirements

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
