# Process defects — correct-merged-unit-records

### correct-merged-unit-records#1 — Another unit's tree was taken over, and its claim overwritten

- **Recorded** — 2026-11-06, by the main agent, on its own observation while setting up this unit.
- **Stage** — proposal.
- **Expected** — a new unit that finds the primary checkout assigned to another unit — or whose ownership it cannot
  establish — works in a sibling worktree, and no unit takes over a tree another writer holds; occupancy is read from
  `git worktree list`, and the claim beside it names the unit the tree carries.
- **Observed** — in the primary checkout, which was on `record-unit-change-layering` with that unit's unit record, its
  review records and its change directory's files staged, this session ran
  `git switch -c correct-merged-unit-records --no-track origin/main`, which moved the tree onto a new branch and carried
  the staged files with it, and then wrote `none` into the claim file in place of `record-unit-change-layering`. The
  check that would have caught it — `git branch --show-current` before the switch — was not run.
- **Evidence** — `git worktree list --porcelain` now shows the primary on `record-unit-change-layering` and this unit's
  tree beside it; `docs/units/record-unit-change-layering/unit.md`'s `Workspace:` line names that unit's writer session,
  not this one; the staged paths are that unit's change dir and records; the claim file read
  `record-unit-change-layering` after it was restored.
- **Consequence** — repaired in the same turn: the branch was switched back, the claim rewritten, and this session's
  empty branch deleted; that unit's staged paths and their content were intact and no commit was made on either branch.
  For the intervening period the other writer's tree was on a foreign branch with an empty claim.
- **Knowledge boundary** — observed: the switch, the overwritten claim, the restore, and the restored state; not
  checked: what the other session did during that window, since its transcript is not this session's.

### correct-merged-unit-records#2 — A count of the unit's own review record froze twice, and its claim about the close

- **Recorded** — 2026-11-06, by the main agent, on the third recurrence across rounds 1, 3 and 7.
- **Stage** — review.
- **Expected** — a record of the unit's own growing review set points at `rounds.tsv` and lets the gate's
  `--expect-clean` certify the close, since a count and a "closing round clean" claim written while rounds are still
  arriving are falsified by the next round.
- **Observed** — three sites froze such a figure: round 1's process-defect `#1` said "seven staged paths" where the
  other unit's staged set moved to eight; round 3's hand-over said "eleven paths"; and the pull-request body draft said
  "six over the diff, the last closing clean" while round 6 had raised a finding and round 7 was in flight.
- **Evidence** — the three findings, `claim-unreproducible` each, and the current `rounds.tsv` whose rows moved under
  each claim.
- **Consequence** — three review rounds were spent on the same mechanism; the body and the checkpoint now point at the
  ledger, and `--expect-clean` carries the close.
- **Knowledge boundary** — observed: the three sites and their findings; not checked: whether the unit's record set is
  the only growing set it counts, since the sweep covered the hand-over, the body and the unit record only.
