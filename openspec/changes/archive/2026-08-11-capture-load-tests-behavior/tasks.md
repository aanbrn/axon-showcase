## 1. Validate

- [x] 1.1 Run `openspec validate --change "capture-load-tests-behavior"` and confirm the change and spec structure pass

## 2. Review

- [x] 2.1 Review the new `showcase/load-tests` spec against the Gatling simulation and its configuration and confirm every
      requirement and scenario matches current behavior

## 3. Archive

- [x] 3.1 Run `openspec archive "capture-load-tests-behavior"` to fold the delta into
      `openspec/specs/showcase/load-tests/spec.md`
- [x] 3.2 Confirm `openspec/specs/showcase/load-tests/spec.md` exists and passes `openspec validate`
