# Tasks

The unit's record is `docs/units/bump-tooling-pins-2026-11-09/unit.md`; its workspace fields live there, not here.

No `design.md` is written: this change makes no architectural decision, adds no dependency, and carries no migration
complexity, so the OpenSpec design artifact is conditional and not owed. The pre-write derivation pass and its claims
table therefore live here, as the review gate's §1 allows for a unit that writes no design.

## 1. The pins

- [x] 1.1 Re-resolve each target against its own lookup (`npm view @fission-ai/openspec version`,
      `gh api repos/snyk/cli/releases/latest --jq .tag_name`) rather than reading issue #308 alone, and record both
      results in the report. **Verification**: the two commands' output names the version each pin moves to. **Done**:
      `1.16.1` and `v1.1308.4`, unchanged since the proposal's lookup.
- [x] 1.2 Set the OpenSpec pin in **every** workflow the `toolingUpdates` check declares for it, deriving the inventory
      from the check's own `ToolingUpdateCheck` entries in `build.gradle.kts` rather than from a hand-written list.
      **Verification**: `git grep -n "@fission-ai/openspec@"` shows one hit per declared file, all at `1.16.1`.
      **Done**: the check declares three entries, and `ci.yml`, `opencode.yml` and `audit.yml` each carry `1.16.1`.
- [x] 1.3 Set `snyk-version` in `.github/workflows/dependency-security.yml` and leave `snyk/actions/setup@master`
      untouched. **Verification**: `git grep -n "snyk-version:"` shows `v1.1308.4`, and the `uses:` line is unchanged.
      **Done**: the file carries `v1.1308.4`, and `snyk/actions/setup@master` is untouched.
- [x] 1.4 Regenerate the instruction files with the pinned CLI from the repository root (`openspec --version` first),
      then inspect the diff rather than the file list: classify each changed file as a `generatedBy` stamp or a template
      content change, and read what a changed template now instructs. IF the command or skill inventory changed rather
      than only the bodies, THEN check that `AGENTS.md` and `README.md` still describe the commands accurately and that
      the root `build.gradle.kts` markdown target's exclusions still match what `openspec update` wrote.
      **Verification**: every changed `.opencode/` file is classified in the report, an inventory change's documentation
      and exclusion sites are checked, and a content change is either explained or investigated before the gate runs.
      **Done**: `openspec update` ran under `1.16.1`, equal to the pin; the six `openspec-*/SKILL.md` files changed by
      their `generatedBy` line only, no `.opencode/commands/**` body changed, and `.opencode/package.json` kept its
      `@opencode-ai/plugin` pin, so the generated inventory is unchanged and no documentation or exclusion site moves.
- [x] 1.5 Check the parked config-guard retirement entry's watch condition, which names the CLI bump as its check point
      (`docs/ideas.md`, the 2026-09-30 section): does this release carry `Fission-AI/OpenSpec#1894`? **Verification**:
      the tracker's own state is read and recorded, and any met clause's removal is applied or its absence explained.
      **Done**: `Fission-AI/OpenSpec#1894` is `OPEN` with `mergedAt: null`, so no clause is met, the `ci.yml` list-shape
      probe and the `/opsx-tool-update` controls stand, and the 2026-09-30 entry stays live.

## 2. Verification

- [x] 2.1 Re-run the check that produced the finding (`./gradlew toolingUpdates`) and confirm all four rows are gone.
      **Verification**: the report reads `No tooling updates available.` **Done**: the report reads exactly that line.
- [x] 2.2 Run `./gradlew workflowLint` over the edited workflows. **Verification**: green; it also runs inside `check`.
      **Done**: `BUILD SUCCESSFUL`.
- [x] 2.3 Run `./gradlew spotlessApply`/`spotlessCheck`, `openspec validate --all`, and
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, then the manual 120-character check over the change's
      whole changed set. **Verification**: every gate green and no over-long added line. **Done**: all green. The first
      `check` run failed at `:verifyWorkspaceRecords` because this file copied the record's workspace fields instead of
      referencing the record; after the fix the run passed. One `:verifyInfraImageVersions` run failed on the bitnami
      chart fetch (a socket error to the Docker registry) and its retry passed; the task's inputs are untouched here.
      The manual check reports only the record-form lines (`rounds.tsv`, `sessions.tsv`, the `Workspace:` line) and the
      formatter-owned claims table, which the formatting convention accepts.
