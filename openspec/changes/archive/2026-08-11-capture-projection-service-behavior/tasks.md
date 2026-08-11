## 1. Validate

- [x] 1.1 Run `openspec validate --change "capture-projection-service-behavior"` and confirm the change and spec structure pass

## 2. Review

- [x] 2.1 Review the new `showcase/projection-service` spec against the projection service implementation and its integration
      tests and confirm every requirement and scenario matches current behavior

## 3. Archive

- [x] 3.1 Run `openspec archive "capture-projection-service-behavior"` to fold the delta into `openspec/specs/showcase/projection-service/spec.md`
- [x] 3.2 Confirm `openspec/specs/showcase/projection-service/spec.md` exists and passes `openspec validate`