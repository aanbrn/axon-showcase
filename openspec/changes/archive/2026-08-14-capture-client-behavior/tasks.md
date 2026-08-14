## 1. Validate

- [x] 1.1 Run `openspec validate capture-client-behavior --type change` and confirm the change and both new spec deltas
  pass

## 2. Review

- [x] 2.1 Review the `showcase/clients/command-client` spec against the command client implementation
  (`ShowcaseCommandClient`, `ShowcaseCommandRetryFilter`, `ShowcaseCommandClientAutoConfiguration`) and its
  component/integration tests, and confirm every requirement and scenario matches current behavior
- [x] 2.2 Review the `showcase/clients/query-client` spec against the query client implementation
  (`ShowcaseQueryClient`, `ShowcaseQueryRetryFilter`, `ShowcaseQueryClientAutoConfiguration`,
  `ShowcaseQueryClientProperties`) and its component/integration tests, and confirm every requirement and scenario
  matches current behavior

## 3. Archive

- [x] 3.1 Run `openspec archive capture-client-behavior --yes` to fold both deltas into
  `openspec/specs/showcase/clients/{command-client,query-client}/spec.md`
- [x] 3.2 Confirm both specs exist under `openspec/specs/showcase/clients/` and `openspec validate --specs` passes
