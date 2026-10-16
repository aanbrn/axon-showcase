# Proposal

## Why

The 2026-10-16 `buildpackUpdates` report names three rows: `paketo-builder-jammy-base` `0.4.659`→`0.4.662`,
`paketo-nginx` `1.3.0`→`1.3.2`, and `paketo-procfile` `5.15.3`→`5.15.4`. **The builder and the buildpacks it bundles
must move together**, so the report's rows are not a to-do list: inspecting the new builder
(`pack builder inspect paketobuildpacks/builder-jammy-base:0.4.662`) shows it bundles **nginx `1.3.0`** — _not_ the
`1.3.2` the registry publishes — and **procfile `5.15.4`**. The paired bump is therefore the builder plus procfile, with
nginx deliberately held at the builder's bundled version, exactly as the 2026-10-12 bump of the same pair did.

## What Changes

- **`gradle/libs.versions.toml`** — `paketo-builder-jammy-base` `0.4.659`→`0.4.662` and `paketo-procfile`
  `5.15.3`→`5.15.4`, both paired with what the new builder lists.
- **`paketo-nginx` stays `1.3.0`** — the builder bundles that version; the report's `1.3.2` is a registry release the
  builder does not carry, and a pin absent from the builder makes `pack` add it from the registry and resolve the added
  buildpackage to an **arm64** slice (a mismatch that fails at `exec: nginx: not found`). This is the documented pairing
  rule, not a deferral.
- **`AGENTS.md`** — the buildpack-pin gotcha names the pins as their current versions, so it moves with them
  (`procfile@5.15.4`, `builder-jammy-base:0.4.662`; `nginx@1.3.0` unchanged), because `verifyDocumentedPins` (part of
  `check`) asserts each against its catalog coordinate.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. A paired version bump; no specified behavior changes. `showcase/quality/infra-image-versions`
already owns this case — it requires the version catalog to declare the Paketo builder and buildpacks as pinned concrete
coordinates, and holds the requirement that a live doc states those pins at their catalog versions, and states no
version this change makes stale. The pairing rule itself — take the pins from the builder's own bundle list — is
recorded in the `AGENTS.md` buildpack-pin gotcha, not in the spec, and this change follows it. This change satisfies
that wording (bump the pair the builder bundles, move the documented pins) without altering it, so no delta is owed —
not that no capability covers it.

## Impact

- Files: `gradle/libs.versions.toml`, `AGENTS.md`.
- **Verified by building and running the image**, not by a green build: the web UI image is built with the new builder
  and started, its `GET /` served, and nginx's architecture read out of the buildpack's own layer (the amd64/AArch64
  trap that a successful build with a wrong-architecture binary hides).
- The `paketo-nginx` row remains in the `buildpackUpdates` report by design; no suppression list covers buildpacks, and
  the row's clearance is the builder bundling that version.
