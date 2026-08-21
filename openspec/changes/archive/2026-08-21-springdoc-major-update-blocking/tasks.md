## 1. Suppress springdoc major updates

- [x] 1.1 Add the exact coordinate `org.springdoc:springdoc-openapi-starter-webflux-ui` to
      `config/dependency-updates/major-disabled.properties` with a short pointer comment, and verify the springdoc
      major row (`[2.8.17 -> 3.1.0]`) no longer appears in `./gradlew dependencyUpdates` output while same-major (2.x)
      updates remain reported

## 2. Verify the change artifacts

- [x] 2.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors
- [x] 2.2 Confirm the delta spec at `specs/showcase/quality/dependency-management/spec.md` declares the springdoc
      suppression requirement with scenarios