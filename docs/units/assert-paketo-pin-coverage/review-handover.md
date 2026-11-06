# Review hand-over — assert-paketo-pin-coverage

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `assert-paketo-pin-coverage`; tree `/Users/afanasyev/Development/axon-showcase` (the
  primary checkout, this unit's tree); branch of the same name; base `origin/main` `d15be7ac`; nothing committed, every
  unit file staged.
- **Packs** — `gradle-build`, loaded in full: the change decides a build gate, a `build-logic` rule, and a task input.
- **Supplied artifacts** — `proposal.md`, `design.md` (Context, Goals/Non-Goals, five decisions with alternatives,
  risks, verification, the claims table and its dismissals), `tasks.md`, `.openspec.yaml`, the delta
  `specs/showcase/quality/infra-image-versions/spec.md`; the unit record.
- **The proposed change** — a pure `PaketoPinCoverageRules` in `build-logic` comparing the catalog's `paketo-*` keys
  with the `buildpackChecks` names; `VerifyDocumentedPinsTask` gains both sets as inputs and runs the coverage before
  the documented-pin comparison; the root build script passes the catalog's version aliases (through the catalog
  extension) and the check names; a test class covers each arm.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate assert-paketo-pin-coverage --strict` passes with the delta; `spotlessApply`
  has run; `commit-hygiene --review-ready` passes.
- **Known limitations** — the rule is verified by its own unit tests and by the gate on the current (fully covered)
  catalog; no fixture adds a pin to the real catalog, by design.

## Round 2 — proposal screen, after round 1's five findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d15be7ac`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 1's fixes: the missed-pin precedent is attributed to `toolingUpdates`, where the
  repository records it, and the point is that this list has no equivalent assertion; the input model is one new input
  (the catalog's key set), with the check names read from the map the task already receives; the dismissal line's
  `absolute` and `count` reasons now describe the scan's actual candidates; the sweep names both packs that describe the
  gate; and tasks 1.1 and 1.3 carry the KDoc updates.
- **Prior findings with their instances** — round 1's five, in `findings/round-1.md`.
- **Completed checks** — `openspec validate assert-paketo-pin-coverage --strict` passes; `spotlessApply` has run;
  `verifyTableRows` passes; `commit-hygiene --review-ready` passes.
- **Known limitations** — the rule is verified by its own unit tests and by the gate on the current, fully covered
  catalog.

## Round 3 — proposal screen, after round 2's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d15be7ac`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 2's fixes, swept across every site rather than the named ones: the input model is one
  new input in the proposal's What Changes, its Impact, the design's risk bullet and task 1.4; and the dismissal line's
  `absolute` examples now quote the artifacts' own statements (the title, the design's "no parser", the Context's
  "`buildpackChecks` has no such assertion"), with the invented phrase gone.
- **Prior findings with their instances** — rounds 1–2 in `findings/`.
- **Completed checks** — `openspec validate assert-paketo-pin-coverage --strict` passes; `spotlessApply` has run;
  `verifyTableRows` passes; `commit-hygiene --review-ready` passes.
- **Known limitations** — the rule is verified by its own unit tests and by the gate on the current, fully covered
  catalog.

## Advisor pass — proposal

- **Result** — three rounds, closing in explicit agreement ("I agree with its direction"). Round 1 kept the unit and the
  mechanism and required a bounded wiring control, since the pure tests cannot show which set the build supplies; round
  2 corrected the control's insertion point (the catalog ends in `[plugins]`, so an appended probe would not be a
  version alias); round 3 reported nothing remaining.
- **What changed in the plan** — the design's Verification section now states the three-step control on a disposable
  copy (baseline, catalog-only probe inside `[versions]`, renamed check entry), task 2.2 carries it, and the proposal's
  Verification bullet says why the pure tests cannot replace it.
- **Its findings** — `[contradiction]` the plan verified the rule but not the new source boundary, so the pure tests
  stay green if the build supplies a check-derived set; `[claim-unreproducible]` the wiring control said to append the
  probe, which would land in `[plugins]` and fail without the gate deciding.

## Round 4 — implementation screen, first pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d15be7ac`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — the implementation: `PaketoPinCoverageRules` (alias normalization, uncovered, unknown) with
  its seven-arm test class; `VerifyDocumentedPinsTask`'s one new input and the coverage before the documented-pin
  comparison; the root script's eagerly captured alias set; the two pack sentences; the ideas removal.
