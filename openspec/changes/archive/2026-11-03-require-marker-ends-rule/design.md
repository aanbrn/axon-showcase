# Design

## Context

`AGENTS.md`'s generalization section stated the record's shape at the base revision (`26c92a96`): "Each proposed rule
carries its origin in a `captured: <unit>` marker … Place the marker at the end of the rule it records; a pre-commit
guard enforces that part in `AGENTS.md` and in the project-authored agent and skill definitions … a marker lies inside a
rule block, never on a plain bullet." The guard enforced the second clause only: `_marker_is_in_rule_block` finds the
nearest preceding bold-lead line and accepts any marker below it, so
`… some rule text captured: unit and the rule continues …` passes (claims row 1). This change also edits that sentence,
because the guard can only check the narrower block-local half (task 5.3).

The capability spec describes the same half — a marker "lies within a rule block — a bold-lead bullet or a bold-lead
paragraph — and never on a plain `- Text` bullet" — and says nothing about where in the block it sits (row 3). So the
guidance claims more than the spec and the code deliver, and the gap is observable: thirteen blocks in the corpus
carried a marker followed by more rule text (row 4), which is exactly the shape the idea records from the
`annotate-the-reviewer-wrapper-recurrence` unit's round 2.

A marker's shape varies in ways the check must not reject: the chain may carry several markers, a name may wrap onto the
next line, a citation may follow a name, and an `evidence:` tag line may follow the chain (row 5). A predicate that
required the marker to be the block's last character would fail all four.

Fenced content is not rule prose, so the check skips it: `.opencode/skills/gradle-build/SKILL.md`'s command block
carries a `# captured: address-scan-findings-2026-10-25` line recording the origin of the guidance its comments state,
which belongs to that command block rather than to a rule the marker could end. The delta states the exemption in those
terms, not as "an example".

## Goals / Non-Goals

**Goals.**

- A marker followed by rule text fails the build verification and the pre-commit guard, naming its location.
- Markers that already end their rule — appended to the last sentence, wrapped, chained, cited — keep passing.
- The corpus satisfies the rule after the change, so the live `AGENTS.md` case keeps passing.

**Non-Goals.**

- Changing which files are scanned, how a marker is recognised, or how a backticked mention is skipped.
- Re-deciding the marker convention itself (one marker per rule, the merge-in rule, the growth bound).
- Enforcing marker _existence_: a rule without a marker is still outside this check.

## Decisions

**1. The predicate is "nothing but the chain may follow the marker inside its block".**

- Chosen: the marker must end the bullet or paragraph it sits in — the block from that block's first line to the next
  bullet (at any indent), numbered step, bold-lead paragraph, heading, table row, fence, or blank line. The text from
  the marker to the block's end must parse as a marker chain: one or more `captured: <name>`, each name optionally
  wrapped onto the next line and optionally followed by a `(#NNN)` citation. Trailing blank lines and an `evidence:` tag
  line may follow the chain, and a marker inside a fenced block is not judged.
- Rejected — requiring the marker on the block's last physical line: a wrapped name and a citation-after-name both put
  the chain's tail on a later line, and both are live shapes.
- Rejected — checking only the marker's own line: a marker at the end of a line whose next line continues the same block
  is the same defect, and the corpus has that shape.
- Rejected — a blanket "no prose after the marker" over the file: it would flag the `evidence:` tag lines and the
  legitimate next-rule text, so the block boundary is what makes the predicate exact.

**2. The block boundary is the bullet or paragraph, not the whole bold-lead rule.**

- Chosen: a bullet's or bold-lead paragraph's own wrapped lines belong to it, while an indented sub-bullet, a numbered
  step, a heading, a table row, a fence, and a blank line each end it — so a plain paragraph is a block of its own and
  the marker is judged against the block it ends.
- Rejected — extending the block to the next bullet, bold-lead line, or heading, spanning every paragraph in between:
  measured on the base revision, that boundary merges paragraphs that each carry their own chain. The
  `**Completion criteria**` block of the update-check workflow runs from line 969 to 1005 and holds three separate
  chains (at 972, 984, and 992–994) belonging to three different subjects; moving a mid-block chain to "the rule's end"
  would relocate three rules' provenance onto the fourth, which is worse than the defect it repairs.
- **Accepted false negative:** a marker that ends the first paragraph of a rule whose second paragraph continues it
  passes, because the check judges the block and not the bold-lead rule. The spec's "ends it" is scoped to the block for
  the same measured reason.

**3. The thirteen violating blocks' chains move to their block's end.**

- Chosen: cut the chain from where it sits and append it after the rule's last text, preserving order and the merged-in
  convention (a rule the analysis appended to keeps its chain at the end).
- Rejected — rewriting the rules around the markers: the marker's placement is the defect, and the rule text is
  evidence.
- Rejected — grandfathering the thirteen by name: the check's subject is the current corpus, and an acknowledgement list
  would let the defect back in silently; the moves are mechanical and reviewable.

**4. One `MODIFIED` block, on the placement requirement.**

- Chosen: the requirement already owns the predicate, so its description gains "and ends it" and its "correctly placed"
  scenario's WHEN gains the same, beside a new scenario for the mid-rule case.
