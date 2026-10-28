# Design

## Context

The obligation this change makes explicit already exists, in exactly one place. `AGENTS.md`'s "Workflow: Generalize
process defects" carries a **Decision points** bullet, "Where the repository already records a recurrence decides its
disposition", which sends a record's incident to one of three sites: a `captured:` rule, a `docs/ideas.md` entry whose
own `Revisit when:` condition the incident meets, or an already-parked problem. The bullet also carries the reason it
exists — no other reader tests a parked entry's condition.

The obligation is caller-side by design, not by accident. `2026-10-27-separate-lesson-capture-from-implementation` split
the inline capture into recording plus a separately-run generalization, and its task 1.11 required all three recurrence
branches to be carried into the new generalization section. The `lesson-capture` subagent's inputs and destinations —
which the `agent-skills` requirement enumerates — omit `docs/ideas.md` entirely: the subagent applies a promotion gate
to a candidate rule, while the disposition of a record against the parked corpus belongs to the unit that owns the
`Decision` lines.

What the split left implicit is the command. `/generalize-process-defects` runs the pass, and its procedure selects the
records, hands them to the subagent, presents the proposals, and appends a `Decision` per record — with no step that
performs the match. `AGENTS.md`'s own Procedure names the command as the pass's entry point (its step 1 runs
`/generalize-process-defects`) and then summarizes the hand-off, apply, and decision steps, so the summary carries no
match either, and nothing in the command's procedure asks for one. A pass that follows either site therefore decides
every record without testing a parked entry's condition, and the subagent cannot notice: `docs/ideas.md` is in neither
its inputs nor its destinations.

The obligation has been executed — `extend-owner-reply-language-scope`, record #2's `Decision`, routes its incident to
the parked table-integrity check — but by the calling session, not by any step the command states.

**Premise interrogation.** `docs/ideas.md`'s 2026-10-27 entry "Read a parked idea's own `Revisit when:` condition" is
_not_ this change: it parks a periodic sweep by the `architecture-auditor` (a sibling branch classifying an entry's own
recorded condition), and it states that the event-time half is the capture's. This change makes that event-time half an
explicit command step; the periodic sweep stays parked. No ADR names the generalization, `lesson-capture`, or the
process-defect corpus. The spec corpus names the command once, for selection and hand-off, and delegates routing to
`AGENTS.md`.

## Goals / Non-Goals

**Goals**

- Make the caller-side match an explicit step of the command that runs the pass.
- Carry each record's recurrence disposition into the presentation and the apply decision.
- Keep the routing conditions single-sourced in `AGENTS.md`.

**Non-Goals**

- Moving the match into `lesson-capture`: that widens a spec'd input and destination set, and it would owe a `MODIFIED`
  delta for behavior the caller can perform.
- The periodic sweep of parked entries: a different trigger, parked separately.
- Reopening the caller/subagent split itself.
- Trimming `AGENTS.md`'s Procedure summary of the hand-off, apply, and decision steps the command also carries: that
  overlap predates this change, and consolidating it is a unit of its own.

## Decisions

**1. Keep the match caller-side and state it as a command step.** The alternatives were to leave it implicit — rejected,
because a pass can complete end to end without it and nothing reports the omission — and to push it into
`lesson-capture` — rejected, because the subagent's contract is the promotion gate over a candidate rule, its inputs are
enumerated by a spec requirement, and `docs/ideas.md` is deliberately not among them.

**2. Point at the `AGENTS.md` bullet rather than restating its conditions.** The command names the bullet and states
that it owns the sites, their conditions, and their actions. A copy would be the drift surface the single-sourcing
invariant warns about, and `AGENTS.md` is where the spec already places the routing.

**3. Withhold an already-parked proposal at the apply step.** The subagent does not see `docs/ideas.md`, so it can
propose a rule for a problem already parked; the apply step and its Decision point compose with the disposition the
match produced, so that proposal is not applied.

**4. Owe no spec delta (`skip_specs: true`).** The requirement's single clause about the command describes selection and
hand-off, both of which the command still performs, and the same requirement states that routing is `AGENTS.md`'s. The
command gains no behavioral contract of its own.

**5. Point at the match from `AGENTS.md`'s Procedure too.** Root's Procedure enumerates the hand-off step, so a reader
can work from it without running the command; a one-line pointer there names the act and its owner — the **Decision
points** below — and copies neither the sites nor their conditions. That is the single-sourcing split's pointer form:
the command's step owns the match, and a procedure step is pointed at rather than restated. Root's Procedure also
summarizes the hand-off, apply, and decision steps the command carries; trimming that overlap is a separate
consolidation, which stays a Non-Goal.

**6. Owe no `README.md` or ADR edit.** The slash-command row's claim — that the pass turns records into rules and
improvements and records a decision per record — stays true, and the added step is internal to the command's procedure.
No ADR covers the generalization, `lesson-capture`, or the defect corpus.

