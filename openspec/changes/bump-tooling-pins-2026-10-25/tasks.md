# Tasks

Workspace: unit=bump-tooling-pins-2026-10-25;
path=/Users/afanasyev/Development/axon-showcase-bump-tooling-pins-2026-10-25; branch=bump-tooling-pins-2026-10-25;
writer=ses_e8d476658ffep4lI2e5U7jH5gj.

## 1. The pins

- [x] 1.1 Re-resolve each target against its own lookup (`npm view @fission-ai/openspec version`,
      `gh api repos/snyk/cli/releases/latest --jq .tag_name`) rather than reading tracker issue #308 alone. **Done**:
      `1.15.0` and `v1.1308.3`, so neither target moved since the issue's report.
- [x] 1.2 Set the OpenSpec pin in **every** workflow the `toolingUpdates` check declares for it — derive the inventory
      from the check's own `ToolingUpdateCheck` entries, not a hand-written list. **Done**: the check declares three
      entries, and `ci.yml`, `opencode.yml` and `audit.yml` now carry `1.15.0`.
- [x] 1.3 Set `snyk-version` in `.github/workflows/dependency-security.yml`. The Snyk action's floating ref stays.
      **Done**: the file carries `v1.1308.3`, and `snyk/actions/setup@master` is untouched.
