# showcase/quality/dependency-management Specification

## Purpose

Defines how the build's dependency update reports are scoped and filtered: the Gradle `dependencyUpdates` report lists
only catalog-owned versions and lets the project opt in to suppressing major-version updates for specific coordinates,
or holding a coordinate back at a version line, while the remaining minor and patch updates stay visible; the web UI's
`npmOutdated` report lists its outdated npm dependencies, filtering out the packages whose majors the project defers;
and the `buildLogicUpdates` report lists the plugin pins the included `build-logic` build resolves — which the main
report cannot see — under the same stability rule and the same suppression files.

## Requirements

### Requirement: Dependency update reporting covers only catalog-owned versions

The build's `dependencyUpdates` task SHALL report available updates only for dependencies whose version is explicitly
declared in the version catalog (`gradle/libs.versions.toml`) via an exact `version.ref`. Dependencies whose version is
inherited from a BOM or constraint — and thus not decided by the project — SHALL NOT be reported. A catalog-owned
coordinate whose declared version is not older than the candidate SHALL NOT be reported when the plugin's current
version differs from it.

#### Scenario: Catalog-owned dependency reports updates

- **WHEN** a dependency coordinate has an exact `version.ref` in the version catalog and a newer version is available
- **THEN** the report lists the available update for that coordinate

#### Scenario: BOM-inherited dependency is not reported

- **WHEN** a dependency coordinate has no exact version in the version catalog (its version comes from a BOM or
  constraint)
- **THEN** the report does not list updates for that coordinate

#### Scenario: A coordinate whose reported current disagrees with the catalog is not reported

- **WHEN** a coordinate's exact `version.ref` version is the available candidate version or a newer one, while the
  plugin reports a lower "current version" for it — the signature of a current read from a constraint floor rather than
  from what the project takes
- **THEN** the report does not list an update for that coordinate

#### Scenario: A coordinate another surface holds below the catalog's is not reported either

- **WHEN** another surface governs a catalog-owned coordinate's resolved version below the version the catalog declares
  for it, while that declared version is not older than the candidate
- **THEN** the report does not list an update for that coordinate

#### Scenario: An up-to-date coordinate keeps its place in the up-to-date section

- **WHEN** a coordinate's exact `version.ref` version is also the version the plugin reports as current
- **THEN** the report does not list an update for it, and still reports it as using the latest release version

### Requirement: Dependency update reporting defaults to all updates for catalog-owned dependencies

The build's `dependencyUpdates` task SHALL report every available update for every catalog-owned dependency by default —
including major version upgrades — with no major-blocking configuration required.

#### Scenario: No blocking configuration is present

- **WHEN** a developer runs `./gradlew dependencyUpdates` and the major-disabled list is empty
- **THEN** the report lists available updates for all catalog-owned dependencies, including major version jumps

#### Scenario: A blocked coordinate still reports minor and patch updates

- **WHEN** a coordinate is listed in the major-disabled configuration and a same-major (minor or patch) version is
  available
- **THEN** the report lists that minor or patch update for the coordinate

### Requirement: Major updates can be suppressed per coordinate

The build SHALL provide an opt-in configuration listing coordinates whose major version updates are suppressed from the
`dependencyUpdates` report. Minor and patch updates for those coordinates SHALL remain reported. For calendar versioned
coordinates (versions following the Spring `YYYY.MINOR.MICRO` scheme, where the leading segment is a 4-digit year), a
change in the `YYYY.TRAIN` pair (the first two version segments) SHALL be treated as a major update — matching the
Spring release-train definition, where `2025.0` and `2025.1` are distinct trains — while a change only in the
service-release segment (the third segment) within the same train SHALL be treated as a non-major update. For
non-calendar (semver) coordinates, the existing leading-integer major comparison SHALL be unchanged.

#### Scenario: Major jump is hidden for a listed coordinate

- **WHEN** a coordinate is listed in the major-disabled configuration and a candidate version whose major exceeds the
  current major is the newest available
- **THEN** the report does not list that major-jump update for the coordinate

#### Scenario: Same-major fallback is reported when a major jump is the newest

- **WHEN** a listed coordinate has both a major-jump candidate and a same-major candidate available
- **THEN** the report lists the newest same-major candidate instead of the major-jump candidate

#### Scenario: Calendar-train change is classified as a major update

