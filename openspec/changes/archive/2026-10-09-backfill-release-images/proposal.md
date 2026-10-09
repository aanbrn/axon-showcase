# Proposal

## Why

The first release, `v0.1.0`, predates the image-publishing workflow (added 2026-10-09), so its images were never built
or pushed — the release names `ghcr.io/aanbrn/axon-showcase-*` versions that do not exist. The workflow cannot backfill
it: it accepts only the declared version's base, refuses a version whose tag already exists, and builds from the
dispatched ref rather than the tag. A released version should be runnable, and backfilling is a small, general
mechanism.

## What Changes

- **`.github/workflows/release.yml`** — add an optional `publish_tag` input and a backfill mode: when set, the workflow
  checks out that tag, derives its version, builds the five images and pushes each to GHCR tagged only `<version>` (no
  `latest`, no tag and no GitHub Release); it skips the release-path guards (the tag must exist instead of not existing)
  and may be dispatched from any ref. Make `version` optional so a backfill dispatch need not pass one.
- **`openspec/changes/backfill-release-images/specs/showcase/quality/releases/spec.md`** — a delta that adds the
  backfill requirement and a mode-selection requirement, widens the `main`-ref guard for a backfill, and scopes the
  image-publishing requirements to the release path.
- **`README.md`**, **`AGENTS.md`**, **`openspec/config.yaml`**, **`docs/adr/0018-publish-service-images-to-ghcr.md`**,
  **`docs/adr/0016-releases-and-versioning.md`** — document the backfill dispatch, update ADR-0018 (its Decision and
  Consequences say publishing is release-only), and scope ADR-0016's re-dispatch claim to a re-release.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/releases`: adds requirements that the release workflow can publish the images for an existing
  release tag (without creating a tag or Release, and without moving `latest`) and that a dispatch requires exactly one
  of `version` or `publish_tag`; widens the `main`-ref guard so a backfill dispatch may run from another ref; and scopes
  the image-publishing requirements to the release path.

## Impact

- **Release workflow**: a new `publish_tag` input and mode branch; the failure-path and publish steps are shared with
  the release path.
- **Registry**: a backfill writes the five `ghcr.io/aanbrn/axon-showcase-*:<version>` tags for an existing release (no
  `latest`).
- **Unchanged**: the release path's behavior; local/`ci` builds; the chart.
- **Docs** refreshed: README, `AGENTS.md`, `openspec/config.yaml`, ADR-0018 (Decision and Consequences), and ADR-0016
  (re-dispatch scoping).
