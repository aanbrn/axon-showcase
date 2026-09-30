# Proposal: Hold back a dependency at a version line

## Why

The weekly `dependency-updates` tracker re-lists `org.opensearch.client:opensearch-java` `3.9.0`→`3.10.0` as an
actionable row, but `3.10.0` is not a drop-in: it changes `Hit.matchedQueries()`'s return type from `List` to the new
`MatchedQueries`, binary-incompatible with `spring-data-opensearch` 2.x's `DocumentAdapters.from`, and supporting it
rides `spring-data-opensearch` 3.x (the deferred Spring Boot 4 migration, ADR-0004). The shipped `major-disabled` list
suppresses major jumps only, so a coordinate the project must hold at an older **minor** has no suppression mechanism —
the false actionable row returns every week until ADR-0004 lands. Worse, the file is loaded with `java.util.Properties`,
which splits a key at its first `:`, so the shipped `group:module` entries collapse to group prefixes and the
exact-coordinate suppression the spec describes does not work today (see What Changes).

## What Changes

- `config/dependency-updates/hold-back.properties` (new) — an opt-in list holding a coordinate at a version line,
  shipped with `org.opensearch.client:opensearch-java=3.9` and a comment pointing at its rationale.
- `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts` — read both suppression files with a
  colon-safe line parser (replacing `Properties.load`, whose first-`:` split collapses the shipped `group:module`
  entries to group prefixes), and delegate `rejectVersionIf` to the rule set.
- `build-logic/src/main/kotlin/DependencyUpdateRules.kt` (new) — the parsing of both suppression files and the whole
  `dependencyUpdates` reject decision (catalog-ownership, major, and the new hold-back predicates) extracted into a
  pure, unit-testable rule set.
- `build-logic/src/test/kotlin/DependencyUpdateRulesTests.kt` (new) — unit tests for the parsing, the predicates, and
  the composed decision, including an exact-coordinate entry matching only its module.
- `.opencode/agent/architecture-auditor.md`, `.opencode/commands/audit-architecture.md`, and the `agent-skills` delta —
  widen the architecture-auditor's swept suppression surfaces to include the hold-back list, so a held-back coordinate's
  rationale is swept like a major-suppressed one.
- `AGENTS.md`, `README.md`, and `.opencode/commands/dependency-updates.md` — document the hold-back list beside the
  major-disabled list.
- `docs/ideas.md` — remove the implemented hold-back idea.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-management`: a new requirement — a catalog-owned coordinate can be held back at a version
  line, suppressing same-major candidates newer than that line while keeping the line's patches and any major jump
  reported; the shipped list holds `org.opensearch.client:opensearch-java` at `3.9`.
- `showcase/quality/agent-skills`: the architecture-auditor's intent-clarification requirement gains the dependency
  hold-back list among the suppression surfaces it sweeps.

## Impact

- **Build**: `build-logic` gains the rule set and its tests; the `dependencyUpdates` report is filtered by the new list
  and the suppression files are parsed correctly. No application code or runtime behavior changes.
- **CI / tooling**: the next `dependency-updates` run stops listing the held-back coordinate; the report, its workflow,
  and the `npmOutdated` report are otherwise unchanged.
- **Suppression behavior**: fixing the parser is code→spec alignment — the `major-disabled` exact-coordinate entries
  finally suppress only their modules, so `opensearch-java`/`opensearch-rest-client` majors become reported as the
  `dependency-management` spec already requires. No spec delta is owed for it.
- **Not taken**: reworking the helm suppression list the same way (a line parse owned by `HelmUpdateRules`) and renaming
  all three suppression files off `.properties` — its own change, parked in `docs/ideas.md` by this change's tasks.
- **Deployment**: none.
