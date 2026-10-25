# Tasks

Workspace: unit=remind-the-anchor-rule-at-authoring;
path=/Users/afanasyev/Development/axon-showcase-remind-the-anchor-rule-at-authoring;
branch=remind-the-anchor-rule-at-authoring; writer=ses_e8d34533fffeyvibaCv54L62xc

## 1. Records

- [x] 1.1 Confirm the workspace against the registry: `git worktree list --porcelain` shows this path on
      `refs/heads/remind-the-anchor-rule-at-authoring`, the branch was created from freshly fetched `origin/main`
      (`efbe072c`) with `--no-track`, and the primary checkout is untouched.
- [x] 1.2 Run the pre-write derivation pass and land it as `design.md`'s claims table; re-derive the table if the
      change's facts move before the manual pass.

## 2. Deliver the rule at authoring time

- [x] 2.1 Add the pointer item from `design.md` decision 1 to `rules.design` in `openspec/config.yaml`. Verify: the item
      is a single-quoted scalar folded across two lines, `./gradlew check` stays green, and
      `openspec instructions design --change remind-the-anchor-rule-at-authoring --json` returns it.
- [x] 2.2 Add the identical item to `rules.tasks`. Verify: the two lists carry the same text (both `rules` arrays show
      it verbatim), and the rule itself remains in the skill only (`grep -c "never by a line number"`: config `0`, skill
      `1`).
- [x] 2.3 Verify delivery through the CLI, not the file, with the commands and counts recorded at `design.md` row 3; the
      pointer appears verbatim in both `rules` arrays (design 10, tasks 12).

## 3. Verification

- [x] 3.1 Run `./gradlew spotlessApply` and then `spotlessCheck` after the last edit; the change's markdown is
      formatter-owned, and the config is not.
- [x] 3.2 Run `openspec validate --all` and confirm the change validates with `skip_specs: true` and no delta spec.
- [x] 3.3 Run `./gradlew check` and confirm it is green; no local task reads `openspec/config.yaml` (`design.md` claims
      row 10), so the list-shape probe is the CI `build` job's and runs on this pull request.

## 4. Review gate

- [x] 4.1 Ran the `review-quick` loop with the session-continuity preflight to a clean close: fifteen rounds, one row
      each in `docs/review-rounds.tsv`, both ledger checks run after every append. Each row carries its round's classes
      and compliance misses; rounds 5, 9, 12 and 15 raised none, and 15 closed the loop. Every finding was fixed and
      re-verified in the round that followed it. Round 13 replaced the reviewer session without a recovery case — the
      ledger's row records that continuity violation.
- [x] 4.2 Derived the deep-review floor: `RECOMMENDED`, signal `[repeated-class]`, resolved by running the deep pass
      once (`ses_e8c5c5765ffeTtAtt023RmW0e9`, initial). Its one nitpick — claims row 2's "nine days" figure, which its
      own command did not emit — is `FIXED_AND_VERIFIED`: the row now records the incident's merge date beside the
      rule's introduction date.
- [x] 4.3 Ran the `lesson-capture` decision and recorded the outcome. **Applied:** one added item per list (the
      locator). **`already covered`:** the capture's second candidate — a rule that a config rule is verified through
      what the CLI delivers — restates `AGENTS.md`'s config gotcha ("probe the consumer's read path"), so no root rule
      and no growth; the observation is kept as the locator wording's rationale in `design.md`. Earlier recurrence
      causes are likewise `already covered` by `review-gate` §1 and the durable-claims rules. **Net deltas:**
      `AGENTS.md` 0 lines, `openspec/config.yaml` +4 lines (two 2-line items). No `captured:` marker on the config
      items: the guard reads `AGENTS.md` and the `.opencode/` definitions only, and a marker there would ride into every
      delivered rule string.
- [x] 4.4 Judge the capture's second candidate as `already covered` (see 4.3) and leave `AGENTS.md` untouched. Verify:
      `git diff efbe072c -- AGENTS.md` is empty and `check` is green.
- [x] 4.5 Stage the unit's new files (explicit paths), ask the owner for the manual review pass, and end the turn —
      nothing committed, pushed, opened as a PR, or archived before the approval. Result: the owner approved on
      2026-10-26; shipped as PR #694 (commit `4bccceba`), archived in that pull request.

## Workflow follow-up

- After the owner approves: run CI, archive the change (`skip_specs`), and merge per the `ship-change` skill; remove
  this worktree afterwards.
