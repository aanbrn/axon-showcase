## 1. Generated-code exclusion

- [x] 1.1 Extend `code-coverage-conventions` so a module can add generated-class excludes via
      `coverage.generatedClassExcludes` (merged with the default protobuf patterns)
- [x] 1.2 Set `showcase-query-proto`'s excludes to `**/QueryProto*.class` and `**/QueryRequest*.class`

## 2. Gate and verification

- [x] 2.1 Re-enable `showcase-query-proto`'s coverage gate (remove `extra["coverage.gate.enabled"] = false`)
- [x] 2.2 Verify the module reports ~100% and its gate passes at the 0.80 baseline
