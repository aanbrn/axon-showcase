# Tasks

The unit's workspace record is `docs/units/record-unit-change-layering/unit.md`.

## 1. Proposal stage

- [x] 1.1 Ran the pre-write derivation pass over this proposal and its evidence, and filled the claims table below —
      **Done** when every row's source reproduces the text it records, and each candidate
      `python3 scripts/claim-scan.py` returns is a row or a dismissal stating the reason by class.
- [x] 1.2 Ran the proposal-stage quick review over rounds 1–4 to a clean closing round, recording each round in
      `docs/units/record-unit-change-layering/rounds.tsv` and its instances in `findings/round-<N>.md` — **Done** when
      the closing round's classes read `-` and
      `python3 scripts/commit-hygiene.py --unique-rounds --expect-clean record-unit-change-layering` passes.
- [x] 1.3 Ask the owner for the manual review pass over the proposal and end the turn there — **Done** when the owner
      replies; no `AGENTS.md` or ADR edit happens before that reply.

## 2. Implementation

- [x] 2.1 Append the dated `## Amendment` to `docs/adr/0029-unit-state-lives-outside-the-change-plan.md`, carrying the
      layering, the supersession of its Context clause with a pointer at that sentence, and the declined alternative —
      **Done** when the section is read against ADR-0029's Decision and Context, against `docs/adr/README.md`'s
      amendment convention, and states nothing the repository does not evidence.
- [x] 2.2 Replace `AGENTS.md`'s "OpenSpec Workflow Agreement" paragraph with the definition — **Done** when the
      replacement is read against the paragraph it replaces and keeps each fact it carries: a unit is intended for one
      pull request but need not ship, an OpenSpec change is one form of a unit and the other forms the section names are
      units too, and "change" reads as "unit of work"; the file grows by no more than one line; and
      `git grep -n 'read "unit of work"' origin/main -- AGENTS.md README.md docs .opencode openspec` still returns only
      the replaced paragraph's line, proving no other site cited the rule.
- [x] 2.3 Write `docs/units/record-unit-change-layering/process-defects.md` as defects are observed, or state that the
      unit observed none — **Done** when the file exists and each record carries the field set.
- [x] 2.4 Write the implementation-stage finding instances and round rows as each quick-review round arrives — **Done**
      when the ledger's checks pass.
- [x] 2.5 Run the implementation-stage quick review to a clean closing round — **Done** when it reports no new
      observations and `--expect-clean` passes.
- [x] 2.6 Derive the deep-review routing floor with
      `python3 scripts/review-routing.py --unit record-unit-change-layering --change record-unit-change-layering` and
      record the outcome with its signals on this task — **Done** when the outcome line is written here. Outcome:
      `RECOMMENDED`, signal `[repeated-class]` — 2 rows repeat a prior class (`incomplete-enumeration`, rounds 3 and 5);
      awaiting the owner's run-or-skip decision.
- [x] 2.7 Run the routed deep review when the outcome requires or the owner selects it — **Done** when its status is
      recorded here, or when the outcome reads `NOT_NEEDED`. Status: first `RECOMMENDED — explicit SKIP` on the owner's
      instruction after the advisor's consultation, then reversed by the owner's instruction to review with the advisor
      to agreement; the deep review ran over this tree in two passes. Pass 1 (initial) raised two findings —
      `[record-staleness]` at this task and `[record-scope-slide]` at ADR-0029's amendment — and pass 2 marked both
      `FIXED_AND_VERIFIED` and found nothing else. The routing's residual risk stands: an overlooked semantic ambiguity
      or unsupported supporting claim in the definition or the amendment, since mechanical checks do not prove future
      agents will classify units correctly.
- [x] 2.8 Run the gates over the finished tree — `./gradlew spotlessApply`, then
      `./gradlew spotlessCheck verifyWorkspaceRecords verifyUnitLayout verifyUniqueReviewRounds verifyFindingInstances`
      and `openspec validate --all` — **Done** when each command exits zero on the final edit.
- [x] 2.9 Ask the owner for the manual review pass over the finished unit and end the turn there — **Done** when the
      owner replies.

## Claims

