## Why

The command service's `showcaseCache` maximum size is stated two ways: the Java `@ConfigurationProperties` default is
`1000`, while `application.yml` and the Helm chart both say `100000`. Since the yml placeholder is always bound, the
Java default is dead in every environment — the failure ADR-0002 exists to prevent. The component test passes only
because its yml-defaults check omits `showcaseCache`.

## What Changes

- Reconcile the surfaces to the value the repository already treats as intended — `100000` — by making the Java
  `@ConfigurationProperties` default authoritative at that value, mirroring how `align-gateway-cache-defaults` resolved
  the same drift for the gateway caches.
- Update the write-side spec's stated default to match.
- Add `showcaseCache` to the component test's yml-defaults check, which is what let the drift pass.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/write-side/command-service`: the configurable-caching requirement's stated default for the showcase cache is
  reconciled with the deployed value, and the cross-surface agreement it depends on is asserted.

## Impact

- `showcase-command-service/src/main/java/showcase/command/ShowcaseCommandProperties.java` — the Java default becomes
  `100000`; `application.yml` and the Helm chart already carry it, so no deployed value changes.
- `showcase-command-service/src/componentTest/java/showcase/command/ShowcaseCommandPropertiesCT.java` — its
  Java-defaults assertion moves to `100000`, and its yml-wiring test gains the `showcaseCache` assertion that was
  missing.
- `openspec/specs/showcase/write-side/command-service/spec.md` — synced at archive.
- `docs/ideas.md` — the change's own parked idea is removed on this branch.
- No API or deployed behavior change: every environment already runs `100000`; this makes the code and the spec say so.
