# Proposal: Block major updates for JGroups with documented rationale

## Why

A `org.jgroups` entry was added to `config/dependency-updates/major-disabled.properties` ad hoc, without recording why.
The `dependencyUpdates` report would otherwise surface a JGroups 5.x jump that the project cannot act on: JGroups is
used only through the Axon Framework's distributed command bus, and the surrounding ecosystem is locked to JGroups
4.x. We need the entry, and we need the rationale captured as part of the spec-driven workflow so the suppression is
reviewable and repeatable.

## What Changes

- `config/dependency-updates/major-disabled.properties`: re-add the `org.jgroups` group prefix to the major-disabled
  list, now with a comment that points at the captured rationale.
- `openspec/specs/showcase/quality/dependency-management/spec.md`: document that the major-disabled list ships with
  the `org.jgroups` group because a JGroups 5 migration is not actionable while Axon's JGroups extension and
  `jgroups-kubernetes` (KUBE_PING) target JGroups 4.x.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-management`: the shipped major-disabled list gains `org.jgroups`; the requirement
  gains the rationale for why this coordinate's major updates are suppressed.

## Impact

- **Code**: `config/dependency-updates/major-disabled.properties` (modified) — a config file, no runtime code.
- **Specs**: `openspec/specs/showcase/quality/dependency-management/spec.md` (modified via delta, synced on archive).
- **Build**: `./gradlew dependencyUpdates` no longer lists a JGroups 5.x major jump for `org.jgroups:jgroups` and
  `org.jgroups.kubernetes:jgroups-kubernetes`; 4.x minor/patch updates remain visible.
- **Tests**: no test changes; verification runs `dependencyUpdates` and confirms the JGroups major jump is suppressed
  while 4.x updates are still reported.