- **WHEN** a calendar-versioned coordinate (e.g. `io.projectreactor:reactor-bom` at `2025.0.7`) has a candidate whose
  `YYYY.TRAIN` pair differs from the current pair (e.g. `2025.1.x` or `2026.0.x`) is available
- **THEN** the report treats that train change as a major update for the coordinate

#### Scenario: Same-train service release is classified as a non-major update

- **WHEN** a calendar-versioned coordinate has a candidate with the same `YYYY.TRAIN` pair but a newer service release
  (e.g. `2025.0.8` vs `2025.0.7`) is available
- **THEN** the report treats that service-release change as a non-major update for the coordinate

#### Scenario: Semver coordinates keep the leading-integer major comparison

- **WHEN** a non-calendar (semver) coordinate is evaluated
- **THEN** a change in its leading integer is treated as a major update and a same-leading-integer change as a non-major
  update, unchanged from current behavior

### Requirement: Major-disabled entries are limited to majors the project cannot migrate

The major-disabled configuration SHALL only list coordinates whose major version the project cannot migrate to while its
ecosystem remains on the current major. The shipped list SHALL therefore include `org.jgroups`: JGroups is used only
through Axon's JGroups extension and the `jgroups-kubernetes` (KUBE_PING) discovery, both of which pin JGroups 4.x, so a
JGroups 5 migration is not actionable until those components support it. Minor and patch updates for these coordinates
SHALL remain reported.

#### Scenario: JGroups major jump is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of `org.jgroups:jgroups` or
  `org.jgroups.kubernetes:jgroups-kubernetes` whose major exceeds the current major (5.x vs 4.x) is available
- **THEN** the report does not list that major-jump update

#### Scenario: JGroups minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 4.x (same-major) version of `org.jgroups:jgroups` is
  available
- **THEN** the report lists that 4.x update

### Requirement: Flyway major updates are suppressed until the Spring Boot 4 migration

The shipped major-disabled configuration SHALL include `org.flywaydb` as a group prefix: Flyway major bumps (12.x, 13.x)
belong with the deferred Spring Boot 4 migration, because Spring Boot 3.5 (the current baseline, per ADR-0004) manages
Flyway 11.x, and even Spring Boot 4.0 manages Flyway 11.x — Flyway major 12 only appears with Spring Boot 4.1. Minor and
patch updates for these coordinates SHALL remain reported.

#### Scenario: Flyway major jump is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of `org.flywaydb:flyway-core` or
  `org.flywaydb:flyway-database-postgresql` whose major exceeds the current major (12.x/13.x vs 11.x) is available
- **THEN** the report does not list that major-jump update

#### Scenario: Flyway minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 11.x (same-major) version of `org.flywaydb:flyway-core`
  is available
- **THEN** the report lists that 11.x update

### Requirement: spring-data-opensearch major updates are suppressed until the Spring Boot 4 migration

The shipped major-disabled configuration SHALL include the exact coordinates
`org.opensearch.client:spring-data-opensearch`, `org.opensearch.client:spring-data-opensearch-starter`, and
`org.opensearch.client:spring-data-opensearch-testcontainers`: spring-data-opensearch 3.x is built on the Spring Data
2025.1 train and Spring Framework 7, which belong with the deferred Spring Boot 4 migration (per ADR-0004). Minor and
patch updates for these coordinates SHALL remain reported. The suppression SHALL NOT cover `opensearch-java` or
`opensearch-rest-client` (the transport clients in the same group), which are independent and remain reported for their
majors.

#### Scenario: spring-data-opensearch major jump is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of
  `org.opensearch.client:spring-data-opensearch` (or its `-starter`/`-testcontainers` variants) whose major exceeds the
  current major (3.x vs 2.x) is available
- **THEN** the report does not list that major-jump update

#### Scenario: spring-data-opensearch minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 2.x (same-major) version of
  `org.opensearch.client:spring-data-opensearch` is available
- **THEN** the report lists that 2.x update

#### Scenario: transport client majors remain reported

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of
  `org.opensearch.client:opensearch-java` or `org.opensearch.client:opensearch-rest-client` whose major exceeds the
  current major is available
- **THEN** the report lists that major-jump update

### Requirement: springdoc major updates are suppressed until the Spring Boot 4 migration

The shipped major-disabled configuration SHALL include the exact coordinate
`org.springdoc:springdoc-openapi-starter-webflux-ui`: springdoc 3.x is built against Spring Boot 4.x (both 3.0.3 on SB
4.0 and 3.1.0 on SB 4.1) and pulls the SB4-modularized auto-configuration artifacts, which belong with the deferred
Spring Boot 4 migration (per ADR-0004). Minor and patch updates for this coordinate SHALL remain reported.

