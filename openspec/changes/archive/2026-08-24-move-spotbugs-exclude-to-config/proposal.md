# Proposal: Move the SpotBugs exclude filter to config/spotbugs/

## Why

The repo's static-analysis configuration already follows a `config/<tool>/` convention (`config/dependency-updates/`,
`config/jacoco/`, `config/checkstyle/`), but `spotbugs-exclude.xml` sits at the repository root — the odd one out.
Unlike Lombok's `lombok.config` (whose location is load-bearing because it is discovered by walking up from sources),
SpotBugs filters are referenced explicitly by path from `code-check-conventions.gradle.kts`, so relocating them is a
pure tidiness change with no behavioral effect.

## What Changes

- Move `spotbugs-exclude.xml` to `config/spotbugs/spotbugs-exclude.xml`.
- Update `code-check-conventions.gradle.kts` so both the exclude and include filter lookups point at
  `config/spotbugs/` (the include lookup stays dormant — no `spotbugs-include.xml` exists today, but the symmetric
  "if present" check keeps working).
- Update the `AGENTS.md` SpotBugs bullet to reference the new location.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. No spec-level behavior change; this is configuration relocation. `skip_specs: true`.

## Impact

- **Code**: `config/spotbugs/spotbugs-exclude.xml` (moved), `build-logic/src/main/kotlin/code-check-conventions.gradle.kts`
  (filter paths), `AGENTS.md` (SpotBugs bullet).
- **Docs**: `AGENTS.md` updated as part of this change.
- **Build**: no behavioral change; SpotBugs exclusion semantics identical after the move.
- **Tests**: no test changes.