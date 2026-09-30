# Spec Delta: showcase/quality/dependency-management

## ADDED Requirements

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
