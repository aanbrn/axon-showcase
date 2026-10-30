# Process defects — trim-readme-workflow-density

### trim-readme-workflow-density#1 — A table row written as two lines became a second row

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — a markdown table row is one source line. `AGENTS.md`'s `Programmatic edits` gotcha states the table
granularity: a row appended below a blank line, or under no delimiter row, is a paragraph the formatter reflows and
passes, so the shape has to be asserted rather than checked afterwards.

**Observed** — the two replacement rows for the agent table were written as two source lines each, to keep every line
within 120 characters. The rendered table gained a second row per agent (the continuation fragment) instead of one row
wrapping, and `spotlessCheck` passed on that state: Prettier leaves a short row unpadded, so the file the gate read was
still short.

**Evidence** — the diff showed the header padded to a wider column and the fragments standing as their own rows, e.g.
`| always-loaded instruction budget — /audit-agents |`; after collapsing each pair into one line and re-running
`spotlessApply`, the table listed the eleven agents once each and `verifyTableRows` reported 165 files clean. The cell
count check was run only after the repair, so it did not see the broken state; a reproduction of the recorded shape
against it reports `A markdown table row has 1 cell where its delimiter row has 2` and exits 1.

**Consequence** — one formatter run and one repair; nothing else in the README moved. `spotlessCheck` does not read a
row's cell count, so it passed the broken table; `verifyTableRows` does, and would have failed this shape had it run
before the repair.

**Knowledge boundary** — observed: the two-line rows, the rendered table, the repair, and both gate results. Not
checked: whether another table in this repository carries an extra well-formed row, which a check comparing each table's
row count against its own content would find.

- **Decision** — no-change: `verifyTableRows` now checks every gated table's cell count, which `spotlessCheck` does not.

### trim-readme-workflow-density#2 — A replacement span took the neighbouring idea with it

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — an edit replaces the unit it means to change and nothing beside it; `AGENTS.md`'s `Programmatic edits`
gotcha states that a range drawn to the next heading crosses the list boundary and takes its siblings.

**Observed** — the `docs/ideas.md` rewrite was scripted as "from the density entry to the `## 2026-10-28` heading",
which also spans the **first-run resource passport** entry directly below it. That entry was unrelated and is not
implemented or decided against, so the edit removed a live parked idea.

**Evidence** — after the edit the file held 102 idea bullets against 103 at `HEAD`, and `grep -n passport docs/ideas.md`
returned nothing; restoring the entry by hand brought the count back to 103.

**Consequence** — the idea was absent for one review round and is restored unchanged. Nothing else in the file moved.

**Knowledge boundary** — observed: the written range, the bullet counts before and after, and the restore. Not checked:
whether another script the unit ran draws a range to a heading, which a read of the scripts' end-boundaries would show.

- **Decision** — no-change: the programmatic-edit rule already requires a replacement's span bounded by its own unit,
  since a range drawn to the next heading takes the sibling.
