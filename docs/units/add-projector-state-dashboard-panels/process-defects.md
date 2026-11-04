# Process defects — add-projector-state-dashboard-panels

### add-projector-state-dashboard-panels#1 — A range replacement between two task anchors deleted the section heading it spanned

- **Recorded** — 2026-11-04, by the main agent.
- **Stage** — proposal.
- **Expected** — replacing task 2.3's text should rewrite one task, leaving `tasks.md`'s section structure intact.
- **Observed** — the replacement ran from the `2.3` anchor to the next `3.1` anchor, and the
  `## 3. The producer-side name pin` heading sat between them, so it was removed with the replaced span.
- **Evidence** — `openspec validate add-projector-state-dashboard-panels --strict` reported "Task \"3.1\" is under group
  2, but its leading number points to group 3" and the same for 3.2; the heading was absent from the region printed
  around sections 2–4.
- **Consequence** — the heading was restored in the next edit and validation passes; nothing else was lost, and the
  defect never reached a review round.
- **Knowledge boundary** — observed: the validation warning and the missing heading; inferred: none; not checked:
  whether the same anchor pattern appears in this unit's other artifacts.

### add-projector-state-dashboard-panels#2 — A control loop restored a mutated file with `git checkout --`, discarding the uncommitted implementation

- **Recorded** — 2026-11-04, by the main agent.
- **Stage** — implementation.
- **Expected** — each rule control mutates `DashboardJsonRules.kt`, runs its tests, and restores the file so the next
  control starts from the implementation.
- **Observed** — the restore ran `git checkout -- build-logic/src/main/kotlin/DashboardJsonRules.kt`, which restores the
  tracked file's index copy; the file had no staged version of the implementation, so every later control and the final
  run measured the pre-change rules.
- **Evidence** — the three controls all printed the same failing-test count (8), where each should differ, and the
  follow-up check printed `restored rules present: False` while `git status` showed the file clean.
- **Consequence** — the implementation was re-authored from the text written in this session, then the controls re-ran
  against a copy under `$TMPDIR/opencode` with a `diff` proving the restore, and the dashboard task-level control took
  its own copy before mutating; no other file was touched by the loop.
- **Knowledge boundary** — observed: the checkout, the identical counts and the missing rules; not checked: whether a
  file outside `build-logic` lost an edit in the same loop.
