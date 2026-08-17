## MODIFIED Requirements

### Requirement: Query validation

The system SHALL validate query payloads against bean validation constraints, enabled by default and configurable via
the `showcase.query.validation-enabled` property, and SHALL reject invalid queries with a 400 Bad Request problem detail
whose `fieldErrors` property maps each offending property path to its violation messages.

#### Scenario: Invalid list query is rejected with property errors

- **WHEN** a `FetchShowcaseListQuery` is dispatched whose payload violates its constraints (for example an `afterId`
  that is not a valid KSUID or a `size` outside 1 to 1000 inclusive) and validation is enabled (the default)
- **THEN** the system rejects the request with a 400 Bad Request, detail "Given query is not valid", and a
  `fieldErrors` map of each offending property path to its validation messages

#### Scenario: Invalid by-ID query is rejected with property errors

- **WHEN** a `FetchShowcaseByIdQuery` is dispatched with a `showcaseId` that is not a valid KSUID and validation is
  enabled (the default)
- **THEN** the system rejects the request with a 400 Bad Request, detail "Given query is not valid", and a
  `fieldErrors` map of the `showcaseId` property to its validation messages

#### Scenario: Query violating constraints succeeds when validation is disabled

- **WHEN** a query is dispatched whose payload violates its constraints while `showcase.query.validation-enabled` is set
  to `false`
- **THEN** the query proceeds to handling without validation, and no 400 Bad Request is produced
