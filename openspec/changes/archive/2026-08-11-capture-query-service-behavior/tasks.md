## 1. Validate

- [x] 1.1 Run `openspec validate --change "capture-query-service-behavior"` and confirm the change and spec structure pass

## 2. Review

- [x] 2.1 Review the new `showcase/query-service` spec against the query service implementation and its integration tests
      and confirm every requirement and scenario matches current behavior

## 3. Archive

- [x] 3.1 Run `openspec archive "capture-query-service-behavior"` to fold the delta into
      `openspec/specs/showcase/query-service/spec.md`
- [x] 3.2 Confirm `openspec/specs/showcase/query-service/spec.md` exists and passes `openspec validate`
