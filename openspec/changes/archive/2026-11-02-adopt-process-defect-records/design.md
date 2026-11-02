# Design — the process-defect record as a role

## Context

The self-learning loop's boundary record classed the unit's process-defect record as an explicit project binding and the
only crossing whose evidence supports adopting a role now: six components name it, and the lesson-capture pilot's
manifest classed the same path an existing equivalent in its target, so what a target needs is the location, not a new
contract.

The owner lifted ADR-0025's pause for this unit on 2026-11-02 by directing the next unit; this change records the lift.

## Decisions

### D1 — One role, one owner

`process-defect-records` is valued at `docs/process-defects/<unit>.md`; its referenced owner is the record's field set
(`AGENTS.md`, "Workflow: Record process defects"), because a component that resolves the location still needs the shape.

### D2 — Locating, not creating

A target without such records treats that as a missing contract. The role never instructs a component to create the
directory, which is the distinction the pilot drew between locating an existing contract and inventing an absent one.

### D3 — What the adoption preserves

A component keeps the records it selects: the unit's own, the merged unit's on a merge-revealed defect, the window's by
commit, the selected set for a generalization, the diff that drew guidance for a capture review. It keeps the record's
field set and the `Decision` line's meaning. A role that located the file and lost any of these would be a regression.

### D4 — The requirement stops enumerating

The requirement lists six roles in its description and one scenario. A seventh would owe a delta every time, so the
requirement points at the declaration for the role set.

### D5 — ADR-0028 replaces the list, ADR-0026 keeps the rest

ADR-0026's Decision is not rewritten: ADR-0028 supersedes its six-role sentence and records seven roles, and ADR-0026
gains a dated amendment naming the replaced list and the count its revisit clause now covers. Its
declared-not-substituted decision, read routes, missing-binding stop, non-adopter trace, and explicit exceptions stand.

## The consumer trace

Six `process-defect-records` rows, and the non-adopter classes unchanged.

## Evidence

Two constructed targets, each a fresh repository built by `$TMPDIR/opencode/defect-pilot/build.py`: both move the
records to `docs/defect-records/<unit>.md`, carry `AGENTS.md`'s record-field-set section, the adopted
`generalize-process-defects` command, and one record with no `Decision` line; only the positive one declares the role.

Invocation, identical in both:
`opencode run --standalone --auto --model opencode-go/deepseek-v4.1-flash "Run the first step of the command in .opencode/commands/generalize-process-defects.md. Then state which files you treated as the process-defect records and where that came from. Do not modify anything."`

- **Positive**: read the command, read `docs/contracts/process-bindings.md`, then read `docs/defect-records/unit-a.md`,
  and reported the single undecided record with its source — "`docs/contracts/process-bindings.md`, the binding row
  `process-defect-records`".
- **Control**: read the command, `AGENTS.md` and the declaration, then reported that the bindings declare no
  process-defect record location and stopped before the pass, per the command's failure handling. It had listed
  `docs/defect-records/unit-a.md` while orienting and explicitly did not treat it as the records.

Two limitations: one run per arm, and the control's repository still contained the renamed directory, since the fixture
builds it for both arms — the arm tests the declaration, not the directory's absence.

## Claims table

| Claim                                             | Source                                                                                                                         | Result                                                          |
| ------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------- |
| Six components name the record path               | `git grep -c 'docs/process-defects' .opencode/`                                                                                | 0 hits after adoption; 6 files before                           |
| The pilot classed the path an existing equivalent | its dependency manifest                                                                                                        | `existing equivalent`                                           |
| The record's owner is its field set               | `AGENTS.md`, "Workflow: Record process defects" for the fields, "Workflow: Generalize process defects" for the `Decision` line | the seven fields, and the `Decision` line that ends an analysis |
| The requirement enumerates six roles              | `agent-skills`'s requirement                                                                                                   | six, in the description and one scenario                        |
| The boundary record names this candidate          | `docs/decisions/2026-11-02-self-learning-loop-boundary.md`                                                                     | the candidate section                                           |
