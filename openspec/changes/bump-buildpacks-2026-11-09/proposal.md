# Proposal

## Why

The weekly buildpack check reports five rows, re-resolved by the check's own run rather than taken from the tracker
issue: both Paketo builders, the health-checker buildpack, and the web UI's nginx and procfile buildpacks. A builder and
the buildpacks it bundles must move together, so taking the builders alone would leave a pin a line behind its builder.

## What Changes

- **`gradle/libs.versions.toml`** — `paketo-builder-jammy-base` `0.4.675`→`0.4.681`, `paketo-builder-noble-java-tiny`
  `0.0.205`→`0.0.211`, `paketo-health-checker` `2.14.1`→`2.14.2`, `paketo-nginx` `1.3.4`→`1.3.5`, `paketo-procfile`
  `5.15.6`→`5.15.7`.
- **`.opencode/skills/deployment-operations/SKILL.md`** — the documented builder, nginx, procfile, and health-checker
  pins move with the catalog (`verifyDocumentedPins`, part of `check`, asserts them against it).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None (`skip_specs`): a paired version bump. `showcase/quality/infra-image-versions` already requires that pack to state
the catalog's Paketo pins at their catalog versions and `check` to verify them, and ADR-0015 already places buildpack
ids and builder tags in the version catalog; this change satisfies both without altering either. No scenario's outcome
moves.

## Impact

- **The web UI image and the four JVM service images.** The web UI image moves its jammy builder plus its nginx and
  procfile buildpacks; the JVM images move their noble builder plus the health-checker image reference.
- **The update report clears all five rows** — nothing is held this time, so no parked entry replaces them.
