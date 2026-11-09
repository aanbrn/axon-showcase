# Process defects — apply-2026-11-07-audit-findings

### apply-2026-11-07-audit-findings#1 — A generated delta block lost the paragraph break

- **Recorded** — 2026-11-09, by the main agent.
- **Stage** — implementation.
- **Expected** — deleting the orphaned sentence from the copied requirement block leaves the two surviving paragraphs
  separated by a blank line.
- **Observed** — the first generated delta consumed the blank line before the sentence as well, merging the two
  paragraphs, and the formatter kept the merge.
- **Evidence** — the diff of the generated block against the main spec showed one merged paragraph where the source has
  two; the regenerated block asserts `"rather than the staged set.\n\nWhen the caller also names"` before writing.
- **Consequence** — one regeneration of the delta; nothing was committed and no other artifact was touched.
- **Knowledge boundary** — observed the merged block and its correction; did not check whether a reader would have read
  the merged paragraph as carrying the same obligation.