- [x] 1.4 Regenerate the instruction files with the pinned CLI from the repository root, then inspect the diff rather
      than the file list: classify each changed file as a `generatedBy` stamp or a template content change, and read
      what a changed template now instructs. **Done**: `openspec update` ran under `1.15.0`, equal to the pin; six
      `openspec-*/SKILL.md` files changed, each by its stamp line only, and no `.opencode/commands/**` body changed, so
      the generated inventory is unchanged. The run also rewrote `.opencode/package.json`'s `@opencode-ai/plugin` pin
      down to `1.19.3` (this worktree's OpenCode startup install), which is installer state rather than this unit's
      subject; it was reverted.
- [x] 1.5 Check the parked config-guard retirement entry's watch condition, which names the CLI bump as its check point
      (`docs/ideas.md`, 2026-09-30): does this release carry `Fission-AI/OpenSpec#1894` or `#1892`'s half? **Done**: the
      `1.15.0` distribution carries no `inspectProjectConfig` (positive control: the same tree holds
      `dist/core/project-config.js` with the warning text), and `#1894` and `#1892` are both open upstream with
      `v1.15.0` the latest release — so the `ci.yml` probe and the `/opsx-tool-update` re-verification stand and the
      entry stays live, unchanged.

## 2. Verification

- [x] 2.1 Re-run the check that produced the finding (`./gradlew toolingUpdates`) and confirm the bumped coordinates are
      gone. **Done**: the report reads "Tooling updates:\nNo tooling updates available."
- [x] 2.2 Run `./gradlew workflowLint` over the edited workflows. **Done**: green, and it also runs inside `check`.
- [x] 2.3 Run `./gradlew spotlessApply`/`spotlessCheck`, `openspec validate --all`, and
      `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false`; run the manual 120-character check over the change's
      whole changed set. **Done**: all green, after one transient failure of `:verifyInfraImageVersions` — its Helm
      repository fetch to `prometheus-community` failed to reach the network and succeeded on the task's own retry, and
      the task's inputs are untouched by this change. The manual check reports no over-long added line in the tracked
      diff; the claims table's rows exceed 120 in `design.md`, which is the formatter-aligned table form the formatting
      convention accepts rather than a hand-wrapped defect.
- [x] 2.4 Sweep the live document surfaces for the old pins (`1.14.3`, `v1.1308.2`); a dated record keeps its values.
      **Done**: `git grep -n -e "1\.14\.3" -e "v1\.1308\.2" -- . ':!openspec/changes/archive'` returns no match.
- [x] 2.5 Re-exercise the config-read-path probe `ci.yml` runs against the pinned CLI: a healthy `openspec/config.yaml`
      reads its declared `rules` and `operations.*.guidance` with no warning, and each warning pattern still fires on a
      malformed one (the guard keys off the CLI's output text, so a minor release that reworded it would disable the
      guard while the pull request stayed green). Restore a zero-line diff afterwards. **Done**: against `1.15.0` a
      healthy config emits no warning, `openspec instructions proposal` returns 9 `rules`, and
      `openspec instructions apply` returns 6 `operationGuidance` entries; an unquoted `rules` item containing `: ` and
      the same in `operations.apply.guidance` each make `openspec new change` print "must be an array of strings", a
      dropped closing quote prints "could not parse". The config was restored to a zero-line diff and the probe changes
      removed.

## 3. Review, capture and the manual pass

- [x] 3.1 Record the pre-write derivation pass and its claims table in `design.md`, seeded by `scripts/claim-scan.py`,
      with every candidate resolved into a row or a class-level dismissal. **Done**: the table and the per-kind
      dismissals are in `design.md`, seeded with `--base c149149d…` and re-derived after this unit's last append.
- [x] 3.2 Run the `review-quick` loop over the proposal and the implementation, appending each round's row to
      `docs/review-rounds.tsv` as its report arrives and running the ledger checkpoint after each append; repeat until a
      round reports nothing new. **Done**: one reviewer session (`ses_e8d3808f…`, `created` then `continued`); each
      round's row and classes are the ledger's, both checks ran clean after each append, and the loop ran to a round
      with no new observation after every fix pass, the advisor's narrowing included.
- [x] 3.3 Derive the deep-review routing with `scripts/review-routing.py`, record its outcome and signals here, and run
      the routed pass when it is `REQUIRED` (or a `RECOMMENDED` outcome the owner resolves to run). **Done**: `REQUIRED`
      — `[infrastructure-or-deployment]` on each of the four edited workflows. Deep pass 1 (initial review) returned no
      findings, and the capture's applied delta post-dated it, so a second pass verified that delta: it returned one
      nitpick — this note recording the review as closed at pass 1 — and no blocking or should-fix finding. Findings:
      the nitpick, `FIXED_AND_VERIFIED`. The advisor's narrowing (3.6) removes the content it flagged rather than adding
      unreviewed material, so a third full pass is not triggered: its diff is a revert of `AGENTS.md` plus a short
      pointer, and the quick round over it is the review.
- [x] 3.4 Run the `lesson-capture` subagent and record its applied net delta or its skip reason. **Done**: the capture
      proposed a general `AGENTS.md` step requiring every bump to check the parked records it activates, a sharpened
      step 3, and a pointer in `.opencode/commands/opsx-tool-update.md`. It was applied, then narrowed on the advisor's
      review (3.6): `AGENTS.md` is back at `HEAD` (net 0), and only the command's step 7 ships, pointing at the standing
      2026-09-30 `docs/ideas.md` condition. Applied net delta: `git diff --numstat AGENTS.md` has no entry, and the
      command gains 4 lines. No retirement: the config-guard entry stays live, which 1.5 records.
- [ ] 3.5 Ask the owner for the manual review pass; commit and push only after approval.
- [x] 3.6 Consult the `technical-advisor` on the shipping scope and apply its recommendation. **Done**: its report
      recommended keeping one PR but narrowing the capture: it measured the promotion threshold against both gate
      branches and found it unestablished on the available materials (one record silent while the one before it ran the
      check), and this unit reads the incident as not severe because the periodic audit bounds the delay. The owner
      directed following it; 3.4 applies that (the `AGENTS.md` addition reverted, the addressed pointer kept). The
      report is a recommendation, not a gate or an approval.
- [x] 3.7 Keep the branch refreshed from `origin/main` before the manual pass. **Done**: it advanced twice while the
      unit was in review, to `528fdcce` (the buildpack bump) and then to `c149149d` (the kps chart and
      closure-disposition bumps). Each refresh was a `git merge --ff-only origin/main` with the work uncommitted, after
      the only overlapping path each time — `docs/review-rounds.tsv` — was restored to `HEAD` and this unit's rows
      re-applied on the merged file (`git diff --numstat` `11 0` after the second). `git diff --stat origin/main` lists
      only this unit's paths, so no advance deleted anything the unit changed. The refreshed `AGENTS.md` made the
      workspace record's `writer` field the writing session's identifier, which the record now carries, and the claims
      table's base anchors follow the latest base.

## 4. The change's own records

- [x] 4.1 After the push and before the archive commit, dispatch `dependency-security.yml` from the branch and read the
      result: it must install the pinned Snyk CLI and run the scan. A red run attributed to the pin means the tag does
      not install; one attributed to a finding means the policy, not the pin. **Done**: run `40196207458` on `5c244526`,
      success — the setup step logged `Installed Snyk v1.1308.3`, and `dependencySecurityCheck` ran.
- [x] 4.2 Record what the dispatched Snyk scan observed, so that pin's verification state is readable from the merge
      rather than inferred from a green `build`. **Done**: the scan reported "Tested 8 dependencies for known issues, no
      vulnerable paths found.", so the pin installs and the policy clears on it.
- [x] 4.3 Park the `pack` pin's verification follow-up durably if a `pack` row appears in the report; this unit's report
      names only the OpenSpec and Snyk pins. **Done**: no `pack` row appeared, so this unit parks nothing; the
      2026-10-22 entry at `docs/ideas.md` ("Confirm the `pack 0.40.10` pin on its first scheduled image build") stands.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The Helm (#40), buildpack (#211), and dependency (#13) findings are separate units, as before.
