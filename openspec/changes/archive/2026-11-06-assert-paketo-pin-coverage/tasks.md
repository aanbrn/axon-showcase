# Tasks — assert every catalog Paketo pin has a check entry

The unit's record is `docs/units/assert-paketo-pin-coverage/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with `python3 scripts/claim-scan.py` over the staged
      artifacts; each candidate is resolved into `design.md`'s claims table or dismissed beside it, which states one
      reason per candidate kind. Verify: `design.md` carries the table and the dismissal line, and every scanned
      candidate is accounted for. Done: the scan's candidates are the change's own statements and paths plus the claims
      table's figures; `design.md`'s dismissal line states one reason per kind.

## 1. The rule

- [x] 1.1 `build-logic/src/main/kotlin/PaketoPinCoverageRules.kt`: the pure rule — the catalog's Paketo keys against the
      check names, normalizing the alias form (`paketo.nginx`) to the key form (`paketo-nginx`) — returning the
      uncovered keys and the entries naming an absent key, each declaration carrying its KDoc in the shape
      `DocumentedPinRules` takes. Verify: its tests cover each arm (see 1.2). Done: `PaketoPinCoverageRules` holds
      `catalogKeys` (alias to key form, Paketo prefix selected), `uncovered` and `unknown`, each with its KDoc.

- [x] 1.2 `build-logic/src/test/kotlin/PaketoPinCoverageRulesTests.kt`: a fully covered catalog passes; a catalog key
      with no check is returned as uncovered and named; a check naming an absent key is returned as unknown; and the
      alias-to-key normalization is asserted on a dotted alias. Verify: each case fails against a mutation of the arm it
      covers. Done: `PaketoPinCoverageRulesTests` decides seven arms — fully covered, an uncovered pin, an unknown
      entry, both directions at once, a dotted alias, a non-Paketo alias, and an empty catalog.

