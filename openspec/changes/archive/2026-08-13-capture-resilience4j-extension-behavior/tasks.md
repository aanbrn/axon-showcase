## 1. Validate

- [ ] 1.1 Run `openspec validate --change "capture-resilience4j-extension-behavior"` and confirm the change and spec
      structure pass

## 2. Review

- [ ] 2.1 Review the new `showcase/resilience4j-extension` spec against the
      `Resilience4jAutoConfigurationImportFilter` implementation, `spring.factories` registration, and
      `additional-spring-configuration-metadata.json` declarations, and confirm every requirement and scenario
      matches current behavior

## 3. Archive

- [ ] 3.1 Run `openspec archive "capture-resilience4j-extension-behavior"` to fold the delta into
      `openspec/specs/showcase/resilience4j-extension/spec.md`
- [ ] 3.2 Confirm `openspec/specs/showcase/resilience4j-extension/spec.md` exists and passes `openspec validate`
