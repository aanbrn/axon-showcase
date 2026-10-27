# record-the-settle-step-defect

### record-the-settle-step-defect#1 — An index-based edit replaced the wrong record and truncated the file

- **Recorded**: 2026-10-27, by the writing session (`ses_e84c9b955ffeY6V3z6ZN1VELva`).
- **Stage**: implementation.
- **Expected**: applying the review finding to record #2's `Expected`, `Observed` and `Knowledge boundary` fields in
  `docs/process-defects/clarify-readme-self-learning-wording.md` changes those fields of that record and nothing else.
- **Observed**: the edit located each field with `str.index`, which returned the first occurrence in the file — record
  #1's `Expected` and `Observed` — and replaced them with record #2's text; locating the knowledge boundary the same way
  and writing from that offset to the end of the file then deleted record #2 entirely. The file was left with one record
  carrying another's fields.
- **Evidence**: `wc -l` reported 19 lines after the edit; the surviving heading was #1, its `Expected` and `Observed`
  were #2's text, and no `#2` heading remained; the file was restored with
  `git show HEAD:docs/process-defects/clarify-readme-self-learning-wording.md` and rebuilt as a whole-file write.
- **Consequence**: the merged file was corrupt for the interval between the edit and the restore; its content was
  recovered from `HEAD` intact, the correction was re-applied, and this record moved to this unit's own file in the next
  review round. Nothing remains broken.
- **Knowledge boundary**: observed the corrupt file, the 19-line count and the restore. Did not preserve the corrupt
  file's own copy, so the pre-edit line count is not derivable from the tree. Did not check whether any other edit in
  this unit's history used the same first-occurrence lookup.
- **Decision** — no-change: the programmatic-edit gotcha already requires scoping a lookup to the owning block and
  deriving the item's end from the structure.

### record-the-settle-step-defect#2 — A repair replacement dropped the words it was meant to keep

- **Recorded**: 2026-10-27, by the writing session (`ses_e84c9b955ffeY6V3z6ZN1VELva`).
- **Stage**: implementation.
- **Expected**: replacing the figure in record #1's `Evidence` sentence leaves the rest of that sentence intact.
- **Observed**: the replacement's matched span ended with "the surviving heading was #1," while its replacement text
  ended at "the surviving heading", so the words "was #1," were dropped and the sentence read "the surviving heading its
  `Expected` and `Observed` were #2's text".
- **Evidence**: the line as read back after the replacement; it was repaired by rewriting that line whole, after which
  it read "the surviving heading was #1, its `Expected` and `Observed` were #2's text".
- **Consequence**: record #1 carried a broken sentence for the interval between the replacement and the repair; nothing
  else changed. Repaired in the same turn.
- **Knowledge boundary**: observed the edited line and its repair. Did not check the replacement's span against the
  sentence before writing it, and the slip is visible only in file reads taken around the edit.
- **Decision** — no-change: the span-edit rule already requires replacing the whole unit in one edit and re-reading it
  against the pre-edit facts.

### record-the-settle-step-defect#3 — An unreproducible line figure was replaced with another one

- **Recorded**: 2026-10-27, by the writing session (`ses_e84c9b955ffeY6V3z6ZN1VELva`).
- **Stage**: implementation.
- **Expected**: correcting the figure round 2 named makes the `Evidence` reproduce from the tree; the finding offered
  two routes — state the measured pre-edit figure, or state the result alone.
- **Observed**: the first correction replaced 38 with 36, which counts the file after that correction, and attributed it
  to the state the corrupt edit had replaced; round 3 found that neither figure reproduces for that state. The second
  correction removed the comparison figure and keeps the 19-line result alone, with `Knowledge boundary` recording that
  the pre-edit count is no longer derivable.
- **Evidence**: `docs/review-rounds.tsv`'s rounds 2 and 3 for this unit both carry `claim-unreproducible`, round 3's
  recorded as a repeat; the current `Evidence` field carries the 19-line result only.
- **Consequence**: one further review round spent on the same sentence; the field now retains the observed result
  without the unsupported comparison. Nothing remains broken.
- **Knowledge boundary**: observed the two rounds and the final text. Did not preserve the pre-edit file, so the count
  it measured cannot be re-derived.
- **Decision** — no-change: `review-gate` §1 already requires deriving a figure from the mechanism that produces it and
  deleting a clause its source does not support.