- [x] 1.3 `VerifyDocumentedPinsTask`: one new input (the catalog's version aliases, from which the rule selects the
      `paketo-*` keys; the check names are the keys of the `paketoPins` map it already receives), the coverage rule run
      before the documented-pin comparison, and a failure naming the uncovered and unknown keys. Verify: the task's
      description and its KDoc state the coverage, and the failure message names both lists. Done: the task takes one
      new input (`catalogAliases`), runs the coverage before the documented-pin comparison, and fails naming the
      uncovered keys ("The catalog pins Paketo coordinates the build does not check") or the unknown entries ("The build
      checks Paketo coordinates the catalog does not pin"); its KDoc and description state it.

- [x] 1.4 The root `build.gradle.kts`: pass the catalog's version aliases (read through the `VersionCatalogsExtension`)
      as the one new input, from which the rule selects the `paketo-*` keys; the check names come from
      `buildpackChecks`, which the task already receives as its pin map. Verify: `./gradlew verifyDocumentedPins` passes
      on the current catalog, and the task's inputs change when a check is added. Done: the root script captures the
      catalog's version aliases through the `VersionCatalogsExtension` into a top-level `val` and passes it to the task;
      the aliases had to be read eagerly, since resolving the extension inside the task's configuration block fails.

## 2. Verification

- [x] 2.1 `./gradlew :build-logic:test` and `./gradlew verifyDocumentedPins`. Verify: both pass, and the rule's tests
      decide each arm. Done: `:build-logic:test` and `verifyDocumentedPins` both pass; the latter prints the five stated
      pins it verifies.

- [x] 2.2 The wiring control, on a disposable copy of the working tree under `$TMPDIR/opencode` (its uncommitted changes
      included, `build/` and `.gradle/` excluded): the baseline `verifyDocumentedPins` passes; a catalog-only `paketo-*`
      pin inserted inside the copy's `[versions]` block (not appended — the file ends in `[plugins]`) makes the catalog
      load and then the gate fail naming that key; and a renamed check entry, with the catalog restored, makes it fail
      naming the catalog key it left uncovered (the other direction needs an entry the catalog lacks while the catalog
      stays covered). Verify: each of the three outcomes is recorded with its command, and the baseline also settles the
      alias representation the rule normalizes. Done, on the disposable copy `$TMPDIR/opencode/paketo-wiring` (the
      working tree with its uncommitted changes, `build/` and `.gradle/` excluded): the baseline passed; a
      `paketo-coverage-probe = "1.0.0"` line inserted inside `[versions]` made the catalog load and the gate fail with
      "The catalog pins Paketo coordinates the build does not check: paketo-coverage-probe"; restoring the catalog and
      adding an entry named `paketo-retired` made it fail with "The build checks Paketo coordinates the catalog does not
      pin: paketo-retired". A renamed entry was also observed to report its key as uncovered.

- [x] 2.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`. Verify: both pass.
      Done: `check -PskipITs -Pcoverage.gate.enabled=false` green (1 m 12 s after the final edits) and
      `openspec validate --all` reports 27.

- [x] 2.4 `./gradlew spotlessApply` after the last edit. Verify: the tree carries no formatter change. Done:
      `spotlessApply` ran after the last edit and `spotlessCheck` passes.

## 3. The durable records

- [x] 3.1 `docs/ideas.md`: remove the entry this change implements. Verify: the entry is gone and its section keeps its
      other entries. Done: the entry is gone from `docs/ideas.md` and `verifyIdeasSections` passes.

- [x] 3.2 `openspec/specs/showcase/quality/infra-image-versions/spec.md`: refresh the `## Purpose` only if the added
      requirement widens a class it names; a delta cannot carry a Purpose, so an edit rides the archive commit. Verify:
      either the Purpose is unchanged and says so here, or it names the coverage and the requirement's text is
      untouched. Decided: the Purpose is refreshed in the archive commit — its sentence names the gate the change widens
      ("a build gate verifies each documented pin against its catalog coordinate"), and the gate now also asserts that
      the catalog's Paketo pins and the build's check list cover the same set, so the class the Purpose names has
      widened. Done: the Purpose named the gate this change widens, so its sentence now also states that the catalog's
      Paketo pins and the build's update-check list cover the same set. Applied in the archive commit.

- [x] 3.3 The two packs that describe the gate: `.opencode/skills/deployment-operations/SKILL.md` (its
      `verifyDocumentedPins` sentence) and `.opencode/skills/gradle-build/SKILL.md` (the buildpack-pin bullet, or its
      absence) — each states that the catalog's Paketo pins and the check list move together. Verify: both name the
      gate, and no live site still describes `verifyDocumentedPins` as checking only the docs' stated pins. Done: the
      `deployment-operations` pack's `verifyDocumentedPins` sentence and the `gradle-build` pack's catalog bullet both
      state the two-way coverage.

## 4. The review gate and the records

- [x] 4.1 The proposal stage's quick review: `review-quick` over the planning artifacts until a clean round, with each
      round's row and findings under `docs/units/assert-paketo-pin-coverage/`. Verify: `--expect-clean` and
      `--session-continuity` pass. Done: rounds 1–3 in `rounds.tsv` (5 → 2 → 0 findings), the closing round 3 with no
      finding class; `--expect-clean`, `--session-continuity` and `--review-ready` pass.

- [x] 4.2 The owner's manual pass over the proposal. Verify: the approval is recorded before the implementation starts.
      Done: the owner approved the proposal and instructed implementation in one message ("Одобряю. Реализуй."), after
      the advisor's pass closed in agreement; nothing was implemented before it.

- [x] 4.3 The implementation stage's quick review. Verify: the closing round is clean and the ledger checks pass. Done:
      rounds 4–7 in `rounds.tsv` (6 → 3 → 1 → 0 findings), the closing round 7 with no finding class; `--expect-clean`,
      `--session-continuity` and `--review-ready` pass.

- [x] 4.4 The deep-review routing decision on the completed diff. Verify: the outcome and its signals are recorded.
      Done: `python3 scripts/review-routing.py` over this unit and change reports RECOMMENDED on one `[repeated-class]`
      signal (four rows repeat a prior class, all in the string-review rounds); the owner resolves whether to run the
      deep review.

- [x] 4.5 The deep review, when 4.4 selects one, with its findings applied and re-reviewed. Verify: the pass's status
      and its findings' disposition are recorded. Done: the advisor recommended the pass and the owner's instruction to
      continue carried it. `review-thorough` reported nothing remaining: every changed file's diff against `d15be7ac`
      preserves its pre-existing content (the round-5 recovery confirmed byte-identical), each normative clause of the
      added requirement traces to `catalogAliases` → `PaketoPinCoverageRules` → a named failure, the aliases were probed
      and come from `getVersionAliases()` in the dotted form the rule reverses, and every Done note reproduces. Its one
      residual limitation — no committed test asserts the wiring — is the one the design already records.

- [x] 4.6 The technical advisor's consultation on the implementation, when the owner authorizes it. Verify: the
      authorization and the findings' disposition are recorded. Done: the owner authorized the pass ("Уточни у
      советника") at the proposal stage and again on the implementation, where the consultation arbitrated the
      continuity violation and the deep-review question. It confirmed the two artifact corrections (the cache claim's
      scope, the process-defect record) and recommended the bounded deep review, whose result is task 4.5. A second
      consultation ("Отревьювь с советником до согласия") asked whether the unit ships as implemented; it recommended
      proceeding to the owner's manual pass with no further fix, and its one falsifying condition — a catalog-only
      Paketo pin leaving the gate green — was probed and did not hold (recorded in the hand-over).
- [x] 4.7 The owner's manual pass over the finished unit. Verify: the approval is recorded, and nothing is committed,
      pushed, or merged before it. Done: the owner approved on 2026-11-06 ("Одобряю. Отгружай."), together with the
      separate delivery instruction. Approval followed the second advisor consultation and the probe of its falsifying
      condition.

- [x] 4.8 `docs/units/assert-paketo-pin-coverage/process-defects.md`: one record per defect observed, or the statement
      that none was. Verify: the file exists before 4.7 is requested. Done: four records — `#1` a pack edit that deleted
      three sentences and a marker while moving one clause; `#2` the reviewer session replaced without an evidenced
      recovery case; `#3` two records created outside the unit's regulated layout; and `#4` the agent's deviation from
      the regulated process, on the owner's report.
