# Proposal: Improve Helm values comments

## Why

The chart's `@param` comments in `values.yaml` are terse one-liners that omit non-obvious information: enum
possibilities, units, valid ranges, list formats, and cross-field caveats. Since these comments power editor hover help
and documentation, users are left guessing at valid values (for example, that `pollingStrategy` accepts only
`lock_and_fetch` and `fetch`, or that `failureRateThreshold` is a 0-100 percentage).

## What Changes

- Enrich a targeted set of `@param` comments in `helm/chart/src/main/helm/values.yaml` with a compact `(...)` suffix
  carrying only the non-obvious information for that field: enum options, units, valid ranges, list formats, and
  cross-field caveats.
- Only enrich comments where the extra information is genuinely non-obvious; leave obvious fields (e.g. `replicaCount`,
  `enabled` flags, plain names) unchanged.
- Do not duplicate the field's default value in the comment — it is already the value in the YAML beside the comment
  (avoiding redundancy).
- Apply the same enrichment consistently across the four near-identical service blocks (`commandService`,
  `queryService`, `projectionService`, `apiGateway`).
- No functional or behavioral change: comments only, no templates, values, or schema are touched.

## Capabilities

### New Capabilities

(none — documentation-only change)

### Modified Capabilities

(none — no spec-level behavior changes; `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- `helm/chart/src/main/helm/values.yaml` — `@param` comments enriched for the non-obvious fields (enums such as
  `pollingStrategy`, `slidingWindowType`, `updateStrategy.type`, `updateMode`, `seccompProfile.type`,
  `terminationMessagePolicy`, `image.pullPolicy`; ranges such as `failureRateThreshold` and
  `slowCallRateThreshold`; units and list-format hints; cross-field caveats).
- No change to Helm templates, chart values (the values themselves stay identical), the Docker images, or any Java
  code. No dependency or API changes.
