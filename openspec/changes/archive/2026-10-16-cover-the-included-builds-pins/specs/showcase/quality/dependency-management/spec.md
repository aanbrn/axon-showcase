## ADDED Requirements

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
