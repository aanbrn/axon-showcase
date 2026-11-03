# Process defects — relocate-dependency-updates-section

### relocate-dependency-updates-section#1 — A section move was scripted rather than made with the edit tool

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — a single known unit's change — a reorder, deletion, or insertion — is made with the edit tool on that
unit's own text, not a script (`AGENTS.md`'s `Programmatic edits` gotcha states it, because a scripted move can take a
sibling block or lose structure).

**Observed** — the `### Dependency Updates and Security` section was cut and reinserted by a Python script that ranged
between heading boundaries. The script's own assertions covered the block's first and last lines, and the checks the
gotcha prescribes afterwards were run: the moved block and the remaining file each compare byte-identical against
`HEAD`.

**Evidence** — the run reported `moved 34 lines`; the follow-up comparison printed `block identical: True`,
`remainder identical: True`, and `total lines: 1156 -> 1156`; the heading walk showed the ten `##` sections unchanged
with the section in its new place.

**Consequence** — none: the move is byte-identical and the surrounding file is untouched. The deviation is the method,
not the result.

**Knowledge boundary** — observed: the script, its assertions, and the three post-hoc checks. Not checked: whether the
same verification would have caught a mistake in a section whose boundaries were less clean than this one's (it moves
between two `###` headings with no nested headings inside).

- **Decision** — no-change: the programmatic-edit rule already says a single known unit's move is made with the edit
  tool; the scripted move was a method deviation with no artifact damage.
