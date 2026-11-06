# Assert every catalog Paketo pin has a `buildpackChecks` entry

## Why

The catalog's Paketo pins are tracked by a hand-maintained list: `buildpackChecks` in `build.gradle.kts` names each
builder, buildpack and image pin with the repository it is released from. Two gates derive from that list —
`buildpackUpdates` reports a newer release, and `verifyDocumentedPins` verifies the version each live doc states — so a
catalog `paketo-*` entry added without a list entry is invisible to both: nothing reports it stale, and nothing requires
the pack to state it. The list is the single point a new pin can slip through, and only a reviewer reading the build
script would notice.

## What Changes

- `build-logic`: a `PaketoPinCoverageRules` object holding the coverage rule as pure logic — the catalog's `paketo-*`
  keys against the check names — so it is unit-tested without Gradle.
- `VerifyDocumentedPinsTask` gains one input — the catalog's version aliases, from which the rule selects the `paketo-*`
  keys — runs the coverage rule before the documented-pin comparison, reading the check names from the map it already
  receives, and fails with the uncovered and the unknown keys named.
- The root `build.gradle.kts` passes the catalog's version aliases (read through the catalog extension, so nothing
  parses the TOML by hand, and the rule selects the `paketo-*` keys from them); the check names ride the map the task
  already receives.
- `build-logic/src/test`: `PaketoPinCoverageRulesTests` — a fully covered catalog passes, a missing check is named, a
  check naming a key the catalog lacks is named, and the alias-to-key normalization is asserted.

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/infra-image-versions` — the Paketo-pin requirement gains the coverage condition: `check` asserts
  that every catalog `paketo-*` pin has a `buildpackChecks` entry, and that every entry names a pin the catalog holds.

## Impact

- **Build logic**: `build-logic/src/main/kotlin/PaketoPinCoverageRules.kt`, `VerifyDocumentedPinsTask.kt`, and a new
  test class beside `DocumentedPinRules`'s.
- **Build script**: the root `build.gradle.kts` passes one more input to `verifyDocumentedPins`, the catalog's version
  aliases; the check names are already the map's keys, and the rule selects the Paketo keys from the aliases.
- **Behavior, dependencies, deployment**: none — the task already runs in `check`; it now fails on a case it did not
  see.
- **Verification**: `build-logic`'s tests for each arm of the rule, `verifyDocumentedPins` on the current catalog (which
  is fully covered), and a wiring control on a disposable copy — a catalog-only pin and a renamed check entry, each
  expected to fail the gate naming what it found — since the pure tests cannot show that the build supplies the
  catalog's set.
