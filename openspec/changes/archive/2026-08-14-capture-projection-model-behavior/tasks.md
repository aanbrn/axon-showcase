## 1. Validate

- [x] 1.1 Run `openspec validate capture-projection-model-behavior --type change` and confirm the change and the new
  spec delta pass

## 2. Review

- [x] 2.1 Review the `showcase/read-side/projection-model` spec against the `ShowcaseEntity` implementation and its
  `ShowcaseEntityMappingCT`/`ShowcaseEntityJacksonCT` component tests, and confirm every requirement and scenario
  matches current behavior

## 3. Archive

- [x] 3.1 Run `openspec archive capture-projection-model-behavior --yes` to fold the delta into
  `openspec/specs/showcase/read-side/projection-model/spec.md`
- [x] 3.2 Confirm the spec exists under `openspec/specs/showcase/read-side/` and `openspec validate --specs` passes
