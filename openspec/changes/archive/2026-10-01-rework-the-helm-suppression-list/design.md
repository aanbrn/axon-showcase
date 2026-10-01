# Design

## Context

See `proposal.md` — Why. Current state that shapes the approach (verified 2026-10-01):

- `DependencyUpdateRules` owns the line parse for the two `config/dependency-updates/` lists: blank lines and `#`
  comments skipped, an entry read as the text before the first `=`, an optional `=value` dropped
  (`disabledEntries`/`holdBackEntries` over a shared `entryLine`). `java.util.Properties` was the earlier loader; its
  first-`:` key split collapsed the shipped `group:module` entries to group prefixes.
- `config/helm-updates/major-disabled.properties` holds colon-free chart names (`bitnami-postgresql`, `bitnami-kafka`,
  `bitnami-opensearch`), so `Properties` parses it correctly today — its `.properties` is not a live defect, only an
  inconsistency with the two reworked lists. It is loaded by `build.gradle.kts`'s `helmUpdates` task registration
  (`Properties().load(...)`, filtering blank names), and the resulting set is matched by name
  (`check.name in majorDisabled.get()` in `HelmUpdatesTask`).
- `HelmUpdateRules` holds only the tested `sameMajor`; the repo's pattern for a rule with cases worth pinning is a pure
  object in `build-logic/src/main/kotlin/` plus a `*Tests` class (as `DependencyUpdateRulesTests` is).
- No requirement names a suppression file's path or extension: `merge-governance` says "the helm major-disabled
  configuration" and `dependency-management` says "the major-disabled configuration", so renaming the files owes no
  delta.
- The loader paths are hard-coded, not globbed: `dependency-versions-conventions.gradle.kts` (the two dependency lists)
  and `build.gradle.kts` (the helm list).

## Goals / Non-Goals

**Goals:**

- One treatment for all three suppression lists: a repository-owned line parse, unit-tested, and an extension that does
  not name a format they no longer use.

**Non-Goals:**

- A chart hold-back mechanism (nothing needs one).
- Changing what any list suppresses, or the reports' outcomes.
- The `.idea/` references (git-ignored, other tools'), the archived change dirs, and the dated `docs/audits/` reports
  (historical records, which a later change must not rewrite into the present tense).

## Decisions

- **Put the helm parse in `HelmUpdateRules`, beside `sameMajor`.** It is the object the helm task already delegates its
  rule to, so the parse belongs with the rule it serves; a shared parser across both rule objects was considered and
  rejected — `DependencyUpdateRules`'s `entryLine` also carries the `=value` half only the hold-back list needs, and
  exporting it would couple two plugins' rules for a four-line duplication.
- **Parse with the same shape (blank/`#` skipped, entry before the first `=`), even though chart names carry no `=`.**
  It keeps one documented format across the three lists and makes the parse's behaviour pinned rather than implied.
- **Rename all three to `.txt`.** A line list is not Java properties; the extension is what let the colon-split defect
  read as an implementation detail rather than a format error. Options considered: a per-list extension (rejected —
  "sometimes `.properties`, sometimes `.txt`" is the inconsistency being removed); leaving the extensions (rejected —
  the whole point of the parked idea, and the helm file's `.properties` stops being accurate the moment it is
  line-parsed).
- **Move the references in the same change as the files.** The loader paths, `AGENTS.md`, `README.md`, `docs/adr/0007`
  and `docs/adr/0011` (each cites a suppression path), the `/dependency-updates` command (two sites in its prose), and
  the architecture-auditor's swept surfaces — its definition, the `/audit-architecture` command, and the `agent-skills`
  requirement that names both dependency paths — all name the old paths; a rename that left them would leave dead
  references, which the repo's own enumeration-sweep rule exists to prevent.

## Risks / Trade-offs

- **A rename can leave a reference behind.** → The tasks sweep the live references with a grep for the old paths, and
  the archived change dirs and `.idea/` are explicitly out of scope.
- **The helm list's parse change could drop entries.** → `HelmUpdateRules`' parse is unit-tested and the `helmUpdates`
  report is compared before/after the change (same charts suppressed, same report sections).
- **`.txt` for a config file may read as unusual.** → It matches the two lists this change already made line-parsed, and
  `config/` also holds `.xml`, `.yaml`, and `.properties` (the JaCoCo baseline, which really is properties).

## Migration Plan

- Apply: the parse, the wiring, the tests, the three renames, and the reference sweep.
- Rollback: revert the commit; the loaders and paths revert with it. No deployed state is affected.
