# Decision record — the two mechanisms' baseline

The 2026-10-31 retrospective's recommended next direction: measure the two mechanisms — the commit guards and the claims
verification mode — before adding a third. This record is the baseline; it binds no standing rule.

## The question

What do the guards and the claims mode measure now, and what does the next window compare?

## The guards

- **Unticked boxes in an archived change** — 128 across 45 change dirs archived before `guard-archived-task-boxes`
  (#739, 2026-10-29); the dirs archived after it (2026-10-29…31) carry none. Command: the task lines matching the
  guard's `TASK_LINE_RE` (`scripts/commit-hygiene.py`); a naive `- [ ]` grep counts two prose examples in the guard's
  own change dir (`2026-10-29-guard-archived-task-boxes/tasks.md:19,27`).
- **Emptied ideas sections** — 0; `python3 scripts/commit-hygiene.py --ideas-sections` exits 0.
- **A foreign change directory** — the `--staged` guard refuses one (`find_foreign_change_paths`). The Workspace failure
  class recurs after #700 (`guard-archived-task-boxes#1`, `sharpen-readme-promises-and-routes#1`), but the work was
  never committed by the wrong unit, so the guard did not fire. A post-guard refusal is not observable: the guard keeps
  no log, so the count is the guard's tests, not a series.

## The claims mode

The window's 41 claims tables (the 55 change dirs archived 2026-10-24…31; 41 carry a `## Claims table`):

| Measure      | Value |
| ------------ | ----- |
| Checked rows | 6     |
| Skipped rows | 505   |
| Mismatches   | 0     |

The named skip reasons, from the mode's own output: **199** "the source must be one command", **183** two-column table,
**21** ambiguous pipe escape, **11** the result is not literal-only, **9** no explicit literal, **7** external (`gh`),
**7** working-tree `grep`, **3** working-tree `sed`, **2** `openspec`, **1** redirection. The remaining **62** rows are
**57 unsafe, 4 external (`opencode` 3, `python3` 1), and 1 working-tree `diff`** — the predicate refuses the unsafe,
defers the external, and reports the advisory. The six checked rows are the
`separate-lesson-capture-from-implementation` table's locator rows — the pilot's own table.

The S2 preference (an explicit-literal Result) targets the 20 rows skipped for a literal-format reason (11 not
literal-only, 9 no explicit literal). It does not touch the 382 rows skipped because the source is not one command or
the table is two-column; those need a table-shape change, not an authoring preference.

## The decision

**No third mechanism.** The next window compares:

- the unticked boxes after the guard (0 expected) and the empty ideas sections (0);
- the claims mode's checked fraction **by skip reason**, with the S2's target being the literal-format rows (20 of 511),
  not the whole 511;
- the guards' post-guard refusals, read from a process-defect record if one recurs, since the guard keeps no log.

## Reopening condition

Reopen this decision when any one of the following holds:

- **The S2-applicable rows show no improvement** — the reason-stratified comparison of the next window's checked
  fraction finds no movement among the literal-format rows, with the full-claim fidelity checked; investigate rather
  than concluding the table shape is the binding constraint (internal trigger: the next retrospective's measurement).
- **A post-guard unticked box appears** — investigate bypass versus coverage (internal trigger: an archived dir with an
  unticked task after #739).
- **A non-exempt foreign path is committed through bypass, or a guarded commit attempt does not refuse one** —
  investigate bypass versus a missed form; a merely staged foreign path or a merge-accounted one does not exercise the
  guard (internal trigger: a process-defect record naming one).
