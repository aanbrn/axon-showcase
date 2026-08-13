## 1. Validate

- [ ] 1.1 Run `openspec validate --change "capture-identifier-extension-behavior"` and confirm the change and spec
      structure pass

## 2. Review

- [ ] 2.1 Review the new `showcase/identifier-extension` spec against the `KsuidIdentifierFactory`, `KsuidValidator`,
      and `@KSUID` annotation implementations, and confirm every requirement and scenario matches current behavior

## 3. Archive

- [ ] 3.1 Run `openspec archive "capture-identifier-extension-behavior"` to fold the delta into
      `openspec/specs/showcase/identifier-extension/spec.md`
- [ ] 3.2 Confirm `openspec/specs/showcase/identifier-extension/spec.md` exists and passes `openspec validate`
