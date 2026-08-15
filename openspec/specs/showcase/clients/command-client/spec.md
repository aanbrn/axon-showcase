# showcase/clients/command-client Specification

## Purpose

Documents the behavior of the showcase command client: a reactive wrapper dispatching the four showcase commands to the
command service through the Axon reactor command gateway, translating business errors, and protecting the service with
Resilience4j time limiter, circuit breaker, and conditional retry.

**Contract source:** the commands and error codes this client dispatches and translates are owned by the
`write-side/command-service` spec — the commands `ScheduleShowcaseCommand`, `StartShowcaseCommand`,
`FinishShowcaseCommand`, `RemoveShowcaseCommand` and the error codes `ILLEGAL_STATE`, `NOT_FOUND`, `TITLE_IN_USE`,
`INVALID_COMMAND`.

## Requirements

### Requirement: Command dispatch operations

The system SHALL expose four asynchronous operations — `schedule`, `start`, `finish`, and `remove` — each accepting the
corresponding showcase command and completing when the command has been handled by the command service.

#### Scenario: Scheduling a showcase succeeds

- **WHEN** a `schedule` operation is invoked with a valid `ScheduleShowcaseCommand`
- **THEN** the command is dispatched and the operation completes successfully

#### Scenario: Starting a showcase succeeds

- **WHEN** a `start` operation is invoked with a valid `StartShowcaseCommand`
- **THEN** the command is dispatched and the operation completes successfully

#### Scenario: Finishing a showcase succeeds

- **WHEN** a `finish` operation is invoked with a valid `FinishShowcaseCommand`
- **THEN** the command is dispatched and the operation completes successfully

#### Scenario: Removing a showcase succeeds

- **WHEN** a `remove` operation is invoked with a valid `RemoveShowcaseCommand`
- **THEN** the command is dispatched and the operation completes successfully

### Requirement: Business error translation

The system SHALL translate command failures that carry showcase error details into a `ShowcaseCommandException`
preserving the error code and message, so downstream code can distinguish business failures from infrastructure
failures.

#### Scenario: Failed dispatch fails with a showcase command exception

- **WHEN** the command service reports a failure carrying showcase error details
- **THEN** the operation fails with a `ShowcaseCommandException` carrying those error details

#### Scenario: Failure without error details propagates unchanged

- **WHEN** the command service reports a failure without showcase error details
- **THEN** the operation fails with the original exception

### Requirement: Retry of retryable failures

The system SHALL retry a failed operation only when the failure is retryable: it is neither a `ShowcaseCommandException`
(business error) nor an Axon non-transient exception. Each retryable failure causes a re-dispatch of the command up to
the configured attempt limit, after which the operation fails with the last error.

#### Scenario: Transient dispatch failure is retried

- **WHEN** a command dispatch fails with a transient error such as a missing handler, a dispatch error, or a remote
  handling failure
- **THEN** the operation retries the dispatch up to the configured attempt limit and then fails with the last error

#### Scenario: Business error is not retried

- **WHEN** a command dispatch fails with a `ShowcaseCommandException`
- **THEN** the operation fails immediately without retrying

#### Scenario: Non-transient Axon error is not retried

- **WHEN** a command dispatch fails with an Axon non-transient exception, directly or as the cause
- **THEN** the operation fails immediately without retrying

### Requirement: Circuit breaker isolation of business errors

The system SHALL treat business errors as circuit breaker failures to ignore: a `ShowcaseCommandException` SHALL NOT
count toward opening the circuit breaker, while infrastructure failures SHALL.

#### Scenario: Business error does not open the circuit breaker

- **WHEN** a command dispatch fails with a `ShowcaseCommandException`
- **THEN** the circuit breaker is not affected by that failure

### Requirement: Automatic resilience configuration

The system SHALL register, on auto-configuration, the Resilience4j customizers that wire the retry filter and the
circuit breaker behavior above for the command service, so consumers get the protection without additional setup.

#### Scenario: Retry customizer is registered

- **WHEN** the command client is auto-configured
- **THEN** the retry configuration for the command service retries only failures accepted by the retry filter

#### Scenario: Circuit breaker customizer is registered

- **WHEN** the command client is auto-configured
- **THEN** the circuit breaker configuration for the command service ignores business errors
