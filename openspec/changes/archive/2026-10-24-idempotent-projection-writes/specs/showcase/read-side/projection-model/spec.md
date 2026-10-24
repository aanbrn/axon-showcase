# Spec Delta

## MODIFIED Requirements

### Requirement: Field mapping

The system SHALL map the document fields with the following OpenSearch types and formats: `showcaseId`, `status`, and
`duration` as `keyword`, `title` as `text`, `sequence` as `long`, and `startTime`, `scheduledAt`, `startedAt`, and
`finishedAt` as `date_nanos` with the custom nanosecond pattern `yyyy-MM-dd['T'HH:mm:ss.SSSSSSSSSXXX]` (Spring Data's
built-in `strict_date_optional_time_nanos` truncates to microseconds — see upstream spring-data-elasticsearch#3334).

#### Scenario: Identifier, status, and duration map as keyword

- **WHEN** the mapping of the `showcaseId`, `status`, and `duration` fields is inspected
- **THEN** all three are mapped with type `keyword`

#### Scenario: Title maps as text

- **WHEN** the mapping of the `title` field is inspected
- **THEN** it is mapped with type `text`

#### Scenario: Aggregate sequence maps as a long

- **WHEN** the mapping of the `sequence` field is inspected
- **THEN** it is mapped with type `long`

#### Scenario: Timestamps map as nanosecond dates

- **WHEN** the mapping of the `startTime`, `scheduledAt`, `startedAt`, and `finishedAt` fields is inspected
- **THEN** each is mapped with type `date_nanos` and the custom `yyyy-MM-dd['T'HH:mm:ss.SSSSSSSSSXXX]` pattern

#### Scenario: Type-hint class property is present

- **WHEN** the mapping of the document is inspected
- **THEN** the type-hint `_class` property is present
