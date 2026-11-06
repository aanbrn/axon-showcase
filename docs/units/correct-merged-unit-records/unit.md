# Unit: correct-merged-unit-records

Workspace:
`unit=correct-merged-unit-records; path=/Users/afanasyev/Development/axon-showcase-correct-merged-unit-records; branch=correct-merged-unit-records; writer=ses_e5dece39fffeYkl8XkXm6Yg9Q3`

State: active

A unit with no change directory: it corrects three merged units' records and one parked idea list. Its plan is this
record and the diff.

## Resume checkpoint

- **Time and revision** — 2026-11-06, forked from `origin/main` at `0b236cde`; nothing committed.
- **Workspace** — the `Workspace:` record above: the sibling worktree, because the primary carries
  `record-unit-change-layering` for another writer.
- **Stage** — the diff is complete and its quick loop is recorded in `rounds.tsv`, whose closing round
  `commit-hygiene --unique-rounds --expect-clean` certifies as clean: a dated correction in `reconcile-review-records`'
  and `assert-paketo-pin-coverage`' hand-overs, `assert-paketo-pin-coverage`'s process defect `#6`, the two
  `docs/ideas.md` removals, the repointed pointers in `unit-layout-allowlist`'s hand-over and process-defect record, and
  this unit's record set.
- **Next step** — merged in pull request #851; the tree and its branch are released after the merge.
- **Change set** — staged: `reconcile-review-records`' and `assert-paketo-pin-coverage`' hand-overs,
  `assert-paketo-pin-coverage`'s and `unit-layout-allowlist`'s process-defect records, `unit-layout-allowlist`'s
  hand-over, `docs/ideas.md`, and this unit's record set.
- **Blockers and open decisions** — none.
- **Pending decision** — none: the owner approved the finished unit and gave the delivery instruction on 2026-11-06
  ("Одобряю. Отгружай."), approving with it the advisor's recommendation to skip the `RECOMMENDED` deep review.
- **Findings no record holds** — none.
- **Operations in flight** — none.

## Pre-write derivation pass

The pass is recorded here rather than in a `design.md`, since this unit has no change directory; its claims table sits
beside it.

| Claim                                                    | Source                                                                                                 | Result                                                                                          |
| -------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------- |
| Every row's sixth field equals its round's finding count | `awk -F'\t' '{print $3, $6}'` over both ledgers, crossed with each `findings/round-<N>.md` entry count | 23 rows — 14 and 9 — each equal to its round's instance count                                   |
| The sixth field means the compliance-miss count          | `showcase/quality/commit-hygiene`'s record-format requirement; `.opencode/agent/review-quick.md`       | the requirement names the field, the definition asks how many findings "were a compliance miss" |
| The merged units' compliance figures are unestablished   | the ledgers, their instance files, and `unit-layout-allowlist#2`                                       | no stored artifact carries the judgment; the instance files carry each finding's class          |
| The guard refused that unit's archive commit             | this session's refused commit and its retry                                                            | not verifiable from the repository; recorded as the session's account                           |
| `docs/ideas.md` keeps its other entries                  | `verifyIdeasSections` and the section's entries                                                        | the IDEA-integration entry remains                                                              |
