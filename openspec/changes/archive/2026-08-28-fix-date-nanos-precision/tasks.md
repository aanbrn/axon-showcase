# Tasks

## 1. Apply the annotation change

- [x] 1.1 Introduce a `private static final String NANOS_DATE_PATTERN` constant on `ShowcaseEntity` with a Javadoc
      explaining that it is the mapping contract for nanosecond-precision `date_nanos` fields (drives both the
      OpenSearch index `format` and the Spring Data read/write converter) and why Spring Data's built-in
      `strict_date_optional_time_nanos` is not usable (it truncates to microseconds)
- [x] 1.2 Replace `format = DateFormat.strict_date_optional_time_nanos` with
      `format = {}, pattern = NANOS_DATE_PATTERN` on the four `Instant` fields (`startTime`, `scheduledAt`,
      `startedAt`, `finishedAt`) in
      `showcase-projection-model/src/main/java/showcase/projection/ShowcaseEntity.java`, and verify the module
      compiles (`:showcase-projection-model:compileJava`) and `spotlessApply` leaves no unused `DateFormat` import
- [x] 1.3 Verify the mapping produced for the entity keeps `type = date_nanos` and sets the new custom `format`
      string (via the generated index mapping or a live `indexOps(ShowcaseEntity.class).createMapping()` dump)

## 2. Verify locally

- [x] 2.1 Run `:showcase-query-service:integrationTest` and confirm all tests pass locally (Docker required)
- [x] 2.2 Run `:showcase-projection-service:integrationTest` and confirm all tests pass locally (Docker required;
      the projection service writes projections through the same entity)

## 3. Verify in CI

- [x] 3.1 Push the change on a branch with the temporary spike workflow and confirm both
      `:showcase-projection-service:integrationTest` and `:showcase-query-service:integrationTest` pass on GitHub
      Actions (Linux + OpenSearch `:3`), then remove the temporary workflow
- [x] 3.2 Run `openspec validate fix-date-nanos-precision` and confirm the change is valid with all artifacts
      consistent

## 4. Update the projection-model spec

- [x] 4.1 Update `openspec/specs/showcase/read-side/projection-model/spec.md` so the field-mapping requirement and the
      "Timestamps map as nanosecond dates" scenario state the custom `yyyy-MM-dd['T'HH:mm:ss.SSSSSSSSSXXX]` pattern
      instead of `strict_date_optional_time_nanos` (keeping type `date_nanos`), so the spec reflects the real mapping