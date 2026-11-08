# Process defects — bump-tooling-pins-2026-11-09

### bump-tooling-pins-2026-11-09#1 — the change-dir derivation kept a task its successor retired

- **Recorded** — 2026-11-09, by the implementing session.
- **Stage** — proposal.
- **Expected** — the recurring-unit-type rule has a bump's change dir derived from the last analogous archived change as
  a floor and reconciled with what the current rules mandate, dropping an inherited task a current rule or a later
  change has superseded.
- **Observed** — task 3.4 ran the `lesson-capture` subagent inline over the unit's process-defect record, copied from
  the `2026-10-25-bump-tooling-pins-2026-10-25` analogue; the newer `2026-11-01-bump-openspec-cli-2026-11-01` change had
  already recorded that step as obsolete. The owner's advisor consultation raised it.
- **Evidence** — `openspec/changes/archive/2026-11-01-bump-openspec-cli-2026-11-01/tasks.md` task 4.4's `**Done**` note:
  "Running it at all was an obsolete step this task inherited from the archived analogue".
- **Consequence** — one obsolete task sat in the plan and would have spent a subagent invocation inside an unfinished
  unit. Repaired: the task is removed, the group renumbered, and the retired-step note added in its place.
- **Knowledge boundary** — I read the 2026-10-25 analogue and the rule, not the newer 2026-11-01 bump, before the
  advisor's report; I read the 2026-11-01 archive during the repair, and its task 4.4 note is the Evidence above. I have
  not checked whether the other bump analogues carry the same inherited step.

### bump-tooling-pins-2026-11-09#2 — the plan edited a dispatchable workflow without scheduling its dispatch

- **Recorded** — 2026-11-09, by the implementing session.
- **Stage** — proposal.
- **Expected** — editing a workflow with a `workflow_dispatch` trigger is verified by a dispatch from the change's own
  pushed branch, so the plan carries that obligation.
- **Observed** — the plan carried a dispatch only for `dependency-security.yml`; `.github/workflows/audit.yml` also has
  its pin edited and declares `workflow_dispatch`, and no task dispatched it. The owner's advisor consultation raised
  it.
- **Evidence** — `.github/workflows/audit.yml`'s `on:` block lists `workflow_dispatch`, and its line 33 carries the pin.
- **Consequence** — the audit workflow's edited body would have gone unexercised before the merge. Repaired: delivery
  task 4.2 dispatches it and records the run's conclusion.
- **Knowledge boundary** — whether the earlier bump analogues dispatched this workflow was not checked; the rule's
  requirement is read from `AGENTS.md`, and the dispatch itself still runs during delivery.

### bump-tooling-pins-2026-11-09#3 — a scaffold README entered the change directory

- **Recorded** — 2026-11-09, by the implementing session, after the owner asked why the change carried a `README.md`.
- **Stage** — proposal.
- **Expected** — a change directory holds the artifacts the schema defines (its `.openspec.yaml`, `proposal.md`,
  `tasks.md`, and `design.md`/`specs/` where owed), matching the shape the repository's changes use.
- **Observed** — `openspec new change` was invoked with `--description`, which writes a `README.md` beside the
  artifacts; the file was staged with the rest and screened by every quick round.
- **Evidence** — `openspec new change --help` documents `--description` as "Description to add to README.md"; ten of the
  483 archived changes carry a `README.md`, all dated 2026-08-11 to 2026-08-14.
- **Consequence** — one scaffold file sat in the change directory and the index. Repaired: the file is removed and the
  remaining contents match the archived changes' shape.
- **Knowledge boundary** — I did not check whether `openspec archive` or any gate treats a change `README.md` specially;
  nothing in `openspec/specs/` or the build's verification tasks names it.

### bump-tooling-pins-2026-11-09#4 — the change's tasks.md copied the unit record's workspace fields

- **Recorded** — 2026-11-09, by the implementing session.
- **Stage** — implementation.
- **Expected** — a change's `tasks.md` references the unit record and carries no copy of its fields.
- **Observed** — `tasks.md` opened with a `Workspace:` line repeating the unit, path, branch and writer, copied from the
  archived analogue. `:verifyWorkspaceRecords` failed twice: first for no record reference, then for the copied fields.
- **Evidence** — the check's messages: "no reference to the unit record:
  docs/units/bump-tooling-pins-2026-11-09/unit.md" and "the record's fields are copied here as well as referenced".
- **Consequence** — two failed `check` runs before the file was corrected to a single record reference. Repaired.
- **Knowledge boundary** — I did not check whether the older archived analogues still carry the copied line; the rule is
  read from `AGENTS.md` and the check.
