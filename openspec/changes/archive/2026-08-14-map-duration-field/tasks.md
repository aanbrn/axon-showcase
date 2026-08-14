# Tasks

## Validate and apply

- [ ] Validate the change with `openspec validate --change map-duration-field`
- [ ] Apply the `@Field(type = FieldType.Keyword)` annotation to `ShowcaseEntity.duration`
- [ ] Update `ShowcaseEntityMappingCT` to assert `duration` is mapped as `keyword` and the property count is 9
- [ ] Update `openspec/specs/showcase/read-side/projection-model/spec.md` directly (skip_specs change; the CLI cannot
      express a scenario rename in a MODIFIED requirement)

## Verify

- [ ] Run `./gradlew :showcase-projection-model:componentTest` and confirm the mapping component tests pass
- [ ] Run `openspec validate --specs` and confirm all specs (including the updated projection-model) validate

## Review and archive

- [ ] Review the diff against the updated spec and the actual mapping output
- [ ] Archive the change with `openspec archive map-duration-field --yes`
- [ ] Commit and push the archived change and updated spec
