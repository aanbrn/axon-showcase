## ADDED Requirements

### Requirement: Command-service saga and snapshot environment

The chart SHALL wire the command-service saga cache, saga associations cache, and showcase snapshot trigger settings
from values to environment variables, mirroring the existing `showcaseCache` wiring, so the settings are tunable per
deployment.

#### Scenario: Saga cache settings are passed as environment

- **WHEN** a command-service Deployment is rendered
- **THEN** it receives the saga cache settings as the `SAGA_CACHE_MAX_SIZE`, `SAGA_CACHE_EXPIRES_AFTER_ACCESS`, and
  `SAGA_CACHE_EXPIRES_AFTER_WRITE` environment variables from the `commandService.sagaCache` values

#### Scenario: Saga associations cache settings are passed as environment

- **WHEN** a command-service Deployment is rendered
- **THEN** it receives the saga associations cache settings as the `SAGA_ASSOCIATIONS_CACHE_MAX_SIZE`,
  `SAGA_ASSOCIATIONS_CACHE_EXPIRES_AFTER_ACCESS`, and `SAGA_ASSOCIATIONS_CACHE_EXPIRES_AFTER_WRITE` environment
  variables from the `commandService.sagaAssociationsCache` values

#### Scenario: Showcase snapshot trigger settings are passed as environment

- **WHEN** a command-service Deployment is rendered
- **THEN** it receives the showcase snapshot trigger setting as the `SHOWCASE_SNAPSHOT_TRIGGER_LOAD_TIME_THRESHOLD`
  environment variable from the `commandService.showcaseSnapshotTrigger` values