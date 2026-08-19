# Proposal: Rename the platform capability to extensions

## Why

The `showcase/platform` capability umbrella holds the extension libraries (identifier, MapStruct, Resilience4j), but
"platform" is a poor label: it collides with the `:platform` Gradle module (the Java BOM for dependency management), and
it connotes a runtime infrastructure tier rather than a collection of framework-integration libraries.

## What Changes

- Rename the spec capability `showcase/platform` → `showcase/extensions`.
- Move the three leaf capability specs accordingly:
  - `showcase/platform/identifier-extension/spec.md` → `showcase/extensions/identifier-extension/spec.md`
  - `showcase/platform/mapstruct-extension/spec.md` → `showcase/extensions/mapstruct-extension/spec.md`
  - `showcase/platform/resilience4j-extension/spec.md` → `showcase/extensions/resilience4j-extension/spec.md`
- Pure spec-path reorganization; no requirement content changes. AGENTS.md references the `platform` role in its
  architectural-roles list, so that list is updated to `extensions` too (the `:platform` BOM module references stay).

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — path-only rename, no requirement changes; `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- Spec directories under `openspec/specs/showcase/` renamed; no code, behavior, or build changes.
