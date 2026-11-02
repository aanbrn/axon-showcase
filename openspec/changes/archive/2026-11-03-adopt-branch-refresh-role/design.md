# Design — the branch-refresh role

## Context

The dependency inventory left six `AGENTS.md` sections undeclared. `adopt-spec-delta-role` settled one. Reading the
other five's uses separates them into two classes:

- **Entry pointers.** The ship-change skill's two references are its own `Source:` lines — the section that governs the
  component, which the declaration's consumer-trace sentence keeps explicit rather than binding.
- **Rules a component applies.** `/sync-branch` follows the branch-refresh route the rule gives the state it finds;
  `/generalize-process-defects` decides each record's disposition against the generalization workflow's Decision points;
  `/apply-audit-reductions` routes an extraction through that procedure.

This unit takes the branch-refresh rule. The other two remain candidate rows, and the two `Source:` lines are recorded
as entry pointers, which corrects the inventory's count of its own dependencies.

The owner lifted ADR-0025's pause on 2026-11-03 by directing that the remaining rows be taken.

## Decisions

### D1 — One role, valued as this repository states it

`branch-refresh` names the "Refreshing a branch" **bullet** under "Workflow: Change lifecycle" → "Failure handling",
owned by that rule. ADR-0026's role-not-string rule is satisfied by binding the bullet and its section, not a
heading-shaped invention.

### D2 — The stop belongs at the adopting site

`/sync-branch` step 7 now says "a declaration that names none stops the refresh rather than being inferred" — the same
imperative shape the other adopters carry, with the operation named.

### D3 — No delta

The requirement delegates the role set to the declaration.

## Evidence

Both arms are fresh repositories built by `$TMPDIR/opencode/branchpilot/build.py`, whose inputs the record states so the
experiment reconstructs without it:

- the fixture's `AGENTS.md` holds one section, `## How to bring a branch up to date`, with a route for each state;
- the declaration is this repository's re-pointed at that section, and differs between the arms by the `branch-refresh`
  row only;
- both copy the adopted `.opencode/commands/sync-branch.md` verbatim and carry a clean branch with no commits of its
  own.

Neither contains "Refreshing a branch", so a literal lookup cannot succeed. The fixture states the rule as a heading
while this repository states it as a bullet; the experiment therefore exercises **resolution through the declaration**,
which is the role's behaviour, and not the bullet's own shape — a reader should not read the arms as a test of the
bullet.

Tool: `opencode` CLI `2.2.5`, model `opencode-go/deepseek-v4.1-flash`; invocation identical in both:
`opencode run --standalone --auto --model opencode-go/deepseek-v4.1-flash "Run step 7 of the command in .opencode/commands/sync-branch.md for this branch, and state which rule and heading you followed and where that came from. Do not modify anything."`

- **Positive**: read the command and the declaration, inspected the branch state, then reported the `branch-refresh`
  rule, its section, and the two-hop source.
- **Control**: read the command and the declaration, inspected the state, then reported "refresh stopped, nothing
  modified" and quoted the stop clause.

Limitation: one run per arm, and the fixture has a single heading, so the control's stop is the load-bearing outcome.

## Claims table

| Claim                                            | Source                                                                     | Result                                                                            |
| ------------------------------------------------ | -------------------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| One component named the section                  | `git grep -c 'Refreshing a branch' ee7bdc58 -- .opencode/`                 | 1 at that revision; 0 in the tree                                                 |
| The value names this repository's shape          | `AGENTS.md`                                                                | the bullet under "Workflow: Change lifecycle" → "Failure handling", not a heading |
| Two rows are entry pointers, two stay candidates | their uses: two `Source:` lines; the Decision points; the extraction route | so the inventory's split                                                          |
| The entry-pointer rule's home                    | the declaration's consumer-trace sentence                                  | a component's own entry pointer stays explicit                                    |
| A missing row stops the refresh                  | the control run                                                            | reported the stop and modified nothing                                            |
| A renamed value resolves                         | the positive run                                                           | the rule, its section and the two-hop source reported                             |
| The clause names the operation                   | `/sync-branch` step 7                                                      | "stops the refresh rather than being inferred"                                    |

**Dismissals from `scripts/claim-scan.py --base ee7bdc58`.** One reason per kind, stated as the kind's ground:

- `[absolute]` — the claims rows carry the unit's own negative statements; the rest are the components' and records'
  rule text.
- `[count]` — the kind's content is a figure, a revision or a task number that a row or a record supplies.
- `[path]` — the kind's content is a path or an invocation; each is existing, a fixture path named as outside the
  repository, or an invocation the evidence records.
- `[cross-ref]` — the kind's content is a section title, an ADR, the declaration or a record heading; each resolves.
