# Design — consolidate the governance contracts

## Context

ADR-0025 §7 fixes the first consolidation: the knowledge-authoring standard and the shared report contract get portable
owners; the recurrence dispositions and the audit assumptions stay with their workflows; the consumers are derived from
the references; the spec's ownership clauses are retargeted through an `agent-skills` delta; and the Purpose is
refreshed at archive. The two reuse pilots are the evidence: both lost or imported these contracts.

## Decisions

### D1 — The contracts live in `docs/contracts/`, not in the spec

The spec keeps the capability outcome and the scenarios; the contract file owns the operational rules. Keeping the rules
in the spec is what made a target copy the corpus. The contract file carries provenance and is loaded by the consumers
that need the rules.

### D2 — The spec's requirement descriptions retarget; the scenarios stay

A `MODIFIED` block for each of the eight requirements whose description changes: the two owners name the contract file;
the six that reference the budget's owner point at it; the scenario sets are preserved verbatim, because the scenarios
are the capability's behavior. The requirement headers stay, so no `REMOVED`/`ADDED` pair is needed.

### D3 — The consumers name the contract at their first reference

The eight definitions, the scheduled `audit` workflow, and `AGENTS.md` point at the contract file; the three audit
commands keep the short name. The consumer set and its derivation are the claims table's row, not restated here.

### D4 — The Purpose refresh is an archive-commit edit

The Purpose describes the knowledge-authoring standard as a capability requirement; the portable owner changes that
description, and a delta cannot carry a Purpose. The archive commit edits it, as the spec-delta workflow requires.

## Alternatives

- **Keep the rules in the spec** — rejected: a target copies the corpus, which is the coupling the pilots measured.
- **A process pack** — rejected: the contract is a reference, not a procedure, and a pack is a subject destination.
- **An omnibus process document** — rejected: it would duplicate owners and hide the per-contract boundary.

## Risks

- The contract and the spec can drift: the spec's scenarios are the behavior, the contract is the rule. The review reads
  both; no audit covers `docs/contracts/` yet — the `agents-auditor`'s scope is `AGENTS.md`,
  `.opencode/{agent,commands,skills}/`, and `docs/guidance-evidence/` — so the drift is unmitigated until that scope
  widens, and `docs/ideas.md` carries the follow-up.
- The consumers' pointers can drift: the spec's requirement names the contract file as the owner, so a stale pointer is
  a finding for the review.

## Claims table

| Claim                                                                          | Source                                                                                                                                                                                    | Result                                                                                                                                                                                        |
| ------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The eight requirements' scenario sets are preserved                            | `openspec validate --all`; the delta's `#### Scenario:` count                                                                                                                             | 90 scenarios (16 owner + 74 referencing)                                                                                                                                                      |
| The modified requirement set is complete                                       | `grep -n 'per-item budget' openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                                          | 7 requirements carry the phrase (the 6 referencing + the authoring owner); the report-contract owner carries the budget without the phrase; the delta has 8 blocks (2 owners + 6 referencing) |
| The consumer set is derived from references                                    | `git grep -l -E -e 'shared report' -e 'knowledge-authoring' -- ':!docs/contracts' ':!docs/adr' ':!openspec' ':!docs/ideas.md' ':!docs/audits' ':!docs/retrospectives' ':!docs/decisions'` | 13 live files: 8 definitions, the 3 audit commands, `AGENTS.md`, `.github/workflows/audit.yml`; the commands and the workflow present a report and use the short name                         |
| The contracts copy the spec's rules                                            | the contract files against the spec's two requirement descriptions                                                                                                                        | the representation/procedure/gotcha/evidence/validation rules; the verdict/budget/verified/bounded/no-alternatives rules                                                                      |
| The Purpose names the knowledge-authoring standard as a capability requirement | the spec's `## Purpose`                                                                                                                                                                   | the refresh is owed at archive                                                                                                                                                                |
| The two pilots lost or imported these contracts                                | `docs/decisions/2026-10-31-process-reuse-pilot.md`; `docs/decisions/2026-10-31-lesson-capture-reuse.md`                                                                                   | the reviewer-session and subject-pack contracts unexercised; the authoring and report contracts imported                                                                                      |
| The portable-contract owner class                                              | `docs/adr/0025-process-core-and-project-substance.md` §3                                                                                                                                  | a `docs/contracts/` file with provenance                                                                                                                                                      |
