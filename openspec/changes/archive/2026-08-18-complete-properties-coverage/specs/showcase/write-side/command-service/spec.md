## ADDED Requirements

### Requirement: Configurable caching and snapshotting

The system SHALL expose the aggregate caches and the showcase snapshot trigger through the `showcase.command`
configuration properties: the showcase, saga, and saga-associations caches each with a maximum size and access and
write expiry durations, and the snapshot trigger with a load time threshold. The properties SHALL default to a maximum
size of 1000, an access expiry of PT10M, a write expiry of PT5M, and a snapshot load time threshold of PT0.5S, and SHALL
be overridable through environment variables.

#### Scenario: Default cache configuration applies

- **WHEN** no cache or snapshot properties are set
- **THEN** the showcase, saga, and saga-associations caches use a maximum size of 1000, an expires-after-access of
  PT10M, and an expires-after-write of PT5M, and the showcase snapshot trigger uses a load time threshold of PT0.5S

#### Scenario: Saga cache is overridable through environment

- **WHEN** the saga cache maximum size is set through the `SAGA_CACHE_MAX_SIZE` environment variable
- **THEN** the saga cache uses that maximum size while its expiry durations remain at their defaults

#### Scenario: Saga associations cache is overridable through environment

- **WHEN** the saga associations cache maximum size is set through the `SAGA_ASSOCIATIONS_CACHE_MAX_SIZE` environment
  variable
- **THEN** the saga associations cache uses that maximum size while its expiry durations remain at their defaults

#### Scenario: Showcase snapshot trigger is overridable through environment

- **WHEN** the showcase snapshot trigger load time threshold is set through the
  `SHOWCASE_SNAPSHOT_TRIGGER_LOAD_TIME_THRESHOLD` environment variable
- **THEN** the showcase snapshot trigger uses that load time threshold
