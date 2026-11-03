# generalize-process-defects-2026-11-01 process defects

### generalize-process-defects-2026-11-01#1 — A replacement appended its text instead of replacing the paragraph

- **Recorded**: 2026-11-01, by the implementing session, on the quick review's finding.
- **Stage**: review.
- **Expected**: a replacement replaces the whole unit it names; the edited paragraph is re-read against the old text's
  facts before the result is trusted, and a scripted replacement asserts the shape it expects.
- **Observed**: the scripted rewrite of four `Decision` paragraphs inserted each new text ahead of the old paragraph's
  tail, so four records stated their clause twice; the fused text passed `spotlessCheck`, `verifyCapturedMarkers` and
  `verifyTableRows`, and only the review's second round found it.
- **Evidence**: `docs/process-defects/probe-pause-resume-reuse.md`'s first `Decision` read "…ships in this unit's pull
  request., with the read-only `list` left allowed and the restriction scoped to the sandbox's own config." before the
  repair; the repair cut each block from its `- **Decision**` line and rewrote it, which the third round verified.
- **Consequence**: two further review rounds, no downstream artifact affected; the four paragraphs now read as single
  statements.
- **Knowledge boundary**: observed the fused paragraphs and the repair. Did not check whether another scripted
  replacement in the unit's history took the same shape.

- **Decision** — no-change: the span-edit rule already requires replacing the whole unit in one edit and re-reading it
  against the old text's facts; the scripted rewrite took the shape it warns about.
