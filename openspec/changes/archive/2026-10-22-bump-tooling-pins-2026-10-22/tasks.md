# Tasks

## 1. The pins

- [x] 1.1 Re-resolve each target against its own lookup (`npm view @fission-ai/openspec version`,
      `gh api repos/snyk/cli/releases/latest --jq .tag_name`,
      `gh api repos/buildpacks/pack/releases/latest --jq .tag_name`) rather than reading tracker issue #308, whose
      report predates two of the three releases. **Done**: all three still resolve to the versions the check named.
- [x] 1.2 Set the OpenSpec pin in **every** workflow that declares it: `.github/workflows/ci.yml`, `opencode.yml`,
      `audit.yml`. **Done**: the three files carry the new version.
- [x] 1.3 Set `snyk-version` in `.github/workflows/dependency-security.yml` and `pack-version` in `e2e.yml`,
      `deployment-smoke.yml` and `release.yml`. The Snyk action's floating ref stays. **Done**: the four values carry
      the new versions.
- [x] 1.4 Regenerate the instruction files with the pinned CLI (`npx @fission-ai/openspec@1.14.3 update`) and read the
      result. **Done**: eight generated files changed, all modifications, and the per-file split is the one
      `proposal.md`'s What Changes states — no file was added or removed, so the generated inventory is unchanged.

## 2. Verification

- [x] 2.1 Re-run the check that produced the finding. **Done**: `./gradlew toolingUpdates` reports "No tooling updates
      available.", so the bumped coordinates are cleared.
- [x] 2.2 Run `./gradlew workflowLint` over the seven edited workflows. **Done**: green.
- [x] 2.3 Run `./gradlew spotlessApply`/`spotlessCheck`, `openspec validate --all`, and
      `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false`; run the manual 120-character check over the change
      dir. **Done**: all green; the change validates (`--strict`) and the manual check is clean.
- [x] 2.4 Sweep the document surfaces a version bump makes stale for the three old pins, fixing every live site; a dated
      record keeps its values. **Done**:
      `git grep -n -e 1.14.2 -e v1.1308.1 -e 0.40.9 -- . ':!openspec/changes/archive'` returns exactly the two
      `docs/audits/2026-10-19.md` lines.
- [x] 2.5 Re-exercise the config-read-path probe `ci.yml` runs against the pinned CLI: a healthy `openspec/config.yaml`
      reads its declared `rules` and `operations.*.guidance` with no warning, and each warning pattern still fires on a
      malformed one (the guard keys off the CLI's output text, so a patch release that reworded it would disable the
      guard while the pull request stayed green). Restore a zero-line diff afterwards. **Done**: against the pinned CLI
      a healthy config reads its rules and guidance with no warning, and each pattern fires — an unquoted item in
      `rules`, the same in `operations.apply.guidance`, and a dropped closing quote. The config was restored to a
      zero-line diff.

## 3. Review, capture and the manual pass

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`, seeded by
      `scripts/claim-scan.py`, with every candidate resolved into a row or a class-level dismissal. **Done**: the table
      and the per-kind dismissals are in `design.md`.
- [x] 3.2 Run a `review-quick` pass over the proposal and the implementation, appending each round's row to
      `docs/review-rounds.tsv` and running the ledger checkpoint after each append; repeat until a round reports nothing
      new. **Done**: each row was appended as its report arrived, with the checkpoint run after each append; the ledger
      carries the rounds and their outcomes, and the loop is clean.
- [x] 3.3 Derive the deep-review routing outcome, decide at or above the floor, and record the outcome and its signals
      here. **Done**: `REQUIRED` (`infrastructure-or-deployment` — every edited workflow). The deep pass ran and
      returned one should-fix — the `pack` pin's verification state had no durable record and task 4.1 could not be
      completed for it — fixed by parking the follow-up in `docs/ideas.md` and re-scoping 4.1 to the Snyk dispatch; and
      one nitpick, task 3.2 unchecked after a clean loop, fixed.
- [x] 3.4 Run the `lesson-capture` subagent and record its applied net delta or its skip reason with the reason.
      **Done**: no `AGENTS.md` proposal (net 0 lines) and no retirement; one authoring-system recommendation was applied
      to the mechanism that produced the defect — `.opencode/commands/opsx-tool-update.md` now inspects the
      regeneration's diff rather than its file list, classifying each file as a stamp or a template change, because a
      body change is invisible to `git status`. The four notes the unit raised were judged already covered — the
      claims-table dismissal rule and the rewrite-once branch, the command-verification rule, the durable-count rule,
      and the inline-span rule — and are recorded as instruction-following failures.
- [x] 3.5 Ask the owner for the manual review pass; commit and push only after approval. **Done**: the owner approved on
      2026-10-22 after the manual review request; the change ships on PR #659.
- [x] 3.6 After the push and before the archive commit, dispatch `dependency-security.yml` from the branch and read the
      result: it must install the pinned Snyk CLI and run the scan. A red run attributed to the pin means the tag does
      not install; one attributed to a finding means the policy, not the pin. **Done**: run `39747682382`, success —
      `snyk/actions/setup` installed `snyk-version: v1.1308.2`, `dependencySecurityCheck` ran, and the scan reported
      "Tested 8 dependencies for known issues, no vulnerable paths found." So the tag installs and the scan clears.
- [x] 3.7 Park the `pack` pin's verification follow-up durably: the next scheduled image build installs it. No dispatch
      — a whole-cluster smoke to prove one CLI tag installs is not a proportionate pre-merge cost, and `workflowLint`
      plus the release's existence are the pre-merge evidence. **Done**: parked in `docs/ideas.md` (2026-10-22) with its
      revisit condition, so the follow-up survives the merge.

## 4. The change's own records

- [x] 4.1 Record what the dispatched Snyk scan observed, so that pin's verification state is readable from the merge
      rather than inferred from a green `build`; the `pack` pin's is the parked follow-up's to record. **Done**: the
      observation is on 3.6 (run `39747682382`: the pin installed, the scan found no vulnerable paths), so the archived
      `tasks.md` carries it.