- [x] 2.4 Sweep the live document surfaces for the old pins (`1.16.0`, `v1.1308.3`) with
      `git grep -n -e "1\.16\.0" -e "v1\.1308\.3" -- . ':!openspec/changes/archive'`, and classify each hit as a live
      claim to retarget or a historical statement to keep. **Verification**: each hit is accounted for in the report; a
      dated record keeps its values. **Done**: no workflow hit remains. The rest are historical citations (`AGENTS.md`,
      `docs/ideas.md`, `.opencode/skills/check-and-test-authoring/SKILL.md`), a dated audit report
      (`docs/audits/2026-11-07.md`), this change's own describing lines, and the unrelated `@exodus/bytes` lockfile
      coordinate.
- [x] 2.5 Re-exercise the config-read-path probe `ci.yml` runs against the pinned CLI: a healthy `openspec/config.yaml`
      reads its declared `rules` and `operations.*.guidance` with no warning; an unquoted `: ` in a `rules` item and the
      same in an `operations.*.guidance` item each make the probe report "must be an array of strings"; and a dropped
      closing quote from a quoted item makes `openspec validate --all` exit non-zero. **Verification**: every direction
      is observed against `1.16.1`, each control's source text is confirmed changed before reading its diagnostic, and
      `openspec/config.yaml` is restored to a zero-line diff afterwards. **Done** against `1.16.1`: a healthy config
      prints no warning, `openspec instructions proposal` returns 9 `rules`, and `openspec instructions apply` returns 6
      `operationGuidance` entries; an unquoted `: ` in a `rules` item prints "Rules for 'proposal' must be an array of
      strings", the same in an `operations.apply.guidance` item prints "Guidance for operation 'apply' must be an array
      of strings", and a dropped closing quote makes `openspec validate --all` exit 1 with "Could not parse". The config
      is back to a zero-line diff.

## 3. Review, capture and the manual pass

- [x] 3.1 Record the pre-write derivation pass and its claims table in this file, seeded by `scripts/claim-scan.py`,
      with every candidate resolved into a row or a class-level dismissal, and re-derive it after the last edit to this
      file. **Verification**: the table below and the dismissals beside it cover every candidate the final scan prints.
      **Done**: the table is re-derived after the implementation's last edit and after each base fast-forward — the
      three rows about the pre-change state run against `0deca079`, and the generated-file row records what the skills
      carried there. The final scan prints four kinds, each with a class-level dismissal.
- [ ] 3.2 Run the `review-quick` loop over the proposal and the implementation, writing each round's finding instances
      to `docs/units/<unit>/findings/round-<N>.md` as its report arrives, appending the round's six-field row to
      `docs/units/<unit>/rounds.tsv` and each quick invocation's row to `docs/units/<unit>/sessions.tsv`, then running
      the ledger checks after each append; repeat until a round reports nothing new. **Verification**: the instance
      file's classes equal the row's raised classes, `--unique-rounds --expect-clean` passes at the boundary, and
      `--finding-instances` and `--session-continuity` are clean.
- [x] 3.3 Derive the deep-review routing with `scripts/review-routing.py`, record its outcome and signals in
      `docs/units/<unit>/deep-review.md`, and run the routed pass when it is `REQUIRED`. **Verification**: the record
      carries an outcome, and a `RECOMMENDED` one has the owner's decision recorded. **Done**: the routing is `REQUIRED`
      (`[infrastructure-or-deployment]` on each of the four edited workflows); pass 1 ran as a full initial pass and
      returned `nothing remains`, recorded in `deep-review.md`.
- [x] 3.4 Write `docs/units/<unit>/process-defects.md` reflecting what the unit observed, or stating that it observed
      none. **Verification**: the file exists and covers proposal and implementation. **Done**: four entries cover the
      proposal (obsolete inherited task, missing workflow dispatch, scaffold README) and the implementation (the copied
      workspace fields).
- [ ] 3.5 Ask the owner for the manual review pass after the readiness check, and commit, push, open the pull request,
      archive, and merge only after that approval and a separate delivery request. **Verification**: the owner's
      approval is recorded before any of those commands runs.

The `lesson-capture` subagent is **not** run inside this unit: `AGENTS.md`'s generalization workflow consumes merged
process-defect records in a unit the owner opens for it, so the inline capture a bump used to inherit is retired
(`2026-10-27-separate-lesson-capture-from-implementation`, and the archived `2026-11-01-bump-openspec-cli-2026-11-01`
recorded the inherited step as obsolete).

