# Proposal

## Why

The buildpack update report names three rows: `paketo-builder-jammy-base: 0.4.662 -> 0.4.667`,
`paketo-nginx: 1.3.0 -> 1.3.3`, and `paketo-procfile: 5.15.4 -> 5.15.5`. Two are taken and one is held, because the
builder and the buildpacks it bundles are one pair: `pack builder inspect paketobuildpacks/builder-jammy-base:0.4.667`
lists **nginx 1.3.0** and procfile up to **5.15.5**, so pinning nginx `1.3.3` would make `pack` add a buildpackage the
builder does not carry, which resolves to an arm64 slice even though the image is amd64-only — the mismatch that fails
with `exec: nginx: not found`. The builder, the procfile bump and the existing nginx pin form the matched set.

## What Changes

- **`gradle/libs.versions.toml`** — `paketo-builder-jammy-base` `0.4.662` → `0.4.667`, `paketo-procfile` `5.15.4` →
  `5.15.5`; `paketo-nginx` **stays `1.3.0`**, the version the new builder bundles.
- **`.opencode/skills/deployment-operations/SKILL.md`** — the documented builder and procfile pins move with the catalog
  (`verifyDocumentedPins`, part of `check`, asserts them). The documented nginx pin is unchanged because the pin is.
- **`docs/ideas.md`** — a parked entry records the held `paketo-nginx 1.3.3` and the retrigger: the next pinned builder
  that lists it.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None (`skip_specs`): `showcase/quality/infra-image-versions` owns the behaviour this change performs — the pack states
the catalog's Paketo pins and `check` verifies them — and this bump keeps that scenario satisfied by moving the
documented pins with the catalog; the `deployment/web-ui` outcome is unchanged too, since the image still serves the
bundle through nginx. No scenario's outcome moves.

## Impact

- **The web UI image only.** The verification is a real build plus a run of the built image, reading nginx's
  architecture from the buildpack's own layer, as the pack requires.
- **The update report keeps one row open** (`paketo-nginx 1.3.0 -> 1.3.3`) until a builder bundles it; the parked entry
  is where that is tracked.