- Rejected — a second `MODIFIED` block on the guard requirement: it states the outcome generically ("a `captured:`
  marker is misplaced in a staged guidance file") and its scenario gives the plain-bullet case as an example, so the
  guard's contract is unchanged; the placement requirement owns what "misplaced" means.

## Risks

- **The block boundary is a heuristic over Markdown.** A construct it does not model — a definition list, a block quote
  — could end a block early or late; the corpus has none, and the live `AGENTS.md` case is the regression guard. A
  fenced block ends the block it follows and is outside the check, since its content is not rule prose.
- **A marker moved past a rule's sub-bullets reads as attached to the last sub-bullet.** The moved chains in this change
  are recorded in the diff, so a reader can see which rule each belongs to; the convention itself already accepts the
  end-of-rule position.
- **`evidence:` tag lines are an allowance.** They are part of the guidance-evidence convention, not rule prose, and the
  delta states it; the corpus places every one of them after a blank line, so the allowance is exercised only by the
  test fixture that puts the tag directly under the chain.

## Claims

Base revision for the before-state rows: `26c92a96` (`git merge-base HEAD origin/main`), the base the branch was
fast-forwarded to; `c93359c2` was the proposal-time base, and every row here reproduces at both.

| #   | Claim                                                                                                   | Source                                                                                                         | Result                                                                                                                                                                                        |
| --- | ------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The guard accepts a marker anywhere in a bold-lead block                                                | `_marker_is_in_rule_block` in `scripts/commit-hygiene.py`, base revision                                       | it returns true when the nearest preceding lead exists, with no reference to what follows the marker                                                                                          |
| 2   | `AGENTS.md` states the end-of-rule placement and says the guard enforces it                             | `AGENTS.md`, "Each proposed rule carries its origin …"                                                         | "Place the marker at the end of the rule it records; a pre-commit guard enforces that part …"                                                                                                 |
| 3   | The spec states only the "inside a block" half                                                          | `openspec/specs/showcase/quality/commit-hygiene/spec.md`, "Captured marker placement is verified by the build" | "lies within a rule block — a bold-lead bullet or a bold-lead paragraph — and never on a plain `- Text` bullet"                                                                               |
| 4   | Thirteen rule blocks carry a marker followed by more rule text                                          | the detector's run over the base revision's corpus, with the block and chain grammar of decisions 1 and 2      | 394 markers in 22 scanned files; 13 blocks violate on 18 marker lines — `AGENTS.md` (8), `check-and-test-authoring` (1), `gradle-build` (2), `review-gate` (2)                                |
| 5   | Marker shapes that must keep passing                                                                    | the same scan's passing cases                                                                                  | marker appended to the last sentence, a chain of several markers, a name wrapped onto the next line, a `(#NNN)` citation after a name, and a rule whose second paragraph ends with the marker |
| 6   | The existing tests encode the lenient behaviour                                                         | `scripts/test-commit-hygiene.py`, `MisplacedMarkerTests`, base revision                                        | `test_a_merged_into_bullet_with_a_mid_item_marker_passes` asserted a marker followed by "Later text appended." passes — the case this change rejects                                          |
| 7   | The live `AGENTS.md` is a regression case                                                               | `test_the_current_agents_md_has_no_misplaced_markers`                                                          | it reads the repository's own file, so the reconciliation must land before the suite is green                                                                                                 |
| 8   | The guard and the build share the predicate                                                             | `check_staged` and `check_captured_markers` in `scripts/commit-hygiene.py`                                     | both call `find_misplaced_markers`, so one change tightens both                                                                                                                               |
| 9   | Reachability: the new predicate reports the thirteen before the reconciliation, and none after          | the detector's run over the corpus, before and after the moves                                                 | `--markers` reported the 13 blocks (18 lines) on the base revision's corpus and none after the moves                                                                                          |
| 10  | The control bites: a mid-item marker and a mid-paragraph marker fail; a chain at the block's end passes | `MisplacedMarkerTests` fixtures                                                                                | the new predicate reports `[1]` for both fixtures, the base revision's reports `[]` for each, and the end-of-block fixture passes under both                                                  |
| 11  | No live marker is lost by the moves                                                                     | the detector's marker count per file before and after the reconciliation                                       | unchanged: `AGENTS.md` 258, `check-and-test-authoring` 25, `gradle-build` 22, `review-gate` 54, and 394 across the 22 scanned files, before and after                                         |

Dismissals, one reason per candidate kind `scripts/claim-scan.py` reports:

- `[count]` — a numeral labels a rule, a file, a marker, or a task; the corpus figures are rows 4 and 11, each derived
  by the scan its Source names.
- `[absolute]` — a universal or absence qualifier is normative text (a requirement, a decision, a scenario) or a rule's
  own wording; the factual instances are the claims rows.
- `[cross-ref]` — the candidates are slash-joined identifiers and the paths of files the change reads or edits; the
  change cites no tracker reference.
- `[path]` — every candidate is a code span carrying a command, a path the change reads or edits, or a marker the check
  uses; each path exists in the tree.
