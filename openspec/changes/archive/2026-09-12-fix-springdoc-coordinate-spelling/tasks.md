## 1. Delta spec

- [x] 1.1 `quality/dependency-management` — normalize the three `org.springdoc: springdoc-openapi-starter-webflux-ui`
      mentions to `group:artifact` (copy the current requirement verbatim, then edit)

## 2. Verification

- [x] 2.1 Run `openspec validate --all` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the PR gate)
- [x] 2.2 Confirm no stray `group: artifact` coordinate remains in `openspec/specs/` and the MODIFIED delta carries both
      scenarios
