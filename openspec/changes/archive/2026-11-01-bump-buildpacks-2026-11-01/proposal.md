# Proposal

## Why

The buildpack update check names five rows, re-resolved by the check's own run rather than taken from the weekly tracker
issue: `paketo-builder-jammy-base` `0.4.669`→`0.4.675`, `paketo-builder-noble-java-tiny` `0.0.200`→`0.0.205`,
`paketo-health-checker` `2.14.0`→`2.14.1`, `paketo-nginx` `1.3.3`→`1.3.4`, and `paketo-procfile` `5.15.5`→`5.15.6`.

A builder and the buildpacks it bundles must move together, and both builders can move this time: `pack builder inspect`
on `builder-jammy-base:0.4.675` lists nginx **1.3.4** and procfile **5.15.6**, and on `builder-noble-java-tiny:0.0.205`
it lists lifecycle `0.21.26` over the bundled Java buildpacks. The health-checker is not bundled by the JVM builder, so
its `docker://` image pin moves on its own.

## What Changes

- **`gradle/libs.versions.toml`** — `paketo-builder-jammy-base` `0.4.669`→`0.4.675`, `paketo-builder-noble-java-tiny`
  `0.0.200`→`0.0.205`, `paketo-health-checker` `2.14.0`→`2.14.1`, `paketo-nginx` `1.3.3`→`1.3.4`, `paketo-procfile`
  `5.15.5`→`5.15.6`.
- **`.opencode/skills/deployment-operations/SKILL.md`** — the documented builder, nginx, procfile, and health-checker
  pins move with the catalog (`verifyDocumentedPins`, part of `check`, asserts them against it).
- **`build-logic/src/main/kotlin/DocumentedPinRules.kt`** — the KDoc's illustrative builder version is de-versioned to
  the placeholder form the same sentence already states, so the illustration no longer re-stales at each bump.

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
