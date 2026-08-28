# Enforce Import Quality in Build Gates

## Why

Palantir-java-format — the formatter behind `spotlessApply` — does not manage imports at all: it never reorders,
never removes unused, and never expands wildcard imports. As a result, a file with `import x.*;` (or
misordered/unused imports) passes `spotlessCheck` and only gets caught by checkstyle's `AvoidStarImport` *after* the
fact. The `ShowcaseProjectorIT` episode demonstrated it: IDEA's native Optimize Imports collapsed seven static
imports into a wildcard locally, `spotlessApply` was a no-op on the corrupted file, and only checkstyle caught it —
no formatter could fix it. The committed tree was unaffected (no wildcards in HEAD), but the gate gap is real: without
a committed code-style scheme, IDEA keeps reintroducing wildcards on its default thresholds (5 classes / 3 static
names) and nothing in the build auto-heals imports.

## What Changes

- Add Spotless Java import steps so `spotlessApply` becomes self-healing for imports:
  - `importOrder()` — sorts imports into the repo's canonical groups (static → blank → others), matching the IDEA
    `IMPORT_LAYOUT_TABLE`
  - `removeUnusedImports()` — removes unused imports
  - `forbidWildcardImports()` — fail fast on `import x.*;` in the build (cheap alternative to `expandWildcardImports`,
    which is resource-intensive and recommended only while a codebase is being cleaned)
- Commit the IDEA project code-style scheme settings that prevent native import collapse:
  - `USE_SINGLE_CLASS_IMPORTS=true`
  - `CLASS_COUNT_TO_USE_IMPORT_ON_DEMAND=999` and `NAMES_COUNT_TO_USE_IMPORT_ON_DEMAND=999`

## Capabilities

### New Capabilities

(none — tooling/build-convention change only; `skip_specs: true` is set in `.openspec.yaml`)

### Modified Capabilities

(none)

## Impact

- **Build**: `build-logic/src/main/kotlin/code-check-conventions.gradle.kts` — the Spotless `java` block gains the
  three import steps. This changes what `spotlessApply` rewrites and what `spotlessCheck` enforces for every module.
- **IDE config**: `.idea/codeStyles/Project.xml` — committed scheme forces single-class imports; needs an IDEA
  restart to reload the scheme.
- **No dependency changes**: all three Spotless steps are built into the already-resolved
  `com.diffplug.spotless:spotless-plugin-gradle:8.10.0` (verified via `javap` on the resolved jar).
- **Notes**: `forbidWildcardImports()` replaces the checkstyle `AvoidStarImport` gate (kept as a belt-and-braces
  safety net); `expandWildcardImports()` is deliberately **not** used because it requires full-classpath resolution via
  JavaParser per file and is documented by Spotless as a transitional step.