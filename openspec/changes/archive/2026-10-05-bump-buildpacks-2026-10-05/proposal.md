# Proposal

## Why

The weekly `buildpack-updates` check (tracker #211, the 2026-10-04 run) flagged newer Paketo coordinates: the builder
and the two buildpacks the web UI image uses. Keeping them current is the routine hygiene the check exists to surface,
and the builder and the buildpacks it bundles must move as a set.

## What Changes

- `gradle/libs.versions.toml` — `paketo-builder-jammy-base` 0.4.649 → 0.4.653, `paketo-nginx` 1.2.2 → 1.3.0,
  `paketo-procfile` 5.15.1 → 5.15.2, bumped together.
- `AGENTS.md` — the sites that spell out the pins (the buildpack list, the builder pin, and the alias-tag semver
  example) are refreshed to match; the mismatched-pair example is re-worded version-agnostically (it previously named
  the now-superseded `1.2.0`/`1.2.1` pair).

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — a pure buildpack-pin bump (`.openspec.yaml` sets `skip_specs: true`); no requirement changes.

## Impact

- `gradle/libs.versions.toml` (the three Paketo pins).
- `AGENTS.md` — the pin references refreshed.
- The web UI image build (`:showcase-web-ui:dockerBuildImage`), which passes the pins to `pack`.
- No source, application, or runtime behavior change.
