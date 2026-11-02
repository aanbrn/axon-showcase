# Design — the second adopting change

## Context

ADR-0026 made the bindings declared, and `process-bindings-contract` adopted them in the pause/resume slice and the
`lesson-capture` analysis, then recorded a consumer trace in its own archived `design.md`. That trace was the unit's, so
the live adopter set has no owner once the change archives. The components outside those four slices still name
`docs/review-rounds.tsv` literally, and `review-gate` — the component a target project copies first — carries four of
them.

ADR-0026 required the declaration to bind a role, not a string, and the ledger's role is identical in all four: its
location, whose format the ledger's own header owns. Nothing about the review-gate procedure, the quick reviewer's
record line, the analyzer's evidence, or the thorough-review command's record depends on the path itself.

## Decisions

### D1 — The trace belongs to the declaration

`docs/contracts/process-bindings.md` gains the consumer trace: the adopting components with the roles each uses, and the
non-adopting classes with each one's reason. The trace is a property of the target's adoption set, not of the change
that first populated it, and the archived change's copy stays as recorded.

### D2 — Adopt only the review-ledger role in this change

The four components adopt one role. The lifecycle and review-gate heading references across the remaining components are
a wider sweep with the same shape, parked in `docs/ideas.md` rather than folded in: their failure mode differs (a
heading that resolves to nothing, rather than a path that does not exist), and one unit that mixes the two cannot report
either sweep as complete.

### D3 — The read route belongs where the component reads the ledger

`review-gate` reads the declaration before using the ledger, as the pause/resume skill and `lesson-capture` already do.
The other three name the ledger and never read it — `review-quick` as the caller's append destination, the
`review-thorough` command to exclude the deep review's record from it, and `experience-analyzer` to name the source of a
derived-facts input — so each points at the declaration inline rather than carrying a read route its agent would never
take.

### D4 — What stays literal

Each component keeps the values it owns, and the declaration owns the non-adopter set rather than this design:

- the ledger's own header and row format, which the format's owner and the ledger itself carry;
- the enforcement tooling, which embeds or defaults to the path and is edited by a target that renames the ledger;
- the dated and normative records that quote the path as it was;
- `AGENTS.md`.

`AGENTS.md` is a literal exception because the lifecycle binding names it as an owner, not a consumer.

## The consumer trace

Recorded in `docs/contracts/process-bindings.md`, which owns both sets: the adopters with the roles each uses, and the
non-adopting classes with their reasons.

## Evidence

The sweep is three commands, reproducible from the repository:

- `git grep -c 'review-rounds\.tsv' .opencode/` prints nothing and exits 1, so no component carries the path.
- `git grep -l 'review-rounds\.tsv' -- ':!openspec/changes/archive' | wc -l` returns 45.
- every one of those paths matches one of the declaration's non-adopter classes:
  `git grep -l 'review-rounds\.tsv' -- ':!openspec/changes/archive' | grep -v -E '^(AGENTS\.md|build\.gradle\.kts|scripts/|docs/(contracts|adr|process-defects|decisions|retrospectives|audits)/|docs/ideas\.md|openspec/(specs|changes)/)'`
  prints nothing and exits 1.

Two controls, each run against the command it exercises and then reverted:

- the first command: appending `docs/review-rounds.tsv` to `.opencode/agent/review-quick.md` made it print `1`;
  restoring the file returned it to `0`.
- the third command: creating `SCRATCH.md` at the repository root carrying the literal and staging it with
  `git add -N SCRATCH.md` made the filter print `SCRATCH.md` and exit 0, since `git grep` searches tracked files;
  `git reset -- SCRATCH.md` and deleting the file restored the empty output and exit 1.

## Claims table

| Claim                                                          | Source                                        | Result                                             |
| -------------------------------------------------------------- | --------------------------------------------- | -------------------------------------------------- |
| The four components name the role, not the path                | `git grep -c 'review-rounds\.tsv' .opencode/` | 0 hits                                             |
| The declaration carries the adopter set                        | `docs/contracts/process-bindings.md`          | 8 adopter rows, 7 non-adopter classes              |
| Every remaining hit matches a declared class's path set        | the sweep's filter over 45 files              | empty, exit 1; each control failed its own command |
| The delta modifies the requirement without dropping a scenario | `openspec validate --changes`                 | 1 passed, 0 failed                                 |
| The main spec corpus stays valid                               | `openspec validate --all`                     | 27 passed, 0 failed                                |
| No new role was introduced                                     | the declaration's table                       | 6 roles                                            |