#### Scenario: springdoc major jump is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of
  `org.springdoc:springdoc-openapi-starter-webflux-ui` whose major exceeds the current major (3.x vs 2.x) is available
- **THEN** the report does not list that major-jump update

#### Scenario: springdoc minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 2.x (same-major) version of
  `org.springdoc:springdoc-openapi-starter-webflux-ui` is available
- **THEN** the report lists that 2.x update

### Requirement: Axon Framework major updates are suppressed until the framework's 5.x dependency surface exists

The shipped major-disabled configuration SHALL include `org.axonframework` as a group prefix: the framework major is
deferred because the migration is not yet viable — the Kafka extension has no 5.x release, and AF4's JGroups distributed
command bus moved into AxonIQ's commercial offering in Axon Framework 5, so command routing needs a decision rather than
an upstream wait. The framework, Spring Boot, Reactor, Micrometer-metrics, and OpenTelemetry-tracing extensions DO have
stable 5.x under coordinates Axon renamed. The deferral and its reopen trigger are recorded in ADR-0011, which the
configuration's comment SHALL name. Minor and patch updates for these coordinates SHALL remain reported.

#### Scenario: The Axon framework major is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and an Axon Framework 5.x candidate is available for a pinned
  `org.axonframework` coordinate
- **THEN** the report does not list that major-jump update

#### Scenario: Axon minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 4.x (same-major) version of an `org.axonframework`
  coordinate is available
- **THEN** the report lists that 4.x update

#### Scenario: The Axon rationale resolves

- **WHEN** a reader follows the `org.axonframework` comment in the major-disabled configuration
- **THEN** it names ADR-0011, which records the deferral and the condition that reopens the migration

### Requirement: Web UI dependency update reporting

