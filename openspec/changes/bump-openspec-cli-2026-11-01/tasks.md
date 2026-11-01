# Tasks

Workspace: unit=bump-openspec-cli-2026-11-01;
path=/Users/afanasyev/Development/axon-showcase-bump-openspec-cli-2026-11-01; branch=bump-openspec-cli-2026-11-01;
writer=ses_e6a51d9beffeto8qnIbWdlzo8j.

## 0. Workspace

- [x] 0.1 Prepare the created worktree for IntelliJ, since the primary checkout carries `.idea/`: inherit its
      `.python-version` (the tree had none) and run `./scripts/setup-idea.sh` there. Run after the owner flagged the
      omission, which `docs/process-defects/bump-openspec-cli-2026-11-01.md#1` records. **Done**: the script reported
      `Settings: CONFIGURED` and `Python: CONFIGURED` (interpreter `3.12.16`, SDK registered), and skipped the plugin
      install because IntelliJ was running — that install needs the whole IDE closed and a re-run.

## 1. The pin

- [x] 1.1 Re-resolve the target against its own lookup (`npm view @fission-ai/openspec version`) rather than reading
      tracker issue #308 alone. **Done**: `1.16.0`, so the target has not moved since the issue's report.
- [x] 1.2 Set the pin in **every** workflow the `toolingUpdates` check declares for it — derive the inventory from the
      check's own `ToolingUpdateCheck` entries, not a hand-written list. **Done**: the check declares three entries, and
      `ci.yml`, `opencode.yml` and `audit.yml` now carry `1.16.0`.
- [x] 1.3 Regenerate the instruction files with the pinned CLI from the repository root, then inspect the diff rather
      than the file list: classify each changed file as a `generatedBy` stamp or a template content change, and read
      what a changed template now instructs. **Done**: `openspec update` ran under `1.16.0`, equal to the pin; six
      `openspec-*/SKILL.md` files changed, each by its stamp line only, and no `.opencode/commands/**` body changed, so
      the generated inventory is unchanged. The run did not rewrite `.opencode/package.json`.

## 2. The config guard this pin retires

- [x] 2.1 Check the parked record this bump activates, and verify its condition against the pinned CLI rather than the
      record's text: `docs/ideas.md`'s 2026-09-30 entry names a pin reaching `v1.16.0` as the point its first clause is
      met. **Done**: with `1.16.0` installed, a config carrying a dropped closing quote makes `openspec validate --all`
      exit 1 (`Error: Could not parse … Missing closing 'quote`), so the CI probe's `could not parse` arm cannot be the
      mechanism that catches that shape.
- [x] 2.2 Drop the probe's `could not parse` arm in `.github/workflows/ci.yml`, keep the list-shape arm, and state in
      the step why: `validate` fails the whole-file case above the probe, and the list-shape warning is the shape it
      does not fail on. **Done**: the grep reads `must be an array of strings` alone, with a comment naming the split,
      and the error message no longer claims a malformed scalar reaches it.
- [x] 2.3 Follow the retirement through every artifact that describes the guard: `AGENTS.md`'s config-guard bullets, the
      `/opsx-tool-update` re-verification's steps, the `check-and-test-authoring` incident sentence, and the
      `docs/ideas.md` entry. **Done**: `AGENTS.md` records the retired arm and that `validate` is the mechanism left,
      the command's whole-file control now asserts `validate` exits non-zero, the skill's sentence records the arm's
      later retirement, and the ideas entry marks its first clause met while the entry stays live for
      `Fission-AI/OpenSpec#1894`.

## 3. Verification

- [x] 3.1 Re-run the check that produced the finding (`./gradlew toolingUpdates`) and confirm the bumped coordinates are
      gone. **Done**: the report reads "No tooling updates available.".
- [x] 3.2 Run `./gradlew workflowLint` over the edited workflows. **Done**: BUILD SUCCESSFUL, and it also runs inside
      `check`.
- [x] 3.3 Re-exercise the config-read-path probe against the pinned CLI with one positive control at a time — the guard
      keys off the CLI's output text, so a release that reworded it would disable the guard while the pull request
      stayed green. Restore a zero-line diff afterwards. **Done**: a healthy config passes `validate` (exit 0) with no
      grep hit; an unquoted `rules` item carrying `: ` and an unquoted `operations.apply.guidance` item carrying `: `
      each keep `validate` at exit 0 and make `openspec new change` print "must be an array of strings"; a dropped
      closing quote makes `validate` exit 1. `openspec/config.yaml` was restored to a zero-line diff and no probe change
      was left behind.
