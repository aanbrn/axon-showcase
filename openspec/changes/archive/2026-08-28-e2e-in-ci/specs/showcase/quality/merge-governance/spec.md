# showcase/quality/merge-governance — Delta Spec

## ADDED Requirements

### Requirement: End-to-end tests run on a schedule and on demand

The end-to-end test suite SHALL run automatically on a nightly schedule and be manually triggerable, as the same
`e2e` job in a dedicated workflow separate from the merge gate. It SHALL build all four service images and boot the
full pipeline (PostgreSQL, Kafka, OpenSearch, and the four services) via the existing
`:showcase-api-gateway:e2eTest` task, SHALL run on `ubuntu-latest` with a Temurin JDK 21, and SHALL NOT be part of
the merge-gate `build` check or a required check for merging into `main`.

#### Scenario: Nightly schedule triggers the e2e suite

- **WHEN** the scheduled nightly trigger fires
- **THEN** the `e2e` job runs `./gradlew :showcase-api-gateway:e2eTest`, building all four service images and booting
  the full pipeline

#### Scenario: Manual trigger runs the e2e suite

- **WHEN** a maintainer dispatches the e2e workflow manually
- **THEN** the `e2e` job runs the same full end-to-end suite against the current `main`

#### Scenario: The e2e suite is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the e2e run is not required, because it is not part of the merge-gate `build` check and no ruleset requires
  it