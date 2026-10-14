# Design

## Context

See `proposal.md` — Why. The pass the review-loop bullet describes has only a ticked box as its output, so each round
re-derives the classes it names; the figures this change rests on are in the table below.

## Claims table

Each row is a fact this change states about its evidence — the prior artifacts it rests on — with the source that
derives it:

| Fact                                                                                                                                                      | Stated in   | Source (re-run)                                                                                                                     |
| --------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| the record covers five units, running 1–13 rounds                                                                                                         | `design.md` | `awk -F'\t' '$1 !~ /^#/ && $2 != "derive-the-change-claims" { n[$2]++ } END { for (u in n) print n[u], u }' docs/review-rounds.tsv` |
| the two most frequent classes are `documented-numbers` and `contradiction`, 15 each                                                                       | `design.md` | the same file and filter, the classes column (`$4`)                                                                                 |
| the compliance misses are 20/18/13/7/0 per unit                                                                                                           | `design.md` | the same file and filter, `$6`                                                                                                      |
| the record shows 24 distinct classes                                                                                                                      | `design.md` | the same file and filter, distinct class count                                                                                      |
| the recurring classes are `documented-numbers`, `contradiction`, `test-control`, `unverifiable-claim`, `single-sourcing`, and `missing-artifact-decision` | `design.md` | the same file and filter, the repeats column (`$5`) at ≥3                                                                           |

The filter excludes this unit's own rows: the corpus is live, so an unfiltered count would move every time this unit's
review appends a row — the table counts the units it is evidence _about_, not itself.

## Decisions

**The table is the change dir's analogue of the retrospective's derived-facts block (#560).** It lives in `design.md`,
or in `tasks.md` for a unit that writes no `design.md`, or on the pull-request checklist line for a unit with no change
dir. The digest computes the retrospective's countable claims; a change has no digest, so its claims table is authored —
but it records the source alongside the claim, which is what lets the review re-run it and compare.

**Rows seeded from the record's recurring classes**, one row per fact this change states about its evidence; the table's
own row names them. The change's mechanism claims are its argument, not its evidence, and are excluded.

**The caller passes instances, and the reviewer compares them.** `review-quick.md` already expects the caller to name
the prior round's findings. The per-round record (`docs/review-rounds.tsv`) carries classes and counts but no
`file:line` or claim, so the caller passes each finding's `file:line` and its claim from the prior round's review
report, and the reviewer reports an unfixed prior instance or a new instance of an exhausted class — the latter being
the loop bound's escalation trigger.

## Risks / Trade-offs

- The table is authored, so it can be incomplete → the review's first check is a claim in the artifacts missing from the
  table, which makes an omission a finding rather than another round.
- A per-claim command can be costly to write → a coherence claim records the artifact pair instead of a command.
- This is a mechanism change to existing artifacts, deliberately, as the table's recurrence row shows.