- [x] 3.4 Run `./gradlew spotlessApply`/`spotlessCheck`, `openspec validate --all`, and
      `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false`; run the manual 120-character check over the change's
      whole changed set. **Done**: `spotlessCheck` and the root `check` are green after two corrections — an unescaped
      `|` in the claims table's issue-body quote, which `verifyTableRows` rejected, and a `spotlessApply` re-run after
      the last tick, which the quick review caught; the manual check reports the claims table's formatter-aligned rows
      and `AGENTS.md`'s pre-existing subject-pack table as the only over-long lines, which the `Formatting` convention
      accepts for tables the formatter cannot wrap.
- [x] 3.5 Sweep the live document surfaces for the old pin and stamp (`1.15.0`), keeping a dated record's own values.
      **Done**: `git grep -n -e "openspec@1\.15\.0" -e 'generatedBy: "1\.15\.0"' -- . ':!openspec/changes/archive'`
      returns no match.

## 4. Review, capture and the manual pass

- [x] 4.1 Record the pre-write derivation pass and its claims table in `design.md`, seeded by `scripts/claim-scan.py`,
      with every candidate resolved into a row or a class-level dismissal. **Done**: `scripts/claim-scan.py` run at
      `521a93ee`, the base this unit began on, seeded the table, whose candidates resolve into rows or the per-kind
      dismissals beside it, and the table was re-derived after the last append to the artifacts. A run at the branch's
      current base would also scan the merge since, so the seed base is stated rather than the current one.
- [x] 4.2 Run the `review-quick` loop over the proposal and the implementation, appending each round's row to
      `docs/review-rounds.tsv` as its report arrives and running the ledger checkpoint after each append; repeat until a
      round reports nothing new. **Done**: the loop ran on one reviewer session (`ses_e6a48d35affev4IMmwHBJOw7UG`,
      `created` then `continued`); each row was appended when its report arrived, both ledger checks ran after each
      append, and each row's `compliance_misses` is derived from its own report as the findings that were a miss of an
      existing rule, never copied from a report's suggested row.
- [x] 4.3 Derive the deep-review routing with `scripts/review-routing.py`, record its outcome and signals here, and run
      the routed pass when it is `REQUIRED` (or a `RECOMMENDED` outcome the owner resolves to run). **Routing**:
      `REQUIRED` — `[infrastructure-or-deployment]` on each of the three edited workflows
      (`.github/workflows/{audit,ci,opencode}.yml`). Both passes and their finding statuses are recorded in 4.3.1.
- [x] 4.3.1 Record each deep-review pass and every finding's status. **Pass 1** (initial review, session
      `ses_e6a3cbdddffeydiUB7x5zCJBDx`): one nitpick — 4.3 forward-referenced a sub-task the file did not carry, leaving
      the pass's findings no named home. **Status**: `FIXED_AND_VERIFIED` — materialized as this record, and pass 2
      re-read the file and the change's central claims independently and returned no finding.
- [x] 4.4 Run the `lesson-capture` subagent and record its applied net delta or its skip reason. **Done**: applied net
      delta 0. Running it at all was an obsolete step this task inherited from the archived analogue: `AGENTS.md`'s
      generalization workflow consumes merged records in the unit the owner opens for it, and
      `2026-10-27-separate-lesson-capture-from-implementation` replaced the inline capture. Its one durable proposal —
      reword the workspace bullet's deadline from "before it is opened" to "before its first write", plus the
      `setup-idea` skill's trigger line — and its `scripts/new-worktree.sh` recommendation are parked in `docs/ideas.md`
      (2026-11-01) with the two sibling records that entry cites: one showing the same omission, one the
      `.python-version` inheritance order.
- [x] 4.5 Write `docs/process-defects/bump-openspec-cli-2026-11-01.md`, stating "no process defect observed" when that
      is what the unit observed. **Done**: the record carries the omitted worktree preparation (`#1`, appended when the
      owner raised it) and the `lesson-capture` run inside this unfinished unit (`#2`, appended on the advisor's
      finding); re-read before the manual pass, so a later observation is added rather than missed.
- [x] 4.6 Ask the owner for the manual review pass; commit and push only after approval. **Done**: the owner approved on
      2026-11-01 after the advisor's third consultation agreed with the scope.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The Helm (#40), buildpack (#211), dependency (#13) and upstream-reference (#343) findings are separate units, as
  before.
