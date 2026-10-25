# Proposal

## Why

The buildpack update check names two rows: `paketo-builder-jammy-base` `0.4.667`→`0.4.669` and `paketo-nginx`
`1.3.0`→`1.3.3`, re-resolved by the check's own run rather than from the weekly tracker issue. The builder and the
buildpacks it bundles must move together, and this time they can: `pack builder inspect` on `0.4.669` lists nginx
**1.3.3** and procfile **5.15.5**. The nginx hold `bump-buildpacks-2026-10-22` parked is over, so both rows are taken as
one matched set.

## What Changes

- **`gradle/libs.versions.toml`** — `paketo-builder-jammy-base` `0.4.667`→`0.4.669`, `paketo-nginx` `1.3.0`→`1.3.3`;
  `paketo-procfile` stays `5.15.5`, the version the new builder bundles.
- **`.opencode/skills/deployment-operations/SKILL.md`** — the documented builder and nginx pins move with the catalog
  (`verifyDocumentedPins`, part of `check`, asserts them against it). The documented procfile pin is unchanged.
- **`build-logic/src/main/kotlin/DocumentedPinRules.kt`** — the KDoc's illustrative builder pin moves with the catalog.
- **`docs/ideas.md`** — the parked `paketo-nginx 1.3.3` entry is removed, its revisit condition met.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None (`skip_specs`): a paired version bump. `showcase/quality/infra-image-versions` already requires the catalog to
declare the Paketo builder and buildpacks as pinned concrete coordinates and a live doc to state those pins at their
catalog versions; this change satisfies that wording — take the pair the builder bundles, move the documented pins —
without altering it. `showcase/deployment/web-ui` is unchanged too, since the image still serves the bundle through
nginx. No scenario's outcome moves.

## Impact

- **The web UI image only.** Verification is a real build plus a run of the built image, reading nginx's architecture
  from the buildpack's own layer, as the pack requires.
- **The update report clears both rows** — nothing is held this time, so no parked entry replaces the one this change
  removes.