The build SHALL report available updates for the web UI's npm dependencies, separate from the catalog-owned Gradle
reporting: the web UI module SHALL expose an `npmOutdated` task that reports the packages whose resolved version is
behind the newest version available in the npm registry (as `npm outdated` reports them), using the project's pinned
Node. The task SHALL NOT be part of the `check` lifecycle, and it SHALL NOT fail when updates exist. The report SHALL
support a suppression list, `config/web-ui-updates/major-disabled.txt`, listing npm packages whose **major** updates are
suppressed: for a listed package the report SHALL list an update only when the newest version the manifest admits
(`npm outdated`'s `Wanted`) shares the newest published version's (`Latest`) leading integer — so a suppressed major is
dropped while that package's same-major updates stay reported. The shipped list SHALL include `typescript`, deferred
because `typescript-eslint` caps TypeScript below the next major, and the list's comment SHALL name the package and
point at this rationale. A row the report cannot parse SHALL be kept unchanged rather than dropped.

#### Scenario: Web UI dependency updates are reported

- **WHEN** a developer runs `./gradlew :showcase-web-ui:npmOutdated` and an npm dependency has a newer version available
- **THEN** the report lists that package with its current, wanted, and latest versions

#### Scenario: Available updates do not fail the task

- **WHEN** the web UI has outdated npm dependencies (so `npm outdated` exits non-zero)
- **THEN** the `npmOutdated` task still completes and its output is available to be reported

#### Scenario: Normal build does not run the web UI update report

- **WHEN** a developer runs `./gradlew check` or any build task other than `npmOutdated`
- **THEN** the web UI update report does not run

#### Scenario: A suppressed package's major update is dropped

- **WHEN** a package is listed in the suppression list and its newest published version is a new major (the manifest's
  admitted version shares the current major)
- **THEN** the report does not list that major-only update for the package

#### Scenario: A suppressed package's same-major update stays reported

- **WHEN** a package is listed in the suppression list and a newer same-major version is available
- **THEN** the report lists that same-major update for the package

#### Scenario: A package not listed is reported unchanged

- **WHEN** a package is absent from the suppression list and any newer version is available
- **THEN** the report lists it, whether the update is major, minor, or patch

### Requirement: A dependency can be held back at a version line

The build SHALL provide an opt-in configuration listing coordinates held back at a version line — the `major.minor` of
the last line the project can take. For a listed coordinate, the `dependencyUpdates` report SHALL NOT list candidates on
a newer version line within the same major version, while candidates within the held line (its patches) SHALL remain
reported, and candidates whose major version exceeds the held line's SHALL remain reported under the major-update policy
rather than the hold-back. The shipped hold-back configuration SHALL hold `org.opensearch.client:opensearch-java` at
`3.9`: `3.10.0` changes `Hit.matchedQueries()`'s return type from `List` to `MatchedQueries`, binary-incompatible with
`spring-data-opensearch` 2.x's `DocumentAdapters.from`, and supporting it rides `spring-data-opensearch` 3.x — the
deferred Spring Boot 4 migration (ADR-0004). The configuration's comment SHALL name the coordinate and point at this
rationale.

#### Scenario: A newer minor line is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a coordinate held back at `3.9` has a `3.10.0` candidate
  available
- **THEN** the report does not list that newer-minor candidate

#### Scenario: Patches within the held line stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a coordinate held back at `3.9` has a `3.9.1` candidate
  available
- **THEN** the report lists that `3.9.1` update

#### Scenario: A major jump stays reported

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a coordinate held back at `3.9` has a `4.0.0` candidate
  available
- **THEN** the report lists that major-jump update, because the hold-back covers newer minor lines only

#### Scenario: The shipped list holds opensearch-java

- **WHEN** a reader opens the hold-back configuration
- **THEN** it holds `org.opensearch.client:opensearch-java` at `3.9` with a comment naming the coordinate and pointing
  at the rationale recorded in this spec

### Requirement: Spring major updates are suppressed until the Spring Boot 4 migration

The shipped major-disabled configuration SHALL include `org.springframework` as a group prefix: a Spring Framework major
bump (7.x vs 6.x) belongs with the deferred Spring Boot 4 migration, because Spring Boot 3.5 (the current baseline, per
ADR-0004) manages Spring Framework 6.x, so a Spring Framework 7 migration is not actionable until that coordinated
migration is taken. The configuration's comment SHALL name the deferral and ADR-0004. Minor and patch updates for these
coordinates SHALL remain reported.

#### Scenario: Spring major jump is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of an `org.springframework:*`
  coordinate whose major exceeds the current major (7.x vs 6.x) is available
- **THEN** the report does not list that major-jump update

#### Scenario: Spring minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 6.x (same-major) version of an `org.springframework:*`
  coordinate is available
- **THEN** the report lists that 6.x update

### Requirement: Build-logic pin dependency update reporting

The build SHALL report available updates for the Gradle plugin versions the included `build-logic` build declares in its
own dependencies — the pins that resolve only on that build's buildscript classpath, and are therefore invisible to the
main build's `dependencyUpdates` report — through a `buildLogicUpdates` task that looks each declared pin up in the
registry that publishes it. The task SHALL NOT be part of the `check` lifecycle and SHALL NOT fail when updates exist.

#### Scenario: A build-logic pin behind a newer stable release is reported

- **WHEN** a coordinate the included build declares has a newer stable release than the version the catalog pins
- **THEN** the report lists that pin with its pinned and available versions

#### Scenario: An unread lookup is reported rather than read as current

- **WHEN** the registry returns no version for a declared pin
- **THEN** the report names that pin as unread instead of treating it as up to date

#### Scenario: Normal build does not run the build-logic pin report

- **WHEN** a developer runs `./gradlew check` or any build task other than `buildLogicUpdates`
- **THEN** the build-logic pin report does not run

### Requirement: Build-logic pin reporting applies the Gradle report's filtering rules

The build-logic pin report SHALL apply the same stability rule and the same suppression files as the Gradle report, so a
pre-release, a major the project defers, and a version line the hold-back list holds are not surfaced as an update,
while the newest release those rules accept is reported.

#### Scenario: A pre-release or a deferred major is not reported

- **WHEN** a declared pin's newest published release is a pre-release, or a major the suppression list defers, or a
  version line the hold-back list holds
- **THEN** the report lists no update for that release, and instead reports the newest release the rules accept

### Requirement: Build-logic pin coverage is asserted against the included build

The `buildLogicUpdates` task SHALL declare its pins as the catalog aliases the included build consumes, and SHALL fail
when that declared set is not exactly the set of plugin aliases the included build's own build script consumes, so a pin
added there cannot go uncovered.

#### Scenario: A pin the included build no longer consumes fails the check

- **WHEN** the declared pins and the plugin aliases the included build's build script consumes are not the same set
- **THEN** the task fails, naming the alias that is unlisted, no longer consumed, or declared twice
