# Process defects — document-air-worktree-safety

### document-air-worktree-safety#1 — a scripted replace no-opped on reflowed prose

- **Recorded**: 2026-11-06, by the implementing agent.
- **Stage**: implementation.
- **Expected**: adding the run-2 section to `air-exercise.md` by replacing its last sentence.
- **Observed**: the anchor had been reflowed by Prettier, `str.replace` returned the input unchanged, and the script
  wrote the file back without the section.
- **Evidence**: a later append asserted its own tail and failed; the missing section was then added and verified.
- **Consequence**: one turn lost; no damage beyond the rework.
- **Knowledge boundary**: observed that the replace no-opped; did not re-measure whether every other scripted edit in
  the unit had matched.

### document-air-worktree-safety#2 — the exercise record sat at a path the unit layout does not admit

- **Recorded**: 2026-11-06, by the implementing agent.
- **Stage**: review.
- **Expected**: a record the unit's directory admits, or one placed outside it.
- **Observed**: `docs/units/document-air-worktree-safety/air-exercise.md` failed `--unit-layout`, and the root `check`
  failed on `:verifyUnitLayout`; the change's `skip_specs` sentence read as if no such kind were added.
- **Evidence**: `verifyUnitLayout` non-zero on the first `check` run; `--unit-layout` exit 1.
- **Consequence**: one review round; the record moved to `docs/decisions/2026-11-06-air-worktree-exercise.md` and every
  reference was repointed.
- **Knowledge boundary**: observed the gate's refusal and the move; did not check whether another admitted kind would
  have been the better home.

### document-air-worktree-safety#3 — a hand-over edit wrote a literal `\n`

- **Recorded**: 2026-11-06, by the implementing agent.
- **Stage**: review.
- **Expected**: a blank line before the appended round section.
- **Observed**: an escaped newline inside a quoted heredoc produced the two characters `\` and `n` at the end of the
  previous round's last line; it happened twice.
- **Evidence**: `grep -n '\\n' review-handover.md` listed the line; the literal was removed and the grep returned
  nothing.
- **Consequence**: two small repairs; no gate reads that file for shape.
- **Knowledge boundary**: observed the literal and its removal; did not inspect other files written the same way.

### document-air-worktree-safety#4 — a findings instance file was wrapped, breaking its one-line shape

- **Recorded**: 2026-11-06, by the implementing agent.
- **Stage**: review.
- **Expected**: one line per finding instance, as `commit-hygiene.py --finding-instances` requires.
- **Observed**: a long instance line was reflowed by `spotlessApply` into a continuation line, and the guard refused the
  staged file.
- **Evidence**: `round 5 carries 1 line(s) outside the instance shape`; rewriting each instance under 120 characters,
  one line each, passed.
- **Consequence**: one rework; the class list and count in `rounds.tsv` were unaffected.
- **Knowledge boundary**: observed the wrap and the fix; did not enumerate every other findings file for the same shape.
