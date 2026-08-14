## Why

The `duration` field of the `showcases` document is currently left out of the explicit OpenSearch mapping and is
silently indexed by dynamic mapping. The projection-model spec documents this state as deliberate, but the code declares
no intent — the field has no `@Field` annotation at all. Mapping `duration` explicitly as `keyword` makes the stored
ISO-8601 span value searchable by exact match and aggregation, and makes the code match the documented contract.

## What Changes

- Annotate `ShowcaseEntity.duration` with `@Field(type = FieldType.Keyword)` so the field appears in the explicit
  OpenSearch mapping.
- Update the projection-model spec: `duration` is now mapped as `keyword` instead of being unmapped.
- Update `ShowcaseEntityMappingCT` to assert the mapped `duration` field and its `keyword` type.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/read-side/projection-model`: the "Field mapping" requirement changes — `duration` is mapped as `keyword`
  rather than not mapped.

## Impact

- `showcase-projection-model` source: `ShowcaseEntity.duration` gains the `@Field` annotation.
- `showcase-projection-model` component tests: `ShowcaseEntityMappingCT` field-mapping and no-unexpected-fields
  assertions change (8 → 9 mapped properties, `duration` present as `keyword`).
- `openspec/specs/showcase/read-side/projection-model/spec.md`: the field-mapping requirement and its
  "Duration is not mapped" scenario change.
- No behavior change on the read side: `duration` already round-trips through `_source`; the mapping only becomes
  explicit.

## Notes

This change uses `skip_specs: true`: the OpenSpec CLI cannot rename or drop a scenario inside a MODIFIED requirement
(the scenario-level-merge feature is still open upstream, PR #843), so the projection-model spec is edited directly
instead of through a delta spec. The delta file was removed from the change and the main spec updated in place.