| Claim                                                                                                             | Source                                                                                                                                                                                                                                                                                        |
| ----------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The corpus names `docs/adr/` in exactly two files: the architecture-auditor's surface and a check's artifact list | `git grep -n "docs/adr" origin/main -- openspec/specs` → `agent-skills/spec.md:744`, `merge-governance/spec.md:516`                                                                                                                                                                           |
| `showcase/quality/commit-hygiene` already specifies the unit record and its reference                             | `git show origin/main:openspec/specs/showcase/quality/commit-hygiene/spec.md` → `### Requirement: A unit records its workspace in its unit record`                                                                                                                                            |
| ADR-0029's Status is `Accepted` and the amendment leaves it unchanged                                             | `git show origin/main:docs/adr/0029-unit-state-lives-outside-the-change-plan.md` → `Status: Accepted`                                                                                                                                                                                         |
| ADR-0029's Context says the record cannot outlive the change directory                                            | `git show origin/main:docs/adr/0029-unit-state-lives-outside-the-change-plan.md` → `And the record cannot outlive the change directory: the archive`                                                                                                                                          |
| ADR-0029 names no layering rationale and no declined alternative of carrying every unit as a change               | `git show origin/main:docs/adr/0029-unit-state-lives-outside-the-change-plan.md` → its Context, Decision, and `Alternatives considered and rejected`                                                                                                                                          |
| A unit may take a form other than an OpenSpec change                                                              | `git show origin/main:AGENTS.md` → `an OpenSpec change, a docs refresh, a standalone fix, or a dependency bump`; ADR-0029 → `it may or may not carry an OpenSpec change directory`                                                                                                            |
| ADR-0029's Decision already places the record outside the change and keeps it there after archive                 | `git show origin/main:docs/adr/0029-unit-state-lives-outside-the-change-plan.md` → `The record is never archived with the change; it stays at its path`                                                                                                                                       |
| No capability spec states the amendment convention; `docs/adr/README.md` owns it                                  | `git grep -ni "amendment" origin/main -- openspec/specs` → no match (exit 1); `git show origin/main:docs/adr/README.md` → `dated ## Amendment naming what changed, with a pointer at the falsified sentence`                                                                                  |
| The `Workspace:` record is written when the workspace is established, before the unit's first content write       | `git show origin/main:AGENTS.md` → `Establish the unit's workspace before its first write.`; ADR-0029's Decision → `written when the unit's workspace is established, before its first content write`                                                                                         |
| A merge-revealed defect is appended to the unit's process-defect record after the merge                           | `git show origin/main:AGENTS.md` → `A defect a merge alone reveals … is appended to the merged unit's record.`                                                                                                                                                                                |
| Archiving relocates a change's files rather than destroying them                                                  | `git show origin/main:AGENTS.md` → `the archive relocates that directory`                                                                                                                                                                                                                     |
| Nothing else cites the rule the replaced paragraph states                                                         | `git grep -n 'read "unit of work"' origin/main -- AGENTS.md README.md docs .opencode openspec` → one hit, `AGENTS.md:128`, so no other site cites it                                                                                                                                          |
| A unit need not ship, so the definition cannot require a pull request                                             | `git show origin/main:openspec/specs/showcase/quality/agent-skills/spec.md` → `#### Scenario: A rejected unit closes in its own tree`                                                                                                                                                         |
| The behavior the AGENTS.md definition orients has existing owners                                                 | `agent-skills` → `A continuation establishes which unit it continues`; `commit-hygiene` → `A unit records its workspace in its unit record`, `A pre-commit guard blocks a mechanically defective staged set`, and `A unit's readiness for the manual review pass is verified at the boundary` |
| ADR-0025 assigns behavior to the capability spec rather than a second copy                                        | `git show origin/main:docs/adr/0025-process-core-and-project-substance.md` → `behavior in the capability spec … rather than carrying a second copy`                                                                                                                                           |

### Dismissals

Each reason is stated by class, and the class governs every candidate the scan returned for it.

- `path` — a candidate is a locator, not a claim about the repository: it names one of this unit's own artifacts, a file
  the base revision holds, or a command the tasks prescribe.
- `absolute` — a candidate is the word a scope statement or a prohibition is phrased with, around an obligation this
  change states rather than a measured quantity; a candidate that does carry a universal claim about the corpus has its
  row in the table above, which is what states the row set.
- `count` — a candidate is a numeral from any identifier or from a command's reported output (a total, an exit status, a
  line count); none is a count this change restates as durable.
- `cross-ref` — a candidate is a name, identifier, or heading that locates a repository artifact, binding, or record
  rather than citing a requirement this change must resolve.
