## 1. Validate

- [x] 1.1 Run `openspec validate --change "capture-api-gateway-behavior"` and confirm the change and spec structure pass

## 2. Review

- [x] 2.1 Review the new `showcase/api-gateway` spec against the API gateway implementation and its component/integration
      tests and confirm every requirement and scenario matches current behavior

## 3. Archive

- [x] 3.1 Run `openspec archive "capture-api-gateway-behavior"` to fold the delta into
      `openspec/specs/showcase/api-gateway/spec.md`
- [x] 3.2 Confirm `openspec/specs/showcase/api-gateway/spec.md` exists and passes `openspec validate`
