# Proposal: Parse the Helm suppression list like the other two, and drop `.properties`

## Why

`config/dependency-updates/major-disabled.properties` and `hold-back.properties` are parsed as line lists by
`DependencyUpdateRules`, but they still carry the `.properties` extension the format no longer uses — the same extension
that invited the `group:module` colon-split defect the hold-back change fixed. The
`config/helm-updates/major-disabled.properties` list is worse off: it is the last suppression list read with
`java.util.Properties`, inline in `build.gradle.kts` and untested, while its sibling rules (`HelmUpdateRules.sameMajor`)
sit in `build-logic`. Three lists, three inconsistent treatment.

## What Changes

- `build-logic/src/main/kotlin/HelmUpdateRules.kt` — gains the line-list parse (mirroring `DependencyUpdateRules`), so a
  chart-name entry is read by the repository's own rule rather than by `Properties`.
- `build.gradle.kts` — the `helmUpdates` task's `majorDisabled` provider reads the file with `HelmUpdateRules` instead
  of `Properties`.
- `build-logic/src/test/kotlin/HelmUpdateRulesTests.kt` — covers the parse (comments/blank lines skipped; a chart name
  with no `=` kept whole), beside the existing `sameMajor` case.
- Renames, so all three suppression lists drop the extension their format no longer uses:
  `config/dependency-updates/major-disabled.properties` → `major-disabled.txt`,
  `config/dependency-updates/hold-back.properties` → `hold-back.txt`, and
  `config/helm-updates/major-disabled.properties` → `major-disabled.txt`.
- The loader paths and the references move with the files:
  `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts`, `build.gradle.kts`, `AGENTS.md`,
  `README.md`, `.opencode/commands/dependency-updates.md`, and the architecture-auditor's swept surfaces (definition,
  command, and the `AGENTS.md` bullet).
- `docs/ideas.md` — remove the implemented idea.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Unexplained design intent is surfaced for clarification" requirement names the
  two `config/dependency-updates/` suppression files verbatim as the architecture-auditor's swept surfaces, so it moves
  with the rename (`MODIFIED`; the requirement's behavior is unchanged).

The `merge-governance` and `dependency-management` requirements need no delta: each says "the helm major-disabled
configuration" or "the major-disabled configuration" without naming a path or extension, and the same entries keep
suppressing the same majors, so no scenario outcome changes.

## Impact

- **Build**: `build-logic` gains the parse and its test; the `helmUpdates` report reads the same entries from a renamed
  file. No application code or runtime change.
- **Follow-up retired**: `anomalyco/opencode#49904` is unrelated; the deferred rename idea in `docs/ideas.md` is what
  this change implements.
