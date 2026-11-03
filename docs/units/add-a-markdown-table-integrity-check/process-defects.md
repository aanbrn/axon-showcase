# Process defects — add-a-markdown-table-integrity-check

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### add-a-markdown-table-integrity-check#1 — The design's claims table carried the defect the check exists for

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the implementation.
- **Stage** — implementation (the first run of the new mode over the gated corpus).
- **Expected** — an artifact written for a table-integrity change carries tables whose rows agree with their delimiter
  rows.
- **Observed** — the new mode's first corpus run reported three claims-table rows in the change's own `design.md`
  carrying 9, 8 and 7 cells against a 4-cell delimiter, because their reproduced cells held table examples inside
  backticks and Prettier had left the raw pipes in place.
- **Evidence** — the mode's three stderr lines naming `design.md:76`, `:77` and `:78`; the rows rewritten to describe
  the probe's output without pipes, after which a run over a hand-built list printed
  `Checked 128 markdown file(s) for table row integrity.` and exited 0 — that list used git's pathspec semantics, which
  read fewer files than the build's target; the Gradle task over the formatter's own set printed
  `Checked 166 markdown file(s)…` at the unit's end.
- **Consequence** — three table rows rewritten before the commit; the defect they had is now a control fixture, since
  the probe's outputs (`| wrapped |` and `| one | a | b |`) are the known-bad cases.
- **Knowledge boundary** — observed this file. The hand-built list read the tracked set only, so the untracked files of
  that moment were absent from it; did not check whether other units' claims tables carry raw pipes inside cells.

- **Decision** — no-change: the new `verifyTableRows` mode found the design's own rows, and they are now its control
  fixture; the mechanism enforces it.

### add-a-markdown-table-integrity-check#2 — The Gradle task captured script objects into the configuration cache

- **Recorded** — 2026-10-29, by the writing session, during the implementation.
- **Stage** — implementation (the first `verifyTableRows` run).
- **Expected** — a registered `Exec` task stores a configuration-cache entry, so `check` runs without discarding it.
- **Observed** — the task set its `commandLine` inside a `doFirst` that closed over script-level values, and the build
  failed with "cannot serialize Gradle script object references as these are not supported with the configuration
  cache".
- **Evidence** — the failure's "1 problem was found storing the configuration cache" and the task named
  `:verifyTableRows`; the corrected task, which resolves the path list at configuration time and passes only strings,
  after which the run printed "Configuration cache entry stored."
- **Consequence** — one failed task run and one rewrite of the task body; nothing shipped with the unsafe shape.
- **Knowledge boundary** — observed this task. Did not check whether other `Exec` tasks in the build use `doFirst` with
  captured values; the ones inspected set `commandLine` at configuration time, which itself turned out to be
  insufficient once the configuration cache was reused (entry #4).

- **Decision** — no-change: the `gradle-build` pack's configuration-cache bullet already records the script-object
  capture; this record is another covered occurrence.

### add-a-markdown-table-integrity-check#3 — A range edit left the replaced block's tail in place

- **Recorded** — 2026-10-29, by the writing session, during the implementation.
- **Stage** — implementation (the tasks file's control list).
- **Expected** — replacing a task list's items replaces the whole list.
- **Observed** — the edit replaced the first item and its continuation lines, then inserted the new items, but the old
  remaining items stayed below them, so the file carried two `3.2`–`3.5` sets; the tick pass then failed on a duplicate
  identifier and the residue showed in the numbering.
- **Evidence** — `grep -n "3\\.[1-6]" tasks.md` printing `3.2` at lines 26 and 37 with the stale block after the new
  `3.6`; the stale block removed, after which every identifier appeared once and the ticks applied.
- **Consequence** — one failed tick pass and one deletion; the tasks file now carries one item per identifier.
- **Knowledge boundary** — observed this edit. Did not establish whether other range edits in this session left similar
  residue; the artefacts they touched were read back at the time.

- **Decision** — no-change: the programmatic-edit rule's range-boundary obligation already requires the replaced block's
  end derived from its own structure.

### add-a-markdown-table-integrity-check#4 — A warm configuration cache left the task scanning a stale file set

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the review gate.
- **Stage** — review (the fifth quick round's limitation, verified by probe).
- **Expected** — a check that scans a file tree scans the tree as it stands when it runs, so a file added after a
  previous run is judged.
- **Observed** — the task resolved its path list at configuration time, so a warm configuration cache replayed the
  earlier list; with the cache warm, adding `docs/table-probe.md` carrying a three-cell row against a two-cell delimiter
  left `verifyTableRows` green.
- **Evidence** — the probe file's content and the green run before the fix; the task rewritten as a custom
  `VerifyTableRowsTask` resolving the tree in its action, after which the same warm-cache probe failed with
  `A markdown table row has 3 cells where its delimiter row has 2: docs/table-probe.md:3`, and removing the file
  returned `Checked 166 markdown file(s)…`.
- **Consequence** — the task was rewritten once; the probe file was removed and the tree is clean.
- **Knowledge boundary** — observed this task. Did not check whether other build tasks resolve a file list at
  configuration time and could miss a file added after a cached run.

- **Decision** — proposed: applied as the scanned-tree clause in the `gradle-build` pack's configuration-cache bullet.
