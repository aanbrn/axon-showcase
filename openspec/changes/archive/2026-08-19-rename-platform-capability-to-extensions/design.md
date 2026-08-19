## Context

The OpenSpec showcase specs are organized by architectural role (`gateway`, `write-side`, `read-side`, `clients`,
`platform`, `deployment`, `quality`). The `platform` role holds the framework-extension libraries (identifier, MapStruct,
Resilience4j). "platform" collides with the `:platform` Gradle BOM module and misdescribes the role. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Rename the `showcase/platform` capability to `showcase/extensions`, reflecting that it groups extension libraries.
- Move the three leaf specs with no content changes.

**Non-Goals:**

- No change to requirement content, module names, or code.

## Decisions

- **Use `extensions` as the umbrella name** — it matches the `*-extension` module naming and removes the `:platform`
  BOM collision. *Alternatives considered:* `shared` (too generic), `cross-cutting` (vague), `integrations` (accurate
  but heavier); `extensions` is the most consistent with the module names.
- **Move the three leaf spec directories** under the renamed umbrella, preserving their names.

## Risks / Trade-offs

- [Renaming spec paths could orphan references in active changes] → there are no active changes referencing these
  specs; the rename is path-only with no content changes.