- **Evidence** — the wiring control on `$TMPDIR/opencode/paketo-wiring`: baseline passed; a probe inside `[versions]`
  failed naming `paketo-coverage-probe`; an entry named `paketo-retired` failed naming it; a renamed entry reported its
  key as uncovered. `check -PskipITs -Pcoverage.gate.enabled=false` green (2 m 49 s); `openspec validate --all` 27.
- **Prior findings with their instances** — rounds 1–3 and the advisor's pass, in `findings/`.
- **Known limitations** — task 3.2 (the Purpose refresh) is decided at the archive commit; the wiring control ran on a
  disposable copy, so its failures are recorded rather than reproduced by `check`.

## Round 5 — implementation screen, after round 4's six findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d15be7ac`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 4's fixes: the input model is described as the catalog's version aliases with the rule
  selecting the `paketo-*` keys, at all four sites, and the design's cache bullet states the whole-set consequence; the
  design and task 2.2 say a rename reports the **uncovered** catalog key, with the unknown direction reached by adding
  an entry while the catalog stays covered; an arm now holds two uncovered and two unknown keys in an order the sort
  must change, and the test class is converted to JUnit 5, AssertJ and `@DisplayName` like its nineteen siblings; the
  pack's clause moved after the `buildLogicUpdates` qualifier; and the unit record's stale proposal pending decision is
  gone.
- **Prior findings with their instances** — rounds 1–4 and the advisor's pass, in `findings/`.
- **Completed checks** — the rule's tests green with the sort and prefix arms mutation-proved;
  `check -PskipITs -Pcoverage.gate.enabled=false` green (1 m 13 s); `openspec validate --all` 27; `spotlessCheck`,
  `verifyIdeasSections`, `verifyTableRows`.
- **Known limitations** — task 3.2 (the Purpose decision) is decided at the archive commit.

## Round 6 — implementation screen, after round 5's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d15be7ac`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 5's fixes: the `gradle-build` bullet is rebuilt from `HEAD`'s text with its three
  deleted sentences and the `captured:` marker restored, and the coverage rule appended as its own sentences before the
  marker; the three remaining input-model sites say the script passes every alias and the rule selects the `paketo-*`
  keys; and task 2.2's false trailing sentence is gone, with 2.3's figure updated to the post-fix run.
- **Prior findings with their instances** — rounds 1–5 and the advisor's pass, in `findings/`.
- **Completed checks** — `check -PskipITs -Pcoverage.gate.enabled=false` green (1 m 12 s); `spotlessCheck`,
  `verifyCapturedMarkers`, `verifyTableRows`, `verifyIdeasSections`; `openspec validate --all` 27.
- **Known limitations** — task 3.2 (the Purpose decision) is decided at the archive commit.

## Round 7 — implementation screen, after round 6's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d15be7ac`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 6's fix: the proposal's bullet no longer calls the aliases "Paketo version aliases", so
  every site now says the script passes the catalog's version aliases and the rule selects the `paketo-*` keys.
- **Prior findings with their instances** — rounds 1–6 and the advisor's pass, in `findings/`.
- **Completed checks** — `check -PskipITs -Pcoverage.gate.enabled=false` green (1 m 12 s); `spotlessCheck`,
  `verifyCapturedMarkers`, `verifyTableRows`, `verifyIdeasSections`; `openspec validate --all` 27.
- **Known limitations** — task 3.2 (the Purpose decision) is decided at the archive commit.

## Deep review — the advisor's recommended pass

- **Who and why** — `review-thorough`, in its own session `ses_e513433d6ffePRVYkn6MBukAeC`, run on the advisor's
  recommendation with the owner's instruction to continue. The ledger's round 8 records the session change as
  `replaced`/`definition-changed`: the deep pass runs `review-thorough`'s definition, not the quick reviewer's.
- **Its four asks, each closed** — every changed file's diff against `d15be7ac` preserves its pre-existing content (the
  round-5 recovery of the `gradle-build` bullet confirmed byte-identical); each clause of the added requirement traces
  through `catalogAliases` → `PaketoPinCoverageRules` → a named failure; `getVersionAliases()` was probed and returns
  the dotted form the rule reverses, so the wiring control's catalog-only probe is evidence the set comes from the
  catalog; and every `Done:` note reproduces from the tree.
