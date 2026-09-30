# Design

## Context

See `proposal.md` — Why. Current state that shapes the approach:

- The `dependencyUpdates` report is produced by the `gradle-versions-plugin` `DependencyUpdatesTask`, filtered by a
  single `rejectVersionIf` closure in `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts`. It
  rejects non-catalog coordinates and, via `matchesDisabled` + `isMajorBump`, any candidate whose major version exceeds
  the current one for a coordinate listed in `config/dependency-updates/major-disabled.properties`. The closure sees
  each candidate version, so a rejected candidate is simply excluded and the report lists the newest non-rejected one —
  the behavior the spec's "Same-major fallback is reported when a major jump is the newest" scenario relies on.
- **The suppression file is mis-parsed today.** It is loaded with `java.util.Properties.load`, which splits a key at its
  first `:`/`=`/whitespace, so `org.opensearch.client:spring-data-opensearch` becomes the key `org.opensearch.client`
  with `spring-data-opensearch` as its value. Loading the shipped file yields the keys
  `[org.axonframework, org.flywaydb, org.jgroups, org.opensearch.client, org.springdoc, org.springframework]` — the four
  exact-coordinate entries collapse to group prefixes, `matchesDisabled`'s exact-coordinate branch is dead, and the
  suppression covers the whole `org.opensearch.client` group (including `opensearch-java`/`opensearch-rest-client`),
  which the `dependency-management` spec's transport-client clause says must remain reported. A `group:module=<line>`
  hold-back key would be destroyed the same way.
- The suppression predicates (`isNonStable`, `isCalendarVersioned`, `isMajorBump`, `versionTrain`, `leadingInteger`,
  `matchesDisabled`) live inline in the precompiled convention script, so they are not unit-testable; the repo's pattern
  for such rules is a pure object in `build-logic/src/main/kotlin/` (e.g. `HelmUpdateRules`, `InfraImageVersionRules`,
  `ModuleDependencyRules`) with a `*Tests` class beside it.
- A shared numeric version ordering already exists: `Versions.isNewer` (zero-padded segments, so `3.9` and `3.9.0`
  compare equal) is used by the update checks and the version-comparison tests.
