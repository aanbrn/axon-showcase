## 1. Validate

- [ ] 1.1 Run `openspec validate --change "capture-mapstruct-extension-behavior"` and confirm the change and spec
      structure pass

## 2. Review

- [ ] 2.1 Review the new `showcase/mapstruct-extension` spec against the `FluentAccessorNamingStrategy` implementation
      and the `showcase-query-service` mapper usage, and confirm every requirement and scenario matches current behavior

## 3. Archive

- [ ] 3.1 Run `openspec archive "capture-mapstruct-extension-behavior"` to fold the delta into
      `openspec/specs/showcase/mapstruct-extension/spec.md`
- [ ] 3.2 Confirm `openspec/specs/showcase/mapstruct-extension/spec.md` exists and passes `openspec validate`
