# Proposal: Scope dependency update reporting to catalog-owned versions with major blocking

## Why

The `dependencyUpdates` task reports the newest version of every resolved dependency, but two things make the report
noisy: (1) it lists updates for BOM-inherited modules whose version the project does not control (e.g. `spring-tx`,
`spring-boot-starter-*`, `micrometer-registry-*`), and (2) it reports major jumps for libraries the project
deliberately pins to a major it cannot migrate away from (e.g. Axon 4.x, Spring Boot 3.x). We need the report to cover
only dependencies whose version the project explicitly decides, and to suppress major updates for a per-coordinate
opt-in list — while keeping minor/patch updates visible for those same coordinates.

## What Changes

- `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts`: extend the `DependencyUpdatesTask`
  configuration with two rules:
  - an ownership filter that keeps only dependencies whose coordinate has an exact version (`version.ref`) in
    `gradle/libs.versions.toml` — BOM-inherited modules are not reported;
  - a major-blocking rule that reads a list of coordinates (from a new config file) and rejects a candidate version
    when `majorOf(candidate) > majorOf(currentVersion)` for a listed coordinate. Coordinates not in the list are
    reported normally, including major updates.
- `config/dependency-updates/major-disabled.properties`: new opt-in list, shipped with the initial entries
  `org.axonframework` and `org.springframework`. Each line names one coordinate (or group prefix) whose major updates
  are suppressed; minor and patch updates for those coordinates remain reported.
- The existing non-stable (`isNonStable`) rejection rule is unchanged.

## Capabilities

### New Capabilities

- `showcase/quality/dependency-management`: the build's dependency-update reporting — the default "report updates for
  catalog-owned versions only" behavior, and the opt-in mechanism to suppress major updates for named coordinates.

### Modified Capabilities

None. This extends the quality tooling, not service behavior.

## Impact

- **Code**: `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts` (modified),
  `config/dependency-updates/major-disabled.properties` (new).
- **Docs**: `AGENTS.md` (Build & Test commands), `README.md` (dependency-update reporting note).
- **Build**: `./gradlew dependencyUpdates` output now covers only catalog-owned coordinates; with the shipped
  major-disabled entries, Axon (`org.axonframework*`) and Spring (`org.springframework*`) major jumps are suppressed
  while their minor/patch updates and all other catalog-owned majors remain reported.
- **Tests**: no unit/integration test changes; verification runs `dependencyUpdates` before and after adding a sample
  coordinate and checks the major jump disappears while a minor update is still reported.