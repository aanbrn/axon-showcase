__# Fix Nanosecond Date Precision in the Showcase Projection — Design

## Context

See proposal.md — Why. Spring Data Elasticsearch 5.5.13's `DateFormat.strict_date_optional_time_nanos` enum maps to
the Java pattern `uuuu-MM-dd['T'HH:mm:ss.SSSSSSXXX]` — **6 `S` digits (micros)** — despite the "nanos" name
(`DateFormat.java:91`). The entity's `Instant` fields are converted by `TemporalPropertyValueConverter.write`, which
calls `dateConverters.get(0).format(...)`. `MappingElasticsearchConverter` builds the converter list with built-in
formats first (`SimpleElasticsearchPersistentProperty.getDateConverters`), so the micros converter is always first.
The result: write truncates `Instant` nanos to micros; the OpenSearch mapping is correct (`date_nanos`) but the
stored value is not.

The spike (GitHub Actions, `spike/it-ci`) proved the failure is purely this write-side truncation: a serialized run
failed identically (`--no-parallel` ruled out resource contention), and the assertion diff showed
`expected ...643933764Z` vs `actual ...643933Z` — the exact micros truncation. The truncation is platform-sensitive
in the *test*, not the OpenSearch stack: `Instant.now()` yields microsecond precision on macOS (hiding the bug) but
nanosecond precision on Linux runners (exposing it), and the seed derives all timestamps from it. OpenSearch itself
stores faithfully whatever it receives; no client/server date-format difference is involved.

## Goals / Non-Goals

**Goals:**
- Make `ShowcaseEntity`'s `Instant` timestamps round-trip at nanosecond precision through OpenSearch.
- Change the annotation minimally, keeping `FieldType.Date_Nanos` and the ISO-8601-with-nanos wire format.

**Non-Goals:**
- Not patching Spring Data (out of scope for a reference app).
- Not switching the mapping type away from `date_nanos`.
- Not the IT index-lifecycle cleanup (tracked in `opensearch-it-index-lifecycle`).

## Decisions

### D1: Custom 9-digit pattern over keeping `strict_date_optional_time_nanos`

Because `TemporalPropertyValueConverter.write` uses `dateConverters.get(0)` and built-in formats sort first, the only
way to get a 9-digit formatter is to drop the built-in format entirely and supply a custom pattern:

```java
@Field(type = FieldType.Date_Nanos, format = {}, pattern = "yyyy-MM-dd['T'HH:mm:ss.SSSSSSSSSXXX]")
```

`format = {}` yields a single converter (the custom one) as index 0, used for both write and read. The pattern is a
valid Java `DateTimeFormatter` pattern (verified locally: `2026-08-28T02:58:45.643933764Z` formats and parses back
exactly). `MappingParameters` writes the custom pattern string into the index mapping's `format` verbatim, so the
mapping `format` changes from `strict_date_optional_time_nanos` to the explicit pattern.

Alternatives considered:
- Keeping `format = strict_date_optional_time_nanos` and adding a custom `pattern` — rejected: the built-in converter
  stays first, so the micros formatter still wins on write.
- Truncating seeds/assertions to micros — rejected (was the old change's rejected option): hides real precision loss.
- `expandWildcardImports`-style ecosystem fix — N/A; this is a Spring Data formatter limitation.

### D2: Reuse the verified pattern on all four `Instant` fields via a named constant

`startTime`, `scheduledAt`, `startedAt`, `finishedAt` all carry the same annotation. The pattern string is repeated
four times and is error-prone (nine `S` digits — easy to miscount, and a typo would silently change precision), so
it is introduced as a private constant on `ShowcaseEntity`:

```java
private static final String NANOS_DATE_PATTERN = "yyyy-MM-dd['T'HH:mm:ss.SSSSSSSSSXXX]";
```

Annotations accept compile-time constants, so `@Field(type = FieldType.Date_Nanos, format = {},
pattern = NANOS_DATE_PATTERN)` resolves correctly even for a `private` constant. The constant carries a Javadoc
describing that it is the mapping contract for nanosecond-precision `date_nanos` fields: it drives both the
OpenSearch index `format` and the Spring Data read/write converter, and that Spring Data's built-in
`strict_date_optional_time_nanos` cannot be used because it truncates to microseconds. The constant is a single
place to change if the projection model grows or another entity needs the same precision.

## Risks / Trade-offs

- **Index mapping format change requires index re-creation** → existing `showcases` indexes must be deleted and
  recreated to pick up the new `format` string; documented in the proposal as **BREAKING**.
- **Custom pattern bypasses the framework's built-in date handling** → the pattern is a standard ISO-8601-with-nanos
  formatter; verified via a scratch `DateTimeFormatter` test and the local `integrationTest` run.
- **OpenSearch `strict_date_optional_time_nanos` acceptance of the custom pattern string** → validated empirically:
  the CI spike run with this exact annotation passed both IT suites against the real OpenSearch `:3` container.

## Migration Plan

1. Apply the annotation change to the four `Instant` fields in `ShowcaseEntity.java`.
2. Verify locally: `:showcase-query-service:integrationTest` and `:showcase-projection-service:integrationTest` pass.
3. Verify in CI (via the spike workflow) that both suites pass on Linux/OpenSearch `:3`.
4. On deploy, re-create the `showcases` index (delete + create with mapping) so the new format takes effect.
5. Update `openspec/specs/showcase/read-side/projection-model/spec.md` to the new `format` (task 4.1), so the spec
   stays the source of truth — it previously named the micros-truncating `strict_date_optional_time_nanos`.

Rollback: revert the annotation; indexes must be re-created again to return to the old format.

## Open Questions

None.
