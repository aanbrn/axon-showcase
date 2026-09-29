# ADR-0015: Single-source build-tool versions in the version catalog

Date: 2025-07-04

Status: Accepted

## Context

The build's convention plugins (`build-logic`) pin tool versions for the whole build: the runtime `node` and Java
versions, the `checkstyle`/`spotbugs`/`jacoco` plugin `toolVersion`s, the ErrorProne/NullAway and protobuf artifacts,
the Paketo builder and buildpack ids, and image tags. Hard-coding a version inside a plugin would scatter the same
coordinate across several files and let the pins drift.

The arrangement — every version declared once in `gradle/libs.versions.toml` and read by the plugins — predates the ADR
practice (ADR-0001, 2026-08-18): it is present from the initial commit (`5ff1a0c`, 2025-07-04), whose convention plugins
already read the catalog. It was recorded only as an `AGENTS.md` convention; whether it deserves an ADR was raised by
the 2026-09-20 architecture audit, repeated 2026-09-21, and carried forward by the 2026-09-28 audit. Recorded
retrospectively on 2026-09-29.

## Decision

Declare every build-tool version — `node`, the Java runtime, plugin `toolVersion`s, the ErrorProne/NullAway and protobuf
artifacts, buildpack ids, and image tags — once in `gradle/libs.versions.toml`, and read it from a `build-logic`
convention plugin via `the<LibrariesForLibs>()` / `libs.versions.<name>.get()`. A convention plugin never hard-codes a
tool version.

A version that is not a `group:name` dependency is a `[versions]`-only entry with no `[libraries]` module — a buildpack
id (`paketo-nginx`), a builder tag (`paketo-builder-jammy-base`), or `node`.

Catalog ownership is **single-sourcing, not update tracking**: a bare `[versions]` entry is not resolved as a
dependency, so `dependencyUpdates` does not see it, and the dedicated checks together with a hand-audit remainder cover
the rest — the split `AGENTS.md`'s convention and the `dependency-management` and `merge-governance` specs record.

Alternatives considered and rejected: inline version literals in each convention plugin (the same coordinate repeated in
every plugin, free to drift); a `gradle.properties` file or a constants class of versions (moves the single copy off the
dependency-management surface that resolves catalog aliases, so a plugin would read two sources); and declaring each
tool as a resolved dependency so `dependencyUpdates` covers everything (forces a dependency edge where none exists — a
buildpack id is not a Maven coordinate).

## Consequences

- A version bump is one edit in the catalog; every plugin that reads the entry follows, and no plugin can silently
  diverge.
- Adding a `[versions]`-only entry means checking which (if any) update checker covers it, since that surface is split;
  an entry outside all of them is a hand-audit remainder.
- A convention plugin that needs a version must go through the catalog even when the value looks local, which keeps the
  rule visible by reading the plugin.
