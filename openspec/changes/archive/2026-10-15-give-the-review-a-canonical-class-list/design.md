# Design

## Context

See `proposal.md` — Why. The review reports each finding under a free-form class label, and the record, the repeat
marking and the digest all compare those labels. The facts this change rests on are in the tables below.

## Claims table

| Fact                                                                                  | Source (re-run)                                                                                                                                                        |
| ------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| the record held 55 distinct class labels before this change                           | `git show HEAD:docs/review-rounds.tsv \| grep -v '^#' \| cut -f4 \| tr ',' '\n' \| grep -vx -- '-' \| grep -v '^$' \| sort -u \| wc -l` → `55`                         |
| one phenomenon carried four labels                                                    | `git show HEAD:docs/review-rounds.tsv \| cut -f4 \| tr ',' '\n' \| grep -c "claims-table"` → `47` rows' worth across `claims-table`, `-source`, `-omission`, `-anchor` |
| a label used exactly once was recorded as repeated                                    | `git show HEAD:docs/review-rounds.tsv \| grep -n "claims-table-anchor"` → one row, its `repeated` column naming it                                                     |
| the contract left the label free-form                                                 | `git show HEAD:.opencode/agent/review-quick.md \| grep -n 'for example'` (four examples, not a list)                                                                   |
| the spec owns the classification and the repeat marking without fixing the vocabulary | `grep -n "each finding is classified" openspec/specs/showcase/quality/agent-skills/spec.md`                                                                            |

## The canonical classes

Each names **what must change**, so two reviewers agree on a label and a class is comparable across rounds and units.
The right-hand column is the mapping from the labels the record already carries (54 mapped; `compliance-miss` is the
record's own flag, not a class), so the older rows stay readable.

| Class                    | Observed labels it subsumes                                                                                                                                                                         |
| ------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `claim-unreproducible`   | claims-table-source, unsourced-claim, derived-claim, unverifiable-claim, stale-claim, documented-numbers, derived-number, unreproducible-reproduction                                               |
| `claim-missing`          | claims-table-omission, claims-table, claims-table-anchor, missing-row                                                                                                                               |
| `contradiction`          | contradiction                                                                                                                                                                                       |
| `incomplete-enumeration` | incomplete-enumeration, missing-artifact                                                                                                                                                            |
| `over-claim`             | over-claim, boundary, scope, robustness                                                                                                                                                             |
| `wording`                | wording, imprecise-claim, misleading-name, ambiguity, typo                                                                                                                                          |
| `formatting`             | formatting                                                                                                                                                                                          |
| `single-sourcing`        | single-sourcing                                                                                                                                                                                     |
| `missing-task`           | missing-task, missing-step, missing-record, missing-artifact-decision, missing-marker, missing-section, missing-delta, missing-content, missing-verification, unticked-task, task-state, task-check |
| `missing-scenario`       | missing-scenario, coverage-gap                                                                                                                                                                      |
| `impact-omission`        | impact-omission                                                                                                                                                                                     |
| `test-control`           | test-control, command-defect                                                                                                                                                                        |
| `dropped-content`        | dropped-content, incorrect-mapping                                                                                                                                                                  |
| `premise`                | premise, mechanism                                                                                                                                                                                  |
| `coherence`              | coherence, dangling-reference, stale-cross-reference, contingent-reference                                                                                                                          |
| `redundant-code`         | redundant-code                                                                                                                                                                                      |
| `docs-residue`           | docs-residue                                                                                                                                                                                        |
| `proposal-schema`        | proposal-schema                                                                                                                                                                                     |

The classes' definitions live once, in the contract the reviewer reads (`.opencode/agent/review-quick.md`); this table
records only which observed labels each subsumes, so the two cannot drift apart.

`compliance-miss` is not a class: it is the record's own column, a flag on a finding, and stays one.

## Decisions

**The classes name the change, not the symptom's taxonomy.** A class is what a fixer must do — re-derive a source,
supply a missing row, re-derive a boundary, point at one source — so the label survives being read by the next round and
by a retrospective, which is what the record and the digest need.

**The list is extensible in the same change.** A finding that fits none of the classes: the calling agent adds one to
the contract, in the change that raised it — as the contract states — so the vocabulary stays canonical instead of
drifting into a second long tail, the failure this change exists to repair.

**The older rows keep their labels.** The record's rows are its measured data, so the mapping is recorded here rather
than applied backwards; a later digest may normalize by it, and the record's header names the boundary.

**The class-exhaustion machinery is untouched.** The clause searches for every instance of a class's _shape_; a
canonical list makes that search and the repeat marking fire on the same word, which is the whole point.

## Risks / Trade-offs

- A canonical list can be read as a closed set → the extension rule is stated beside it, and the list is short enough to
  extend deliberately.
- Merging the tail risks losing a distinction a fix depends on → the classes are grouped by the _fix_, not by the
  symptom: `claim-unreproducible` (fix the source) stays apart from `claim-missing` (supply or delete the fact).
- A retrospective reading the older rows still sees the drift → the mapping is in this change's design, and the record's
  header says where the boundary is.
