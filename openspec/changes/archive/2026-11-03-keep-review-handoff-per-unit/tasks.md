# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is `docs/units/keep-review-handoff-per-unit/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The pre-write derivation pass ran, and the claims table below is its record. No `design.md` exists, so the
      table lives here.

| #   | Claim                                                                                   | Source                                                                                                                                                                | Result |
| --- | --------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------ |
| 1   | Eleven `review-handover.md` files exist at the base, all in archived change directories | `git ls-tree -r --name-only 69b249d1 \| grep -c 'review-handover\.md$'`; `git ls-tree -r --name-only 69b249d1 \| grep 'review-handover\.md$' \| grep -vc '/archive/'` | 11; 0  |
| 2   | The review-gate preflight named no hand-over path before this change                    | `git show 69b249d1:.opencode/skills/review-gate/SKILL.md \| grep -c 'review-handover'`                                                                                | 0      |
| 3   | No live site named a per-unit hand-over path at the base                                | `git grep -l 'docs/units/<unit>/review-handover' 69b249d1 \| wc -l`                                                                                                   | 0      |

**Dismissals**, from `python3 scripts/claim-scan.py --base origin/main`, one reason per kind:

- **absolute** — each candidate states a rule this change adds, or quotes the owner's idea.
- **count** — each is a figure a row states and its command reproduces.
- **cross-ref** — the candidate is a phrase of this change's own artifacts.
- **path** — a planned path names a file this change creates or an existing one.

## 1. The hand-over's per-unit home

- [x] 1.1 `.opencode/skills/review-gate/SKILL.md` names `docs/units/<unit>/review-handover.md` where its preflight
      requires the record before waiting on a quick-review invocation: one section per invocation appended to the unit's
      file, the deep review's hand-over left to `/review-thorough`, and an earlier hand-over read where it was recorded.
- [x] 1.2 `docs/contracts/process-bindings.md` keeps the findings store (`findings/round-<N>.md`) as `prior-findings`'
      primary source and changes only its historical branch: for a round recorded before the store, the checkpoint or
      the hand-over, with the hand-over read at the unit's file or in the change directory or its archive.
- [x] 1.3 The parked idea for this work is removed from `docs/ideas.md`.

## 2. The gates

- [x] 2.1 `openspec validate --all` passes, and `./gradlew spotlessApply` runs after the final edit.
- [x] 2.2 The touched build's `check` passes.
- [x] 2.3 `docs/process-defects/keep-review-handoff-per-unit.md` records what the unit observed.
- [x] 2.4 The review gate runs: quick rounds 1–6 with a clean closing round; the routing decision is `RECOMMENDED`
      (`repeated-class`, three rows). The owner's decision, taken on the technical advisor's consultation, is to skip
      the deep review: the repeats were record-discipline fixes rather than an unresolved interaction, and a substantive
      contradiction between the skill, the binding, and the delta reopens the decision. The owner's manual pass approves
      the implementation.
