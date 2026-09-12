## ADDED Requirements

### Requirement: Time limiter

The system SHALL enforce a timeout on command operations: an operation that does not complete within the configured
timeout SHALL fail with a timeout error.

#### Scenario: Slow dispatch times out

- **WHEN** the command service responds slower than the configured timeout
- **THEN** the operation fails with a timeout error