## Risks / Trade-offs

- **A pointer can be skipped as easily as an implicit obligation.** Mitigation: the step names the bullet, the
  presentation step reports each record's disposition, and the command's Verification clause requires both the
  disposition and the withheld parked proposal, so an omission fails a stated check rather than passing silently.
- **The command and the parked periodic sweep could drift into duplicating each other.** Mitigation: the parked entry
  keeps its own trigger (a scheduled/on-demand audit sweep), and this change adds only the event-time match.
- **A future change could move the routing back into `lesson-capture`.** Mitigation: decision 1 records why it stays
  caller-side, and the design names the delta such a move would owe.

## Claims

Each row is a fact this change states about its evidence. Rows about the state before the change anchor to the unit's
base revision, `d9f0f5a62f6bd047c74d89c890f66074621793c2` (`git merge-base HEAD origin/main`).

| Claim                                                                                                          | Source or deriving command                                                                                                                                                            |
| -------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The spec corpus names the command once, for selection and hand-off.                                            | `git grep -n 'generalize-process-defects' d9f0f5a6 -- openspec/specs` → one hit, `showcase/quality/agent-skills/spec.md:2567`.                                                        |
| That requirement states no parked-idea, `Revisit when:`, or `docs/ideas.md` clause.                            | `git show d9f0f5a6:openspec/specs/showcase/quality/agent-skills/spec.md \| sed -n '2531,2683p' \| grep -in 'idea\|parked\|revisit'` → no output.                                      |
| The spec delegates the routing to `AGENTS.md`.                                                                 | `git show d9f0f5a6:openspec/specs/showcase/quality/agent-skills/spec.md \| sed -n '2583p'` → "the owning destinations and their routing are `AGENTS.md`'s generalization workflow's". |
| `lesson-capture`'s inputs and destinations omit `docs/ideas.md`.                                               | `git show d9f0f5a6:.opencode/agent/lesson-capture.md \| sed -n '19,48p'`, against the requirement's own enumeration at `spec.md:2585-2593`.                                           |
| The command's procedure has no match step.                                                                     | `git show d9f0f5a6:.opencode/commands/generalize-process-defects.md \| sed -n '12,26p'` → steps 1–6, none performing the match.                                                       |
| The three recurrence branches were carried into the generalization section deliberately.                       | `git show d9f0f5a6:openspec/changes/archive/2026-10-27-separate-lesson-capture-from-implementation/tasks.md \| sed -n '63,66p'` → task 1.11.                                          |
| The match has been executed at least once, by the calling session.                                             | `git show d9f0f5a6:docs/process-defects/extend-owner-reply-language-scope.md \| sed -n '46,47p'` → record #2's `Decision`.                                                            |
| The parked 2026-10-27 entry is the periodic sweep, not this event-time match.                                  | `git show d9f0f5a6:docs/ideas.md \| sed -n '73,84p'`.                                                                                                                                 |
| No ADR names the generalization, `lesson-capture`, or the defect corpus.                                       | `grep -rln 'lesson-capture\|generalize\|process-defect' docs/adr/` → no output (25 ADRs present).                                                                                     |
| `AGENTS.md`'s Procedure runs the command as its step 1 and summarizes the hand-off, apply, and decision steps. | `git show d9f0f5a6:AGENTS.md \| sed -n '/### Workflow: Generalize process defects/,/^### Workflow: Author a spec delta/p' \| sed -n '8,17p'` → steps 1–4.                             |
| The README's slash-command row still describes the pass's outcome.                                             | `git show d9f0f5a6:README.md \| sed -n '607p'` → "Turns the accumulated process-defect records into rules and improvements, recording a decision per record".                         |
| The single-sourcing split's pointer form is the one this pointer takes.                                        | `git show d9f0f5a6:AGENTS.md \| sed -n '293,299p'` → the split, with "only the owner's procedure steps, field set, or record are pointed at".                                         |
| The product diff is the command (`12 6`) and `AGENTS.md` (`4 3`); no other product path changes.               | `git diff --numstat` → those two rows, and nothing else once the unit's artifacts are staged.                                                                                         |

**Scan dismissals** — `scripts/claim-scan.py` seeded these rows. Each candidate it reported is resolved above or
dismissed by kind, one reason per kind:

- `path` — a repository path, a command, or a revision a row already derives, not a claim about itself.
- `cross-ref` — a record, bullet, or role name a sentence cites, never a count of those.
- `count` — a numbered task, step, decision, or record identifier, a line number, or a command's own output recorded in
  a Done note or a row.
- `absolute` — a quantifier or procedure word in prose ("only", "every", "never", "none"), the `skip_specs` statement
  the Capabilities rows establish, or a process record's own `Evidence` field, which carries that record's claims rather
  than this table.
