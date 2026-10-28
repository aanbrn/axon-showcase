# Process defects — guard-the-ledger-at-commit

Process defects observed while working this change. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### guard-the-ledger-at-commit#1 — Structured-file edits used boundaries the file did not have

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the proposal round.
- **Stage** — proposal (the claims table and the delta).
- **Expected** — an edit to a Prettier-padded table or a bullet list targets a boundary the file's own structure
  establishes: a row identified by its text, a list item ended by the blank line that separates entries.
- **Observed** — several replacements anchored on an unpadded table row did not match after `spotlessApply` (the padded
  form differs), and the idea-removal script's `rindex("\n\n")` boundary found no match between the entry and its
  neighbour, so nothing was written; each was redone with a line-based insertion or a blank-line scan.
- **Evidence** — the aborted scripts' `AssertionError` on the claims table rows in `design.md` and the idea-removal
  script's `ValueError: substring not found`; the restored files show the line-based edits.
- **Consequence** — three extra edit attempts in this unit. The asserts stopped every one before a partial write, so no
  artifact carried a misedit; the delta and the ideas file are complete.
- **Knowledge boundary** — observed for this unit's two files. Did not check whether other units' padded-table anchors
  survive `spotlessApply`; the repository's programmatic-edit gotcha already names the class.
