# Proposal

## Why

`verifyInstallCommands` asserts the `AGENTS.md` manual `helm install … --version` commands against the catalog chart
pins, so a chart bump cannot leave a documented command stale. But the docs also state the **Paketo** pins in prose —
the buildpack builder and the buildpacks — and nothing asserts them; the 2026-10-12 buildpack bump left two pins stale
in a doc. The check's coverage is a hand-written subset of the pins a live doc states, and a hand-written list
under-describes what a grep returns.

## What Changes

- **`build-logic`** — a `VerifyDocumentedPinsTask` (and its rule object) that derives the pins a live doc names by
  grepping the docs, and asserts each against the catalog coordinate it belongs to; registered as `verifyDocumentedPins`
  in `build.gradle.kts` and added to `check`.
- **The catalog/docs** — the buildpack and builder pins stay as they are; the check's inputs are the catalog coordinates
  and the live docs.
- **`showcase/quality/infra-image-versions`** (delta) — the single-sourcing requirement's documented-pin assertion
  widens to require a live doc to state the catalog's Paketo pins at their catalog versions.
- **`AGENTS.md`** and **`README.md`** — their `check`-member / `verify*` enumerations name the new task; `AGENTS.md`'s
  pin-verification note states the widened check.
- **`docs/retrospectives/2026-10-13.md`** — S2's row records this change as applied.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/infra-image-versions`: the "Infrastructure image references are single-sourced" requirement's
  documented-pin assertion widens from the `helm install` commands to the catalog's Paketo pins a live doc states.

## Impact

- Files: `build-logic/src/main/kotlin/VerifyDocumentedPinsTask.kt`, `build-logic/src/main/kotlin/DocumentedPinRules.kt`,
  `build-logic/src/test/kotlin/DocumentedPinRulesTests.kt`, `build.gradle.kts` (the task registration + `check`), the
  `showcase/quality/infra-image-versions` delta, `AGENTS.md` (its pin-verification note and `check`-member enumeration),
  `README.md` (its `verify*` enumeration), and `docs/retrospectives/2026-10-13.md` (S2's row).
- A `check` member, so it runs on every PR and push to `main`; it resolves nothing external (the catalog holds the
  expected versions), so it stays Docker-free and cacheable.