- **Result** — "nothing remains". Its one residual limitation — no committed test asserts the wiring — is the one the
  design already records, since `build-logic` has no test kit and the task is registered in the root script.

## Round 9 — the IDE-warning fix

- **What raised it** — the owner asked whether IntelliJ's warnings on `build.gradle.kts` and the other changed files
  were real. The IDE's inspections were unreadable through Steroid (its script runtime fails on IDEA 2026.3), and the
  repository's own bundled MCP server reported `RemoveRedundantQualifierName` twice on
  `the<org.gradle.api.artifacts.VersionCatalogsExtension>()` — one on the line this change adds, one on the adjacent
  `helmClientVersion` that predates it.
- **The fix** — an explicit import, one shared `val versionCatalog = the<VersionCatalogsExtension>().named("libs")`, and
  both call sites read from it. `catalogVersionAliases` is still captured eagerly at top level, and `helmClientVersion`
  keeps the same `findVersion("helm").get().requiredVersion` semantics.
- **Verification** — `./gradlew help` compiles the script, `check -PskipITs -Pcoverage.gate.enabled=false` is green, and
  the IDE inspection now returns no items for `build.gradle.kts` and the three changed Kotlin files.
- **Review** — `review-quick`, round 9, "nothing remains": behavior preserved, no ordering hazard, no name collision,
  the file's staged diff carries nothing beyond the fix and the change's own two lines. Process-defect `#5` records the
  first hand-over's missing `Packs:` line, which the reviewer declined to review without.

## Second consultation — is the unit ready for the owner's pass?

- **Who and why** — `technical-advisor`, on the owner's instruction "Отревьювь с советником до согласия". The question
  was whether the unit proceeds to the owner's manual pass as implemented, or a defect requires a fix first.
- **Its verdict** — proceed, with no further fix: "Recommendation: Proceed to the owner's manual pass without another
  implementation fix. 4 observations, 0 confirmed problems, 0 hypotheses, 0 open questions." It named the gap and its
  closure independently: the check list supplied the map the document comparison read, so an omitted catalog pin was
  undiscoverable; the new comparison takes the catalog aliases as a separate input and rejects either difference before
  comparing documents. It classed the missing wiring test as a regression-testing limitation rather than a current
  defect, and set aside the two failure messages' ordering as unnecessary.
- **Its one falsifying condition, probed** — "IF a catalog-only Paketo addition can leave `verifyDocumentedPins` green,
  THEN fix that bypass first." Probed in the tree: `verifyDocumentedPins` green, one `paketo-coverage-probe = "1.0.0"`
  alias added to `gradle/libs.versions.toml`, the task failed with "The catalog pins Paketo coordinates the build does
  not check: paketo-coverage-probe", the line reverted (the file returns to a clean `git status`), and the task green
  again. The condition does not hold.
- **What it is not** — a recommendation is not approval: the manual pass is still the owner's, and shipping still needs
  the separate delivery word.

## Correction — the ledger's sixth field (2026-11-06)

- **What the field means** — the review-round record's sixth field is the number of findings that missed an existing
  rule — `AGENTS.md`, a subject pack, the specs, or the reviewer's definition — as `showcase/quality/commit-hygiene`'s
  record-format requirement names it and `.opencode/agent/review-quick.md` defines it.
- **What the rows carry** — every row of `rounds.tsv` carries, in that field, the number of finding instances its
  `findings/round-<N>.md` holds, which is consistent with the field having been written from the finding count, and
  `unit-layout-allowlist`'s process defect `#2` records that method error, found when a later unit compared its own row
  against the reviewer's line.
- **What the true values are** — unestablished. The reviewer's per-round compliance figure was session-only — the stored
  instances carry each finding's class, not the judgment that it missed a rule — so equality with the finding count does
  not determine the right figure: every finding might have qualified as a compliance miss, and a clean round's zero is
  correct either way. A summary derived from the field — `scripts/review-rounds.py` sums it — inherits that, so such a
  digest is not a verified compliance figure.
- **What this note does not do** — it does not rewrite the numbers. They stand as recorded; a later pass that judges
  each finding against the rules in force at its round's date is a dated retrospective reclassification, not recovery of
  the original figures.
