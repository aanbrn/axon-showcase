# Process defects — split-docs-refresh-conventions

### split-docs-refresh-conventions#1 — a batch edit anchored on a pre-formatter read aborted

- **Recorded**: 2026-11-01, by the unit's writer.
- **Stage**: implementation, while reconciling `design.md` after review round 1.
- **Expected**: take an edit's anchor from a read taken after `spotlessApply` last rewrote the file, so the batch
  replacement of `design.md`'s Context-through-Claims block matches.
- **Observed**: the anchor came from a read taken before `spotlessApply` re-wrapped that file, so the batch edit found
  no match and aborted, and the reflowed region had to be re-read before the edits were re-applied one at a time.
- **Evidence**: the edit failed with `Could not find oldString in .../split-docs-refresh-conventions/design.md` and
  reported "The closest lines in the file: 3: `## Context`".
- **Consequence**: one failed edit and one extra read; no content was lost, since the batch edit aborted before writing
  anything.
- **Knowledge boundary**: observed the failed match and the re-read; the cause is assumed to be the formatter's reflow,
  since the anchor text was copied from a read taken before that run, and no other writer touched the file.

### split-docs-refresh-conventions#2 — a bash-only exit-status idiom ran under zsh and reported nothing

- **Recorded**: 2026-11-01, by the unit's writer.
- **Stage**: verification, running the gate tasks.
- **Expected**: a verification command's exit status reaches the report, so a green result is derived rather than
  assumed.
- **Observed**: the command piped Gradle's output through `tail` and read `${PIPESTATUS[0]}`, which zsh does not set, so
  the line printed an empty status and the task was re-run with the exit code captured directly.
- **Evidence**: the run printed `VERIFY_EXIT:` with no value after the command completed.
- **Consequence**: one redundant Gradle invocation; the re-run performed the check the first one was meant to report.
- **Knowledge boundary**: observed the empty value and the re-run; the shell is zsh on this host, and no other shell was
  tried.

### split-docs-refresh-conventions#3 — a measured figure was written before the text stopped changing

- **Recorded**: 2026-11-01, by the unit's writer.
- **Stage**: implementation, while the split moved from family granularity to per-obligation granularity.
- **Expected**: derive a measured figure from the text once that text is final, so the artifact records the value the
  shipped file produces.
- **Observed**: the bullet's growth figure was written into `design.md` between edits of `AGENTS.md`, so each later
  wording fix changed it and the figure was re-derived and rewritten three times in the same session: 115, then 84, then
  88 non-whitespace characters.
- **Evidence**: the three successive figures and their measured bases — 6,418, 6,387 and 6,391 non-whitespace characters
  against the base bullet's 6,303 — each replacing the previous one in `design.md`.
- **Consequence**: two redundant measurement runs and two extra edits to a file already staged for review; no figure was
  ever shipped wrong, since each rewrite preceded the next review round.
- **Knowledge boundary**: observed the three figures and their re-measurements; nothing was measured for a version of
  the text that was never written to disk.

### split-docs-refresh-conventions#4 — an enumeration of permitted edits under-matched the diff in three rounds

- **Recorded**: 2026-11-01, by the unit's writer.
- **Stage**: review rounds 2, 4 and 5, all against `design.md`'s decision 3.
- **Expected**: state the edit scope once, so a later restructure of the same block either falls inside it or is visible
  as a change of scope.
- **Observed**: decision 3 listed the kinds of edit the splice permits, and each round reported another kind the list
  omitted — the sentence's voice, then a dash becoming a colon or parentheses — so each fix appended the instance the
  round named and the next round found the next one.
- **Evidence**: the three rounds' findings against decision 3 (round 2 `wording`, round 4 `wording`, round 5 `wording`),
  and round 4's statement that the enumeration itself was the shared cause.
- **Consequence**: three rounds of the quick loop spent on one decision's wording; the fix that converged restates the
  scope as a category — the splice stays at the boundaries — rather than as a list of instance kinds.
- **Knowledge boundary**: observed the three findings and their fixes; whether a fourth kind remained outside the
  category statement was judged only by round 6's clean report.
