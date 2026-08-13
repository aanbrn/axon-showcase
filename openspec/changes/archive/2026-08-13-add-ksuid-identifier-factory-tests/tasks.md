## 1. Tests

- [ ] 1.1 Create `KsuidIdentifierFactoryTests` in `showcase-identifier-extension/src/test/java/showcase/identifier/`
- [ ] 1.2 Add a test asserting `generateIdentifier()` returns a non-blank, 27-character Base62 string that
      round-trips through `Ksuid.fromString`
- [ ] 1.3 Add a test asserting generated identifiers are unique across many invocations
- [ ] 1.4 Add a test asserting `IdentifierFactory.getInstance()` resolves to a `KsuidIdentifierFactory`
- [ ] 1.5 Add a test asserting generated KSUIDs expose monotonically non-decreasing timestamps, with a comment
      documenting the same-second sortability caveat

## 2. Validate

- [ ] 2.1 Run `openspec validate --changes "add-ksuid-identifier-factory-tests"` and confirm the change passes
- [ ] 2.2 Run `./gradlew :showcase-identifier-extension:test` and confirm all tests pass

## 3. Archive

- [ ] 3.1 Run `openspec archive "add-ksuid-identifier-factory-tests"` to archive the change
