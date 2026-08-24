## 1. Add the request unit tests

- [x] 1.1 Create `ScheduleShowcaseRequestTests` covering all-fields construction (fluent accessors) and
      `Validator`-based validation: a valid request has no violations; blank or too-long title, missing start time,
      and missing/too-short/too-long duration each report the expected constraint on the expected property

## 2. Add the response unit tests

- [x] 2.1 Create `ScheduleShowcaseResponseTests` covering all-fields construction, the null-pointer exception on a
      missing showcase ID (`@NonNull`), and `@KSUID` validation (valid ID passes, invalid ID reports the constraint)

## 3. Verify the change artifacts

- [x] 3.1 Run the gateway `test` suite (and `spotlessApply`/`checkstyleTest` on the new files) and confirm it passes
- [x] 3.2 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors