# Proposal

## Why

The 2026-10-12 `buildpackUpdates` report names three rows: `paketo-builder-jammy-base` `0.4.653`→`0.4.659`,
`paketo-nginx` `1.3.0`→`1.3.1`, and `paketo-procfile` `5.15.2`→`5.15.3`. **The builder and the buildpacks it bundles
must move together**, so the report's rows are not a to-do list: inspecting the new builder
(`pack builder inspect paketobuildpacks/builder-jammy-base:0.4.659`) shows it bundles **nginx `1.3.0`** — _not_ the
`1.3.1` the registry publishes — and **procfile `5.15.3`**. The paired bump is therefore the builder plus procfile, with
nginx deliberately held at the builder's bundled version.

## What Changes

- **`gradle/libs.versions.toml`** — `paketo-builder-jammy-base` `0.4.653`→`0.4.659` and `paketo-procfile`
  `5.15.2`→`5.15.3`, both paired with what the new builder lists.
- **`paketo-nginx` stays `1.3.0`** — the builder bundles that version; the report's `1.3.1` is a registry release the
  builder does not carry, and a pin absent from the builder makes `pack` add it from the registry and resolve the added
  buildpackage to an **arm64** slice (a mismatch that fails at `exec: nginx: not found`). This is the documented pairing
  rule, not an ignored update.
- **`AGENTS.md`** — the buildpack-pin gotcha names the pins as their current versions, so it moves with them
  (`procfile@5.15.3`, `builder-jammy-base:0.4.659`; `nginx@1.3.0` unchanged).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. The bump changes no specified behavior — the web UI image's contract (serving the bundle on
`8080`, the nginx `stub_status` metrics) is unchanged; only the toolchain that builds it moves.

## Impact

- Files: `gradle/libs.versions.toml`, `AGENTS.md`.
- Verified by **running** the built image, not by a green build or a metadata label: `docker run`, `GET /`, and the
  nginx binary's architecture — read from its layer's ELF header — on a `linux/amd64` image under an arm64 host.
- The report will keep naming `paketo-nginx 1.3.0 -> 1.3.1`; that row is not actionable while the builder bundles
  `1.3.0`.
