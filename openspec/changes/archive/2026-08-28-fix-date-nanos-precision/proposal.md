# Fix Nanosecond Date Precision in the Showcase Projection

## Why

`ShowcaseEntity` declares its `Instant` timestamps as OpenSearch `date_nanos`, but Spring Data Elasticsearch 5.5.13's
`DateFormat.strict_date_optional_time_nanos` maps to a Java pattern with only **6 fractional digits**
(`SSSSSS` = microseconds), not 9. Every timestamp written to OpenSearch is silently truncated from nanoseconds to
microseconds, so a read-back round-trip never equals the original `Instant`. This surfaced as a deterministic
`ShowcaseQueryControllerIT` failure in GitHub Actions (verified via a spike: expected `...643933764Z`, got
`...643933Z`) and represents real precision loss for every projection in the system.

## What Changes

- Replace `format = DateFormat.strict_date_optional_time_nanos` with a custom 9-digit pattern on the four `Instant`
  fields of `showcase-projection-model/.../ShowcaseEntity.java`, introduced as a single javadoc'd constant
  (`NANOS_DATE_PATTERN`) reused by all four fields:
  - `@Field(type = FieldType.Date_Nanos, format = {}, pattern = NANOS_DATE_PATTERN)`
  - `NANOS_DATE_PATTERN = "yyyy-MM-dd['T'HH:mm:ss.SSSSSSSSSXXX]"`
- The mapping keeps `type = date_nanos`; only the `format` string changes from the built-in name to the explicit
  custom pattern. Spring Data then uses a 9-digit formatter on both write and read, so nanos round-trip exactly.
- **BREAKING**: existing OpenSearch indexes created with the old format must be re-created; the new mapping string
  differs from the old one.
- The projection-model spec (`openspec/specs/showcase/read-side/projection-model/spec.md`) is corrected: its field
  mapping requirement and scenario name the custom pattern instead of `strict_date_optional_time_nanos`, matching the
  new mapping (task 4.1).

## Capabilities

### New Capabilities

(none — bug fix; the projection-model spec's field-mapping requirement is corrected to the new `format` string as part
of the change, so `skip_specs: true` is kept in `.openspec.yaml`)

### Modified Capabilities

(none)

## Impact

- **Affected code**: `showcase-projection-model/src/main/java/showcase/projection/ShowcaseEntity.java` (shared by the
  projection and query services).
- **Behavior**: timestamps now round-trip at nanosecond precision through OpenSearch instead of microsecond truncation.
- **Data/ops**: existing `showcases` indexes need re-creation to pick up the new mapping `format`; no migration of
  stored data is possible beyond reindexing.
- **Tests**: `ShowcaseQueryControllerIT` and `ShowcaseProjectorIT` were failing in CI on this truncation; the fix is
  verified by the `opensearch-it-index-lifecycle` change's CI run.