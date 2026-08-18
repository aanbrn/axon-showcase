## 1. Coverage convention

- [x] 1.1 Add `jacoco = 0.8.13` to the version catalog
- [x] 1.2 Create the `code-coverage-conventions` build plugin (explicit `jacoco` plugin, typed report and verification
      configuration, catalog-managed version)
- [x] 1.3 Wire the gate into `check`, reading the baseline from `config/jacoco/coverage-baseline.properties`
- [x] 1.4 Add generated-code exclusion (protobuf patterns) to the report and verification `classDirectories`
- [x] 1.5 Support per-module gate opt-out via `extra["coverage.gate.enabled"] = false`
- [x] 1.6 Remove the redundant hardcoded accessor-hash imports in `code-check-conventions`

## 2. Apply the convention

- [x] 2.1 Apply `code-coverage-conventions` to the four services, two clients, and four logic libraries
- [x] 2.2 Apply it to `showcase-query-proto` (report only) and opt out of its gate
- [x] 2.3 Opt out of the gate for `showcase-api-gateway`, `showcase-query-service`, `showcase-query-client`

## 3. Aggregate report

- [x] 3.1 Add the `jacocoRootReport` aggregate task to the root build, with the generated-code exclusion

## 4. Baseline and verification

- [x] 4.1 Set `config/jacoco/coverage-baseline.properties` to `0.80`
- [x] 4.2 Verify `./gradlew build -x e2eTest` passes with the gate enforced on the gated modules
