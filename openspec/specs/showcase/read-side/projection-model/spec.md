# showcase/read-side/projection-model Specification

## Purpose

Documents the behavior of the showcase projection model: the shared `showcases` read-model document, its OpenSearch
index mapping and sort configuration, and its Jackson serialization contract — the contract between the projection side
that writes the document and the query side that reads it.

## Requirements

### Requirement: Showcases index and sort setting

The system SHALL store the read-model document in the OpenSearch index named `showcases` with a sort setting on
`showcaseId` in descending order.

#### Scenario: Index name is showcases

- **WHEN** the projection model is mapped to an OpenSearch index
- **THEN** the index name is `showcases`

#### Scenario: Default sort is showcaseId descending

- **WHEN** the index's default sort setting is inspected
- **THEN** the index sorts on the `showcaseId` field in descending order

### Requirement: Field mapping

The system SHALL map the document fields with the following OpenSearch types and formats: `showcaseId` and `status` as
`keyword`, `title` as `text`, and `startTime`, `scheduledAt`, `startedAt`, and `finishedAt` as `date_nanos` with the
`strict_date_optional_time_nanos` format. The `duration` field SHALL not be mapped.

#### Scenario: Identifier and status map as keyword

- **WHEN** the mapping of the `showcaseId` and `status` fields is inspected
- **THEN** both are mapped with type `keyword`

#### Scenario: Title maps as text

- **WHEN** the mapping of the `title` field is inspected
- **THEN** it is mapped with type `text`

#### Scenario: Timestamps map as nanosecond dates

- **WHEN** the mapping of the `startTime`, `scheduledAt`, `startedAt`, and `finishedAt` fields is inspected
- **THEN** each is mapped with type `date_nanos` and format `strict_date_optional_time_nanos`

#### Scenario: Duration is not mapped

- **WHEN** the mapping of the document is inspected
- **THEN** the `duration` field is not present, while the type-hint `_class` property is present

### Requirement: Jackson serialization contract

The system SHALL serialize and deserialize the document with Jackson such that a round-trip preserves every field in any
lifecycle state, preserves the nanosecond precision of `Instant` values, and preserves `null` fields as `null`.

#### Scenario: Round-trip preserves every field in any lifecycle state

- **WHEN** a document in the SCHEDULED, STARTED, or FINISHED state is serialized and deserialized
- **THEN** the resulting document equals the original with every field preserved

#### Scenario: Round-trip preserves nanosecond precision

- **WHEN** a document carrying `Instant` values with nanosecond precision is serialized and deserialized
- **THEN** the instants round-trip with their full nanosecond precision

#### Scenario: Round-trip preserves nulls

- **WHEN** a document with unset fields is serialized and deserialized
- **THEN** the unset fields remain `null`
