# Proposal: Remove the redundant spring-data-bom catalog pin

## Why

`spring-data-bom` is catalog-owned (`2025.0.13`) and imported by the `platform`, but Spring Boot 3.5.16's own
`spring-boot-dependencies` BOM already imports the same spring-data-bom at the same version. The pin is fully
redundant: it has tracked the SB-aligned `2025.0.x` train in lockstep since the initial commit, so it adds no explicit
control and only surfaces a misleading row in `dependencyUpdates` — the `2025.0.13 -> 2025.1.7` bump is the Spring Boot
4 train, which the project cannot take on SB 3.5.

## What Changes

- `gradle/libs.versions.toml`: remove the `spring-data-bom` version entry and its `[libraries]` coordinate.
- `platform/build.gradle.kts`: remove `api(platform(libs.spring.data.bom))`.
- `spring-data-commons` and `spring-data-jdbc` catalog entries stay — they are used directly by services and resolve
  their versions from the SB-imported spring-data-bom.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. No spec-level requirement changes: the `dependency-management` capability already specifies that BOM-inherited
dependencies (not catalog-owned) are not reported. This change makes `spring-data-bom` BOM-inherited, matching that
existing requirement. `skip_specs: true`.

## Impact

- **Code**: `gradle/libs.versions.toml` (two lines removed), `platform/build.gradle.kts` (one line removed).
- **Build**: Spring Data artifacts continue to resolve to `2025.0.13` (from the SB-imported BOM); `dependencyUpdates`
  stops listing `spring-data-bom [2025.0.13 -> 2025.1.7]` because it is no longer catalog-owned.
- **Tests**: no test changes; verification runs `dependencyUpdates` and confirms the row is gone while Spring Data
  resolution is unchanged.