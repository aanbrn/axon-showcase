# Design

## Context

See `proposal.md` — Why. Current state that shapes the approach:

- The `gradle-versions-plugin` (0.61.0) `DependencyUpdatesTask` has a `revision` property whose default is `milestone`;
  its `PlainTextReporter` labels the sections `The following dependencies are using the latest <revision> version:` and
  `The following dependencies have later <revision> versions:` (plus `Gradle CURRENT updates:`). It has no
  `have newer versions:` header — that string exists only in the workflow's `awk` and the repo's prose.
- `dependency-versions-conventions.gradle.kts` sets `gradleReleaseChannel = "CURRENT"` but never `revision`, so the
  report uses the `milestone` default.
- The workflow's `awk` starts at `^The following dependencies have newer versions:` and stops at
  `^The following dependencies have later milestone versions:` / `^Gradle CURRENT updates:` — so its catalog section is
  always empty.
- Verified: `./gradlew dependencyUpdates` and `-Drevision=release` produce the **same 28 actionable rows** (the repo's
  `rejectVersionIf` filters non-stable candidates either way); only the section label differs.
- `.opencode/commands/dependency-updates.md` (and the `merge-governance` scenario) encode the same false premise — that
  a `have newer versions` section exists and the milestone section is non-actionable.

## Goals / Non-Goals

**Goals:**

- Make the tracker report the report's stable-catalog section (`have later release versions:`), so the 28 stable catalog
  updates reach the "Dependency updates" issue.
- Remove the false "milestone section is non-actionable" premise from the spec and the command.

**Non-Goals:**

- Changing the report's candidate set (verified unchanged) or its `major-disabled` suppression; the web UI npm report.

## Decisions

- **Set `revision = "release"`.** It labels the actionable section `have later release versions:` — unambiguous,
  matching the spec's "stable catalog updates" wording and the plugin's getting-started example — while producing the
  same 28 rows as the `milestone` default. Rejected: leaving the default and matching `have later milestone versions:`,
  which keeps a label the repo's prose misreads as non-actionable.
- **Fix the extraction to the emitted header** (`have later release versions:`), stopping at `Failed to ` and
  `Gradle CURRENT updates:` — the plugin emits its `Failed to …` sections (undeclared, unresolved, skipped) between the
  upgrades section and the Gradle one, so they need their own boundary rather than relying on a non-matching header.
- **Correct the spec and command wording** (the false premise), rather than only the workflow — they are the durable
  statements a future reader follows.

## Risks / Trade-offs

- **The tracker's issue gains 28 catalog rows on its next run** → intended; they are the actionable stable updates the
  workflow was built to report.
- **The `awk`'s stop conditions must track the report's sections** → the extracted block ends at `Failed to ` or
  `Gradle CURRENT updates:`; a single stop would sweep the plugin's `Failed to …` sections (undeclared, unresolved,
  skipped) into the catalog updates.

## Migration Plan

- Apply: set `revision`, fix the `awk`, correct the spec/command/doc prose.
- Rollback: revert the edits (the report's set is unchanged either way).