- `dependency-management` currently describes suppression as a **major-only** concept;
  `org.opensearch.client: opensearch-java` is catalog-owned at `3.9.0` and deliberately not in the major-disabled list
  (whose spring-data-opensearch requirement explicitly keeps the transport clients' majors reported).

## Goals / Non-Goals

**Goals:**

- Give the project a config-driven way to stop the tracker listing a same-major update it cannot take, without hiding
  the updates it can.
- Read both suppression files so a `group:module` key keeps its colon, restoring the exact-coordinate suppression the
  `dependency-management` spec already describes.
- Make the new rule unit-testable, so its boundary behavior is pinned rather than inferred from a full report run.

**Non-Goals:**

- Changing the major-disabled policy beyond restoring the exact-coordinate behavior the spec already states; the parser
  fix adds no new major-suppression decision.
- Changing the calendar-train heuristic or the catalog-ownership filter.
- Suppressing a whole coordinate (blanket) or hiding its major jumps.
- Reworking and renaming the suppression lists off the `.properties` extension: the two
  `config/dependency-updates/*.properties` files are line-parsed lists after this change, and the helm list is the last
  one still read with `Properties`, so the rework (a `HelmUpdateRules` line parse beside the tested `sameMajor`) plus
  the rename is its own change, parked in `docs/ideas.md` — not a new capability, and no chart hold-back.
- The web UI npm report's majors (its own parked idea) and the spurious `log4j-core` row (ADR-0007).

## Decisions

- **A separate `config/dependency-updates/hold-back.properties`, not entries in `major-disabled.properties`.** The
  existing file's name, header, and spec requirements all state a major-only contract; a hold-back is a different
  decision (a version-line ceiling for a binary incompatibility), with a different rationale class. Options considered:
  overloading `major-disabled.properties` with `group:module=<line>` values (rejected: it makes the file's name and its
  documented "minor and patch stay reported" contract lie, and would require rewording the major-disabled requirements);
  a blanket per-coordinate suppression (rejected: hides the safe same-line patches and the coordinate's majors).
- **The entry names the version line to hold at (`major.minor`), and the rule suppresses same-major candidates on a
  newer line only.** For `3.9` held, `3.10.0` is suppressed, `3.9.1` stays reported, and `4.0.0` stays reported under
  the major-update policy. Options considered: naming the exact incompatible version (`=3.10.0`) and rejecting anything
  newer (rejected: a `3.10.1` patch in the incompatible line would slip through, and the incompatibility is a line, not
  one build); rejecting everything newer (rejected: the transport-client majors requirement, and the repo's
  per-coordinate stance that non-blocked updates stay visible, both argue for keeping the line's patches and majors
  reported).
- **Parse both suppression files line by line, splitting on the first `=`, instead of `Properties.load`.** A
  `group:module` key must keep its colon; `Properties` treats `:` as a key/value separator, which is exactly the defect
  above. Options considered: escaping the colon (`org.opensearch.client\:opensearch-java=3.9`, rejected: it contradicts
  the file's own documented `group:module` format and is easy to get wrong); keeping `Properties` with a colon-free key
  spelling (rejected: the coordinate convention is `group:module` everywhere else in the repo). The `.properties`
  extension is kept for now even though the file is no longer read as Java properties; renaming it is deliberately its
  own change (see Non-Goals).
- **Extract the config parsing and the whole `rejectVersionIf` decision into a pure `DependencyUpdateRules` object, and
  have the script call it.** The hold-back predicate is a comparison with boundary cases worth a test (`3.9` vs `3.9.1`
  vs `3.10.0` vs `4.0.0`), the parse rule needs a test of its own (a `group:module` key survives; a bare group stays a
  prefix), and the inline script functions cannot be unit-tested. Options considered: adding the predicate inline in the
  script (rejected: untestable, and it would leave the sibling predicates there while adding a third comparison beside
  them); extracting only the hold-back predicate (rejected: it would split the decision across two places and leave the
  parse rule untested).
- **Compare version lines with `Versions.isNewer` on the `major.minor` train, not on whole versions.** `3.9` and `3.9.1`
  share the line `3.9`, so a whole-version comparison would wrongly reject the patch; the train comparison is the
  meaningful unit and reuses the shared numeric ordering.
- **Widen the architecture-auditor's swept suppression surfaces (its definition and the `agent-skills` requirement)
  rather than leave the new list un-swept.** A held-back coordinate is a deliberate choice whose rationale must be
  recorded, exactly like a major-suppressed one; an enumeration that reads complete while omitting it is the drift the
  repo's own rules warn about.
- **Record the hold-back rationale in the `dependency-management` spec (the authoritative home) with a pointer comment
  in the config**, mirroring how `major-disabled` entries point at the spec.

## Risks / Trade-offs

- **The report's "newest non-rejected" behavior is assumed, not re-derived.** → Pin the predicate with unit tests, then
  verify end to end by running `./gradlew dependencyUpdates` and reading the report: the `opensearch-java` row is
  absent, and no other catalog-owned row moves.
- **The parser fix changes which candidates are suppressed for the four exact-coordinate entries** (the
  `org.opensearch.client` pseudo-prefix becomes the three spring-data-opensearch modules, so
  `opensearch-java`/`opensearch-rest-client` majors become reported). → That is the behavior the spec's transport-client
  scenario already requires; pin it with a unit test that an exact-coordinate entry matches only its module, and read
  the end-to-end report to confirm no unrelated coordinate appears.
- **The hold-back is a second suppression surface to retire when ADR-0004 lands.** → The shipped entry and its
  requirement name ADR-0004 as the reopen condition, so the retirement is discoverable when `spring-data-opensearch` 3.x
  becomes adoptable.
- **A second config file adds a surface the architecture-auditor must sweep.** → The same change widens the auditor's
  surface list and its spec requirement.

## Migration Plan

- Apply: add the config file, the rules object and tests, wire the convention script (including the colon-safe parse),
  and refresh the docs.
- Rollback: revert the change; the report returns to listing `3.10.0` for the held coordinate. No runtime or deployed
  state is affected.
