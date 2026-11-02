# Design — the remaining guidance roles

## Context

The inventory's last two candidates are rules their components apply: `/generalize-process-defects` decides each
record's disposition against the recurrence-dispositions bullet in the generalization workflow's Decision points, and
`/apply-audit-reductions` routes an extraction through the extraction procedure. Both are `AGENTS.md` locations a target
would rename; both have one consumer.

The owner lifted ADR-0025's pause on 2026-11-03 by directing that the remaining candidates be taken.

## Decisions

### D1 — Two roles, each valued as this repository states it

`generalization-dispositions` names the "Where the repository already records a recurrence decides its disposition"
bullet in "Workflow: Generalize process defects" → "Decision points", owned by the recurrence dispositions.
`extraction-procedure` names the "Workflow: Extract an `AGENTS.md` section" heading, owned by that procedure. Both
satisfy ADR-0026's role-not-string rule.

### D2 — The stop belongs at each adopting site

Each command now says a declaration that names none stops its own operation — the pass before the hand-off, and the
route of the move — rather than being inferred.

### D3 — No delta

The requirement delegates the role set to the declaration.

## Evidence

Both arms are fresh repositories built by `$TMPDIR/opencode/guidancepilot/build.py`, whose inputs the record states:

- the fixture's `AGENTS.md` holds two sections — `## Deciding a record's fate` →
  `### Where the record already is decides` (three disposition routes, stated there as a `###` subheading) and
  `## Moving a section` (route a move through a change that owns the destination);
- the declaration is a three-row stub — the two role rows and a review-ledger row — with the two roles pointed at the
  fixture's anchors, and differs between the arms by those two rows only;
- both copy the two adopted commands verbatim.

Neither fixture contains the repository's own wording for either anchor. Tool: `opencode` CLI `2.2.5`, model
`opencode-go/deepseek-v4.1-flash`; invocation identical in both arms:
`opencode run --standalone --auto --model opencode-go/deepseek-v4.1-flash "Do two things and report each in one line: (a) say which disposition sites step 3 of .opencode/commands/generalize-process-defects.md makes you use and where they come from; (b) say which procedure the extraction bullet in .opencode/commands/apply-audit-reductions.md sends a move through and where that comes from. Do not modify anything."`

- **Positive**: read both commands, the declaration and `AGENTS.md`, then reported both anchors with their two-hop
  sources.
- **Control**: read the three files, then reported that the declaration carries neither bullet and that both commands
  hit their stop rules — "the selection stops with no sites inferred", and the extraction likewise.

Limitation: one run per arm; the two questions were asked together, so a failure in one does not isolate which role
failed.

## Claims table

| Claim                                    | Source                                                                                            | Result                                                                                 |
| ---------------------------------------- | ------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| Two components named their sections      | `git show bea7c172:.opencode/commands/<cmd>.md` searched for the anchor's words, wrapping ignored | one each at that revision; 0 in the tree                                               |
| Each value names this repository's shape | `AGENTS.md`                                                                                       | a bullet with its Decision-points section, and a heading                               |
| No section row remains unresolved        | the inventory's table                                                                             | four settled, two entry pointers                                                       |
| A missing row stops each operation       | the control run                                                                                   | both stop rules reported                                                               |
| A renamed anchor resolves                | the positive run                                                                                  | both anchors and their two-hop sources reported                                        |
| No delta is owed                         | `openspec/specs/showcase/quality/agent-skills/spec.md`                                            | adding a role changes the declaration and the adopting components, not the requirement |
| The clauses name the operations          | the two commands                                                                                  | "stops the pass before the hand-off" and "stops the extraction"                        |

**Dismissals from `scripts/claim-scan.py --base bea7c172`.** One reason per kind:

- `[absolute]` — the claims rows carry the unit's own negatives; the rest are the components' and records' rule text.
- `[count]` — a figure, a revision or a task number that a row or a record supplies.
- `[path]` — an existing path, a fixture path named as outside the repository, or an invocation the evidence records.
- `[cross-ref]` — a section title, an ADR, the declaration or a record heading; each resolves.
