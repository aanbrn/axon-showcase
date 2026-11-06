# Design

## Context

The parked idea proposed one sweep with three parts: delete the `advisor-*.md` files whose content "is already
summarized in the hand-overs", add the missing rationale to the store's requirement, and correct one round's transition
reason. Each premise was checked against the repository before designing, and two did not hold as written.

| Premise as parked                                          | What the repository shows                                                                                                                                                                                                                                                                  | Consequence                                                                                                                 |
| ---------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------- |
| Five `advisor-*.md` files                                  | `git ls-tree -r --name-only 9ac45da7 docs/units \| grep 'findings/advisor-'` returns four: two in `surface-projector-backlog`, two in `pin-wrapper-distribution-checksum`                                                                                                                  | The scope is four files; a fifth would have been invented or hunted for                                                     |
| Their content is already summarized in the hand-overs      | Each hand-over summarizes the pass's _outcomes_ — the fixes applied and the open question's settlement — never the findings' locations and classes; `surface-projector-backlog` names both files by path, `pin-wrapper-distribution-checksum` only as "the advisor's pass, in `findings/`" | Deleting without folding loses instances that no record holds                                                               |
| Round 12 of `surface-projector-backlog` is the deep review | `findings/round-12.md` holds the ADR-0023 over-claim the round-13 section calls "the deep review's finding", and no other row shares its session                                                                                                                                           | The parking's rationale holds: the permitted case is `definition-changed`, because `review-thorough`'s definition ran there |

The third part — the missing rationale — stands, with one correction: the stale reason it names is not in the
requirement. The requirement never carried a rationale at all; "the hand-over leaves with the archive" is the
introducing proposal's (`openspec/changes/archive/2026-11-03-persist-review-round-findings/proposal.md`), a historical
record this change does not rewrite. What the requirement lacks is any statement of why the store exists _now_.

Two facts bound the mechanism:

- `tree_instances` globs `*/findings/round-*.md` only, so an extra file in the store is neither required nor rejected —
  the same invention recurs without a failure.
- The four files were written independently in two units, which makes this a missing rule rather than one unit's slip.

One part of the parked idea was dropped from the first draft of this design and is restored here: it asked for "the same
class recorded in each unit's `process-defects.md`", and neither merged unit's record carries it.

## Goals

- Every advisor consultation's finding instances survive the cleanup, readable in the unit's permanent record.
- The store's requirement states why it exists and what it may hold, so the next consultation has a stated home.
- The store's verification fails on a file the store does not hold, rather than ignoring it.
- Round 12's transition states what ran, without rewriting any other row.

## Non-Goals

- Reclassifying the other `caller-restarted` rows (`surface-projector-backlog` 5 and 13,
  `pin-wrapper-distribution-checksum` 6, 10 and 11). Each needs its own evidence and the parking names none of them;
  they stay as recorded.
- Making an advisor consultation a first-class record kind beside a round. The hand-over already carries the outcome,
  and `surface-projector-backlog` already records its advisor pass in a section it labels "no review round"; the change
  adds the instances to that section rather than inventing a store.
- Re-writing the archived proposal's rationale, or any historical record beyond the two corrections this change states.
- Detecting a consultation's findings smuggled into an existing `round-<N>.md`: the class-set comparison catches a
  mismatch, not a well-classed insertion. The stated scope and the file-set check make the sanctioned home explicit; a
  content-level attribution check has no reliable signal.
- Extending the pre-commit guard to the store's file set: the guard's staged selection is built from changed
  `round-<N>.md` paths, so a stray file arrives with a commit and is caught by the next `check`, not before it. Widening
  that selection is a change to the guard, not to the store's rule.

## Decisions

### The four files fold into the sections that already name each pass, and the references are repointed

`surface-projector-backlog` already has the section — "Control evidence — the deployed positive backlog (no review
round)" — so the fold adds its four entries there and records that the second pass raised none. The pin unit's two
passes differ in time: its proposal pass belongs in the round-4 section that names it, while its implementation pass
concerns the shipped tool and gets its own section headed as a pass with no review round, the shape the surface unit
already uses. Both hand-overs cite the deleted files in several later sections; those citations become a citation of the
folded section, because a reference to a file that no longer exists is worse than none.

Rejected: deleting alone (loses instances no record holds); admitting `findings/advisor-<n>.md` to the store (adds a
record kind, a writer and a gate for a pass whose outcome the hand-over already states); leaving the files (the layout
violation the parking exists to remove).

### Round 12's reason becomes `definition-changed`

`caller-restarted` records a continuity violation: a reviewer session replaced without an evidenced recovery case, which
the reviewer's own rule reads as a violation and which does not authorise the gate to continue. At round 12 the quick
reviewer did not continue under a restart — the deep review ran, which is a different definition — so the permitted case
that fits is `definition-changed`. The row records only that value: the ledger's last field is the closed reason enum,
so an explanation cannot live there, and the hand-over's round-13 section already states what the pass was.

