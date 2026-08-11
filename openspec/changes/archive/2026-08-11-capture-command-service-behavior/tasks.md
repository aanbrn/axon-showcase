## 1. Validate

- [x] 1.1 Run `openspec validate --change "capture-command-service-behavior"` and confirm the change and spec structure pass

## 2. Review

- [x] 2.1 Review the new `showcase/command-service` spec against the command service implementation and its component/integration
      tests and confirm every requirement and scenario matches current behavior

## 3. Archive

- [x] 3.1 Run `openspec archive "capture-command-service-behavior"` to fold the delta into `openspec/specs/showcase/command-service/spec.md`
- [x] 3.2 Confirm `openspec/specs/showcase/command-service/spec.md` exists and passes `openspec validate`