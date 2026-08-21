## 1. Suppress Flyway major updates

- [x] 1.1 Add `org.flywaydb` to `config/dependency-updates/major-disabled.properties` and verify the Flyway major row
      (`flyway-core` and `flyway-database-postgresql` `[11.20.3 -> 13.3.0]`) no longer appears in `./gradlew
      dependencyUpdates` output while same-major (11.x) updates remain reported
- [x] 1.2 Confirm existing major-disabled behavior is unaffected: `org.axonframework` and `org.springframework`
      entries still suppress their major jumps in the `dependencyUpdates` report

## 2. Verify the change artifacts

- [x] 2.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors
- [x] 2.2 Confirm the delta spec at `specs/showcase/quality/dependency-management/spec.md` declares the `org.flywaydb`
      suppression requirement with scenarios