Rejected: deleting the row (it would break the ledger's continuity chain and hide what happened); leaving
`caller-restarted` (it charges the unit with a violation whose cause is a permitted replacement).

### The requirement gains the rationale and the store's scope

The store's requirement owns what `findings/` holds, so the round-only rule lives there; `agent-skills` keeps its own
sentence about where a round's instances are written, which the new scope does not change, so it gains no delta. The
review gate's writer step points at the store's requirement instead of restating it. The rationale is stated as the
current one — the hand-over is written before a round runs and its findings arrive after, and the build cross-checks
each round against its file — not as a correction to history.

### The verification rejects a non-round file in the store

The invention recurred twice without a failure because `tree_instances` globs `round-*.md` and ignores everything else.
Extending the mode to fail on any other tracked file under `findings/` turns the file-name half of the stated scope into
a check, at the cost of one glob, one comparison and three tests. It does not judge content, which the Non-Goals state.

Rejected: stating the rule with no check (the record shows that is what produced this unit); a separate task or mode
(the existing mode already owns the store's shape and runs in `check` and in the guard).

### The two merged units record the class, and a wrong count is corrected in place

The parked idea asked for the class in each merged unit's record, and neither has it. Because a record states what was
observed, `assert-paketo-pin-coverage#3`'s "five such files between them" is corrected by an appended dated line rather
than by rewriting its observation.

## Risks

- **The fold could lose an entry.** Mitigated by copying each file's entries verbatim and by a control that greps every
  folded class/location pair in the hand-over after deletion, against `git show 9ac45da7:<file>`.
- **A future pass could write an unregulated file and it would land before the check sees it.** The verification runs
  over the tracked tree, so it fails from the commit that adds the file onward; the pre-commit guard does not see it,
  because the guard selects a unit from changed `round-<N>.md` paths and builds its index map from round files alone.
  The change keeps that scope rather than widening the guard, so the protection is the check, not the commit.
- **The reclassification could read as excusing a violation.** Mitigated by scoping it to the one row whose session ran
  a different definition, and by leaving every other `caller-restarted` row untouched.
- **Repointing a hand-over reference could lose the provenance** of which file held what. Mitigated by the fold's
  heading naming the consultation and its pass, so the section says what the file said.

## Claims table

| Claim                                                                 | Source                                                                                                                                                                       | Result                                                                                                                                                        |
| --------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The store holds four `advisor-*.md` files                             | `git ls-tree -r --name-only 9ac45da7 docs/units \| grep 'findings/advisor-'`                                                                                                 | four: `surface-projector-backlog` 1–2, `pin-wrapper-distribution-checksum` 1–2                                                                                |
| The hand-overs state outcomes, not instances                          | `git grep -n advisor 9ac45da7 -- 'docs/units/*/review-handover.md'`                                                                                                          | `surface-projector-backlog` names both files by path and summarizes fixes; `pin-wrapper-distribution-checksum` says only "the advisor's pass, in `findings/`" |
| Round 12 is the deep review                                           | `docs/units/surface-projector-backlog/findings/round-12.md` and `review-handover.md:180-184`                                                                                 | `round-12.md` holds the ADR-0023 over-claim; the round-13 section calls it "the deep review's finding" and says nothing else changed                          |
| The advisor pass is a separate, non-round record                      | `review-handover.md:196-208`                                                                                                                                                 | the section is headed "Control evidence — the deployed positive backlog (no review round)" and names `findings/advisor-1.md`                                  |
| `surface-projector-backlog`'s fold is four entries                    | `findings/advisor-1.md`                                                                                                                                                      | two findings, one hypothesis, one open question; `advisor-2.md` is `- none`                                                                                   |
| `pin-wrapper-distribution-checksum`'s fold is nine entries            | `findings/advisor-{1,2}.md`                                                                                                                                                  | five and four                                                                                                                                                 |
| The requirement carries fourteen scenarios                            | `openspec/specs/showcase/quality/commit-hygiene/spec.md:1346-1437`                                                                                                           | fourteen `#### Scenario:` headings                                                                                                                            |
| The requirement covers `round-<N>.md` only                            | same lines                                                                                                                                                                   | one file per recorded round; the scenarios name `round-<N>.md`                                                                                                |
| An extra file in the store is invisible today                         | `scripts/commit-hygiene.py:1805-1820`                                                                                                                                        | `tree_instances` globs `*/findings/round-*.md`; nothing inspects other files                                                                                  |
| The stale rationale is the archived proposal's, not the requirement's | `openspec/changes/archive/2026-11-03-persist-review-round-findings/proposal.md`                                                                                              | its Why says "the hand-over leaves with the archive"; the requirement states no rationale                                                                     |
| Nothing else cites the advisor files by name                          | `git grep -n "advisor-1.md\|advisor-2.md" 9ac45da7`                                                                                                                          | two hits, both in `surface-projector-backlog`'s hand-over                                                                                                     |
| Neither merged unit records the invented-file class                   | `grep -c "^### " docs/units/surface-projector-backlog/process-defects.md` and `git show 9ac45da7:docs/units/pin-wrapper-distribution-checksum/process-defects.md \| head -3` | `surface-projector-backlog` has three records, none about the layout; the pin unit states "No process defect observed while working this unit"                |
