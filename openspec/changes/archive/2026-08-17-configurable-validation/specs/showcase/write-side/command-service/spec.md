## MODIFIED Requirements

### Requirement: Command validation

The system SHALL validate command payloads against bean validation constraints, enabled by default and configurable via
the `showcase.command.validation-enabled` property, and SHALL reject invalid commands with error code INVALID_COMMAND,
including a map of property path to violation messages.

#### Scenario: Invalid command is rejected with property errors

- **WHEN** a command is dispatched whose payload violates its validation constraints (for example blank title, startTime
  not in the future, duration outside 1 to 10 minutes inclusive, or a `showcaseId` that is not a valid KSUID) and
  validation is enabled (the default)
- **THEN** the system rejects the command with error code INVALID_COMMAND, message "Given command is not valid", and
  metadata mapping each offending property path to its validation message

#### Scenario: Command violating constraints succeeds when validation is disabled

- **WHEN** a command is dispatched whose payload violates its validation constraints while
  `showcase.command.validation-enabled` is set to `false`
- **THEN** the command proceeds to handling without validation, and no INVALID_COMMAND error is produced
