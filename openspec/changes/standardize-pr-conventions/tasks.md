# Tasks

Workspace: unit=standardize-pr-conventions; path=/Users/afanasyev/Development/axon-showcase-standardize-pr-conventions;
branch=standardize-pr-conventions; writer=ses_e8f05c061ffeVdZniQbS3yuTHk.

## 1. Implementation

- [x] 1.1 `.github/PULL_REQUEST_TEMPLATE.md` — replace the header comment with the standard: the title rule (one
      imperative English phrase naming the change, no prefix, the change name and issue link in `## Summary`), the two
      required sections in order, further sections allowed, and the statement that a `--body-file` body must carry both.
      Keep the five checklist lines unchanged. **Done**: the header states all four; it governs every pull request,
      noting that the cloud response carries the sections and that a report pull request's body is the report, which
      `merge-governance` owns. The consultant's review of the implementation corrected the first draft's scope, which
      exempted the whole cloud body; the checklist is untouched.
- [x] 1.2 `.opencode/skills/ship-change/SKILL.md` — "Ship a change" step 1 points at that standard, since a body written
      to a file bypasses the template.
- [x] 1.3 `.opencode/skills/ship-change/SKILL.md` — the cloud-agent pull-request section gains a decision point: check
      an action-derived title against the standard and rename a deviating one with `gh pr edit <pr> --title` before
      merging.
- [x] 1.4 `.opencode/skills/ship-change/SKILL.md` — "How the action derives the pull request" points at that check
      rather than leaving the generated title unaddressed.
- [x] 1.5 `AGENTS.md` — the `--body-file` gotcha's closing clause carries the title rule through `--title`, beside the
      sections it already sends into the body file. **Done**: one clause added, and `spotlessApply` reflowed the
      `captured:` marker onto the following line — placement verified, not assumed.
- [x] 1.6 `docs/ideas.md` — park the deferred title check under `## 2026-10-25` with its revisit condition (titles
      drifting from the standard) and its trailing status tag.

## 2. Verification

- [x] 2.1 `./gradlew spotlessApply`, then `spotlessCheck`, after the last edit to a formatter-owned file. **Done**: both
      exit 0 after the final edit.
- [x] 2.2 `openspec validate --all` passes with the new change dir. **Done**: 27 passed, 0 failed, and the change
      reports `skip_specs` as an accepted zero-delta declaration.
- [x] 2.3 The edited skill definition still loads. **Done**: `opencode debug config` exits 0, so the project
      configuration including this skill's directory loads; the definition-inventory assertion (`opencode debug skill`)
      exists only on the V1 binary the CI `build` gate runs, and this edit touches no frontmatter.
- [x] 2.4 The 120-character manual check over the whole changed set, derived with
      `git status --porcelain --untracked-files=all` and `perl -CSD -lne 'print if length > 120'`. **Done**: the only
      over-long lines are markdown table rows in `design.md` and `AGENTS.md`'s pre-existing subject-pack table, which
      cannot wrap.

## 3. Documentation and capture

- [x] 3.1 Sweep the live docs that describe pull-request authoring for a claim this change falsifies: `AGENTS.md`'s
      docs-refresh and PR-body bullets, `README.md`'s workflow narrative, `docs/ideas.md`'s two adjacent parked entries,
      and the `merge-governance` / `agent-skills` requirements. **Done**: `AGENTS.md`'s gotcha updated; the README,
      `openspec/config.yaml`, and the `merge-governance` requirement state no title or section rule, so nothing there is
      falsified; the 2026-09-21 audit-title idea gained the clause that the rename now corrects the merged title, while
      its own dispatch check still verifies the prompt's effect.
- [x] 3.2 Confirm no spec delta is owed (`skip_specs: true`): grep `openspec/specs/` for the pull-request title and body
      behaviour. **Done**: `git grep -n -i -E "test plan|## Summary|pull request body" -- openspec/specs docs/adr`
      returns one hit — `merge-governance`'s owner-mention scenario, which this change leaves intact. The broader
      `git grep -n -i "title" -- openspec/specs docs/adr` returns 44 hits, classified by file: 35 the showcase's `title`
      field, 4 the `specs-auditor`'s spec-title matching, 2 `docs/adr/README.md` (filename convention and ADR title
      template), and 3 `merge-governance` (lines 441–442, 457). No requirement states a title or body format.
- [x] 3.3 Run the `lesson-capture` subagent over the diff and the review rows, and apply its durable proposals (or
      record the skip and its reason). **Done**: one proposal applied — the pre-write pass's universal-claim clause now
      names the absence form (`no`/`none`/`nothing`) and a subject with two halves, since this change's claims row C10
      was re-patched on those two gaps; the three mirrored `openspec/config.yaml` rules follow, and the rule's
      `captured:` chain gained this unit. Net delta: `.opencode/skills/review-gate/SKILL.md` +9/−6,
      `openspec/config.yaml` +9/−6, `AGENTS.md` unchanged by the capture (its +2/−1 is the `--body-file` clause).
      Rejected as already covered: the output-description and patch-per-finding lessons; no retirement candidate.

## 4. Review

- [x] 4.1 Pre-write derivation pass and claims table: `python3 scripts/claim-scan.py` seeded the candidates, each
      resolved into `design.md`'s claims table or a dismissal beside it.
- [x] 4.2 Quick-review loop: fix every round's findings and re-run until a round reports no new observation; each
      round's classes and state are `docs/review-rounds.tsv`'s appended rows, not restated here. **Done**: round 6 and
      round 9 each reported no new observation. The loop ran nine rounds on one reviewer session, in two arcs — rounds
      1–6 on the implementation, then rounds 7–9 after the consultation's review corrected the cloud branch. Both ledger
      checks pass after each append.
- [x] 4.3 Deep-review routing: derive the floor with
      `python3 scripts/review-routing.py --change standardize-pr-conventions`, record its outcome here, and run
      `/review-thorough` when the outcome is `REQUIRED` (or when a `RECOMMENDED` outcome is resolved to run). **Done**:
      the derivation returned `REQUIRED` on `[infrastructure-or-deployment] .github/PULL_REQUEST_TEMPLATE.md` — the
      router's `INFRASTRUCTURE` prefixes cover `.github/`. Pass 1 (`ses_e8ec3c067ffeW3Sr7HF0Y60tsq`) returned one
      nitpick: the Why called all six non-kebab `:`-titles "another prefix", while five carry a mid-title `:` and only
      one is a `docs:` prefix — status `FIXED_AND_VERIFIED` after the sentence was reduced to C1's and C2's figures.
      Pass 2 in the same session found no residue and no new defect, so the default budget of two passes is used. The
      consultation's later correction (rounds 7–9) changed three hunks the two passes had verified; a third deep pass is
      outside the budget and its expected value did not warrant the recorded decision one needs, so those hunks rest on
      the quick rounds and the manual pass.
- [ ] 4.4 Ask the owner for the manual review pass; commit, push, archive, and merge only after approval.
