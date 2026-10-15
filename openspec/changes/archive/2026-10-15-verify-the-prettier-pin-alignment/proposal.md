# Proposal

## Why

The root's markdown Prettier version and the web module's resolved one must agree, and nothing guards the agreement: the
catalog's `[versions]`-only `prettier` entry is read by no update check, and the web module's npm report watches only
its own declaration. Their divergence (3.9.6 beside 3.9.10) is what left a markdown file silently ungated — Spotless
skips a file it cannot converge on, with a zero exit — and a future npm bump of the web module's Prettier would recreate
it silently, because the report would move one declaration and not the other. The figures are in this change's claims
table (`design.md`).

## What Changes

- **A `verifyPrettierAlignment` check**: the catalog's `prettier` version SHALL equal the version the web module's
  lockfile resolves, and the check SHALL fail `check` naming both when they differ — or when either side records no
  Prettier at all, since a missing entry is the inconsistency the check exists to catch.
- **`build-logic`** — `PrettierAlignmentRules` holds the parsing and the comparison (pure, so it is testable without
  Gradle), and `PrettierAlignmentRulesTests` carries the known-bad and known-good cases the check-control rule requires:
  an aligned pair, a drift, a missing entry on either side, an unreadable lockfile, and the two parse boundaries a naive
  read would cross (`prettier-plugin` and `my-prettier`).
- **`check`** gains the task, and `AGENTS.md`'s enumeration of `check`'s members is refreshed with it.
- **`showcase/quality/code-quality`** (delta) — the "Source formatting is enforced by the build" requirement states the
  version agreement, with a scenario for a drift.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/code-quality`: "Source formatting is enforced by the build" — the requirement that owns the root
  formatting's Prettier use, and so the declaration of the version it uses; the web module's own Prettier use is the
  sibling requirement's.

## Impact

- Files: `build-logic/src/main/kotlin/PrettierAlignmentRules.kt`,
  `build-logic/src/main/kotlin/VerifyPrettierAlignmentTask.kt`,
  `build-logic/src/test/kotlin/PrettierAlignmentRulesTests.kt`, `build.gradle.kts` (the task and its `check`
  membership), `AGENTS.md` (the `check`-members enumeration), `README.md` (its own enumeration of the `verify*` tasks),
  and this change's delta.
- The check reads two committed files — the catalog and the web module's lockfile — so it resolves nothing external and
  stays cacheable, and its failure mode is exercised by the rules' tests rather than by a checklist task asserting the
  control ran.
