# Proposal

## Why

`dependencyUpdates` runs in the main build, so it resolves what that build resolves — and of the twelve plugin pins
`build-logic` declares in its own `dependencies {}` block, exactly **one** reaches it (`spotless-plugin`, because the
root also applies `alias(libs.plugins.spotless)`); the other eleven resolve only on the included build's buildscript
classpath and appear nowhere. ADR-0015 makes identifying each catalog entry's checker a duty, and records the surface as
deliberately split between the dedicated checks and a hand-audit remainder; the pins sit in that remainder by accident.
The cost is measured: `helm-plugin` sat at `3.1.2` from 2026-09-27, when `3.2.0` was published, until 2026-10-15, when
unrelated work needed it — three scheduled report runs in between, none listing a helm row.

## What Changes

- **`build-logic`** gains a `BuildLogicUpdatesTask` with its helpers, in the shape the sibling update checks use: a
  declared coordinate list, a lookup per coordinate, and a report file registered in the root build.
- The declared list is the twelve `*-plugin` pins `build-logic`'s `dependencies` block consumes — the gap is exactly
  those, because the other nine catalog libraries it consumes (jackson, assertj, log4j, commons-lang3, spotbugs) also
  resolve in the main build and already appear in its report.
- The list is asserted against both files — every `-plugin` alias `build-logic/build.gradle.kts` consumes must be
  declared — so a pin added there fails the check until it is listed, rather than going uncovered the way the helm pin
  did.
- The check reads a latest release the way the report does: the Gradle Plugin Portal's metadata, put through the same
  stability rule and the same `config/dependency-updates/` suppression files, so a milestone, a deferred major, or a
  held-back line is not surfaced as an update. A lookup that yields no readable version is reported as unread, never as
  current.
- **`.github/workflows/dependency-updates.yml`** runs the check alongside the existing reports and includes its
  actionable rows in the same "Dependency updates" issue, so a lagging pin is visible where the other updates are.
- **Docs**: `AGENTS.md`'s per-check inventory and both of its workflow coverage sentences, the README's report lists and
  its suppression-coverage sentence, and `.opencode/commands/dependency-updates.md`, which today tells an agent to run
  two reports and would otherwise keep omitting this one; and the parked `docs/ideas.md` entry this change implements.
- No ADR change: ADR-0015's Decision — single-sourcing with each entry's checker identified, and a hand-audit remainder
  — is what this implements; the remainder shrinks by the twelve pins.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/merge-governance`: the "Dependency update report runs on a schedule and on demand" requirement gains
  the third check — the build-logic pins — and the issue's section for it.
- `showcase/quality/dependency-management`: three new requirements for the `buildLogicUpdates` report — its scope (the
  pins the included build resolves and the main report cannot see), its filtering (the Gradle report's stability rule
  and suppression files), and its coverage assertion against the included build's own build script.

## Impact

- **Build**: `build-logic` (a task and its helpers) and the root build's registration. Nothing runs in the merge gate.
- **Network**: one metadata request per coordinate, so a weekly run makes twelve small lookups — against the resolution
  walk a second `dependencyUpdates` would have cost, had that been possible in the included build.
- **Observable**: a newer stable release for a `build-logic` pin appears in the "Dependency updates" issue; today no row
  exists for eleven of the twelve.
- **Docs**: `AGENTS.md`, `README.md`, `.opencode/commands/dependency-updates.md`, and the parked `docs/ideas.md` entry
  this change implements.