## 4. Delivery

- [ ] 4.1 Dispatch `dependency-security.yml` against the pushed branch and confirm the run is green — the bumped Snyk
      pin's first real execution — then keep the run's conclusion in the change's report. **Verification**: the
      dispatched run's conclusion is `success` (or its failure is analysed and reported), and the task is ticked before
      or in the archive commit.
- [ ] 4.2 Dispatch `audit.yml` against the pushed branch and confirm the run is green, since this change edits that
      workflow's pin and it declares `workflow_dispatch`. **Verification**: the dispatched run completes, its install
      step uses `1.16.1`, and the run's conclusion is recorded; the report it opens is left for the owner rather than
      merged.

## Claims table

Rows about the state before this change are anchored to the unit's base revision, `0deca079`; the two fast-forwards the
readiness check required (`bfe30a08` → `2b4c9a00` → `0deca079`) left the OpenSpec, Snyk and workflow tool pins
unchanged, and the rows' classes read the same there.

| Claim                                                                                 | Source or command                                                                                         | Result                                                                                                                                                                              |
| ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Issue #308 names these four rows                                                      | `gh issue view 308 --repo aanbrn/axon-showcase`                                                           | the body lists the three OpenSpec rows and the Snyk row                                                                                                                             |
| The OpenSpec CLI's latest release is `1.16.1`                                         | `npm view @fission-ai/openspec version`                                                                   | `1.16.1`                                                                                                                                                                            |
| The Snyk CLI's latest release is `v1.1308.4`                                          | `gh api repos/snyk/cli/releases/latest --jq .tag_name`                                                    | `v1.1308.4`                                                                                                                                                                         |
| The four reported rows were the only ones at the base                                 | `git grep -n -e "@fission-ai/openspec@1.16.0" -e "snyk-version: v1.1308.3" 0deca079 -- .github/workflows` | exactly four hits; `./gradlew toolingUpdates` reported the same four before the bumps                                                                                               |
| The OpenSpec pin sits in three declared workflows                                     | `toolingUpdates` check's own `ToolingUpdateCheck` entries, `build.gradle.kts`                             | three `openspec-cli (<file>)` entries                                                                                                                                               |
| No capability spec states either pin                                                  | `git grep -n -e "@fission-ai/openspec" -e "snyk-version" -- openspec/specs`                               | no match, so `skip_specs: true` is the correct marker                                                                                                                               |
| The generated skills carried `generatedBy: "1.16.0"` at the base                      | `git grep -n "generatedBy:" 0deca079 -- .opencode/skills`                                                 | six matches at `1.16.0`; the `opsx-*` commands carry no stamp                                                                                                                       |
| `Fission-AI/OpenSpec#1894` is still open and unmerged                                 | `gh pr view 1894 --repo Fission-AI/OpenSpec --json state,mergedAt`                                        | `"state":"OPEN"`, `"mergedAt":null`                                                                                                                                                 |
| At the base the old pin strings appear as live pins, stamps, or historical statements | `git grep -n -e "1\.16\.0" -e "v1\.1308\.3" 0deca079 -- . ':!openspec/changes/archive'`                   | by class: four live pins, six generation stamps, historical citations (`AGENTS.md`, `docs/ideas.md`, one skill), a dated audit record's lines, and an unrelated lockfile coordinate |
| The audit prompt invokes no `openspec` command                                        | `grep -n openspec .github/workflows/audit.yml`                                                            | the install step only, at lines 32-33; the prompt invokes none                                                                                                                      |

### Dismissals

- `[path]` candidates: each is a repository path or workflow coordinate the change names; every one is read in this
  unit, and the paths the change edits are the four workflow files and the six regenerated skill files.
- `[cross-ref]` candidates: issue `#308` and the `Fission-AI/OpenSpec` references are resolved through the tracker in
  the table above; `openspec/specs` and `showcase/quality/merge-governance` are read to establish the `skip_specs`
  position.
- `[absolute]` candidates: the absolutes here are ordinary words inside instructions, a record's own "none" field, or a
  claim stated beside its source — a claims-table row or the paragraph recording the deliberate `design.md` decision —
  so each is checked where it stands.
- `[count]` candidates: the scan flags task and step identifiers and the unit's own record figures (a round number, a
  field count); they are bookkeeping rather than claims about the repository, and the claims that do count instances are
  rows above.
