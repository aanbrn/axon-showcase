# Proposal

## Why

Two adjacent members with no blank line between them read poorly. The repo already separates every hand-written method,
constructor, initializer, and nested type with a blank line; a repo-wide run of Checkstyle's `EmptyLineSeparator` (with
member tokens, generated sources suppressed) flags nothing for those and exactly 14 grouped fields in 8 blocks — so the
convention is nearly universal, and the rule just codifies it and stops new grouped members. `EmptyLineSeparator` is the
stock Checkstyle rule for this; the sibling type-body padding rule (`enforce-type-body-padding`) covers the gap after a
type body's `{`, which `EmptyLineSeparator` never checks, so the two compose.

## What Changes

- `config/checkstyle/checkstyle.xml`: add a `TreeWalker` `EmptyLineSeparator` module scoped to member tokens (its
  default tokens include `PACKAGE_DEF`, which would flag the SPDX-header-to-`package` gap in every file) and requiring a
  blank line between consecutive members, fields included.
- Fix the 14 grouped fields across 8 files (all in test sources), each by inserting a blank line between the fields:
  `ShowcaseApiErrorResolverCT`, `ShowcaseApiGatewayE2E`, `ShowcaseDbSchedulerMetricsCT`,
  `FluentAccessorNamingStrategyTests`, `ShowcaseEntityMappingCT`, `Resilience4jAutoConfigurationImportFilterTests`,
  `KafkaTestPublisherTests`, and `RandomTestUtilsTests`.
- `AGENTS.md`: extend the Formatting note to cover member separation.
- `docs/ideas.md`: remove the `## 2026-10-08` section, whose only entry is this change's idea (it rides this branch).
- A delta spec on `showcase/quality/code-quality`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/code-quality`: a new requirement that consecutive members of a type body are separated by a blank
  line (no blank line is required after an enum's constant, an annotation type's declaration, or an annotation type's
  member).

## Impact

- **Config**: `config/checkstyle/checkstyle.xml` (one `TreeWalker` module); the Checkstyle gate already runs on every
  source set, so no build wiring changes.
- **Code**: 14 grouped fields across 8 files gain a blank line; no semantics change.
- **Docs**: the `AGENTS.md` Formatting note and the `docs/ideas.md` removal.
- **Build / deployment**: no dependency, image, chart, or CI change.
