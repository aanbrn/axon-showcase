## Why

The first `/audit-specs` run found the `<group>: <artifact>` coordinate spelling inconsistent in
`showcase/quality/dependency-management`: `apply-specs-audit-findings` normalized the OpenSearch coordinates, but the
sibling `springdoc` requirement in the same file carries the identical stray space
(`org.springdoc: springdoc-openapi-starter-webflux-ui`, three mentions). The finding scoped the fix to the OpenSearch
requirement; the _pattern_ is a corpus-wide convention, so this change sweeps the remaining instance in the same file —
as the "scope by the convention, not the finding" rule requires.

## What Changes

- Normalize the three `org.springdoc: springdoc-openapi-starter-webflux-ui` mentions in
  `showcase/quality/dependency-management` to `org.springdoc:springdoc-openapi-starter-webflux-ui` (drop the space after
  the colon), matching the `group:artifact` form used throughout the corpus.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/dependency-management`: the
  `springdoc major updates are suppressed until the Spring Boot 4 migration` requirement spells its coordinate with no
  stray space.

## Impact

- Delta spec for `showcase/quality/dependency-management` in this change.
- No code, build, or deployment change — spec text only.
