# Process bindings

The role-based bindings an adopting process component resolves. A target maintains its own copy with its own values.

- Read this declaration before using a binding, and read the referenced owner before relying on that owner's contract.
- A binding names a role, not a string.

Provenance: introduced by ADR-0026, and the portable contract for the `agent-skills` requirement "Adopting process
components use the declared bindings"; the first adopting slices are the pause/resume procedure and the `lesson-capture`
analysis.

## The roles

| Canonical name   | This repository's value                                                                                                          | Referenced owner                                                                                     |
| ---------------- | -------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| review-ledger    | `docs/review-rounds.tsv`                                                                                                         | the ledger's header (the record format)                                                              |
| lifecycle        | `AGENTS.md`, the "Workflow: Change lifecycle" heading                                                                            | the lifecycle rules in that section                                                                  |
| review-gate      | `AGENTS.md`, the "Workflow: The review gate" heading                                                                             | the approval stop and the gate entry point; the procedure is `.opencode/skills/review-gate/SKILL.md` |
| workspace-record | the unit's `tasks.md` `Workspace:` record, else the hand-off or report and the pull-request checklist                            | the workspace rule (`AGENTS.md`, "Workflow: Change lifecycle")                                       |
| change-artifacts | `openspec/changes/<change>/`                                                                                                     | the OpenSpec change directory and its schema                                                         |
| prior-findings   | the prior review round's report, preserved in the `Resume checkpoint`'s "Findings no record holds" field or the review hand-over | the review-gate handover rule                                                                        |

## The non-adopter trace

The unit's consumer trace names a component that does not adopt the bindings as a non-adopter, so the consumer set is
explicit rather than inferred.

## A missing binding

A component that cannot resolve a binding — missing, ambiguous, or semantically incompatible — stops the affected
operation and reports the specific dependency. It does not invent a value, delete the obligation, or weaken its policy.

## What stays explicit

Component entry paths and command names, the `Resume checkpoint` field set, the concrete values at their authoritative
owners, script arguments, generated OpenSpec paths, the enforcement mechanisms, and the lesson destinations and
recurrence routing. The declaration binds locations and authorities, not those.
