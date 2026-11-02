# Process bindings

The role-based bindings an adopting process component resolves. A target maintains its own copy with its own values.

- Read this declaration before using a binding, and read the referenced owner before relying on that owner's contract.
- A binding names a role, not a string.

Provenance: introduced by ADR-0026, and the portable contract for the `agent-skills` requirement "Adopting process
components use the declared bindings"; the first adopting slices are the pause/resume procedure and the `lesson-capture`
analysis.

## The roles

| Canonical name         | This repository's value                                                                                                          | Referenced owner                                                                                     |
| ---------------------- | -------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| review-ledger          | `docs/review-rounds.tsv`                                                                                                         | the ledger's header (the record format)                                                              |
| lifecycle              | `AGENTS.md`, the "Workflow: Change lifecycle" heading                                                                            | the lifecycle rules in that section                                                                  |
| review-gate            | `AGENTS.md`, the "Workflow: The review gate" heading                                                                             | the approval stop and the gate entry point; the procedure is `.opencode/skills/review-gate/SKILL.md` |
| workspace-record       | the unit's `tasks.md` `Workspace:` record, else the hand-off or report and the pull-request checklist                            | the workspace rule (`AGENTS.md`, "Workflow: Change lifecycle")                                       |
| change-artifacts       | `openspec/changes/<change>/`                                                                                                     | the OpenSpec change directory and its schema                                                         |
| process-defect-records | `docs/process-defects/<unit>.md`                                                                                                 | the record's field set (`AGENTS.md`, "Workflow: Record process defects")                             |
| spec-delta             | `AGENTS.md`, the "Workflow: Author a spec delta and archive" heading                                                             | the spec-delta-and-archive workflow                                                                  |
| prior-findings         | the prior review round's report, preserved in the `Resume checkpoint`'s "Findings no record holds" field or the review hand-over | the review-gate handover rule                                                                        |

## The consumer trace

An adopting component resolves the roles it uses here. A component that does not adopt them is named below with its
reason, so the set is explicit rather than inferred.

| Component                                          | Roles it uses                                                                             |
| -------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| `.opencode/agent/experience-analyzer.md`           | review-ledger, process-defect-records                                                     |
| `.opencode/agent/lesson-capture.md`                | review-ledger, change-artifacts, process-defect-records                                   |
| `.opencode/agent/review-quick.md`                  | review-ledger, process-defect-records                                                     |
| `.opencode/commands/apply-audit-reductions.md`     | lifecycle                                                                                 |
| `.opencode/commands/audit-agents.md`               | lifecycle, spec-delta                                                                     |
| `.opencode/commands/audit-architecture.md`         | lifecycle, spec-delta                                                                     |
| `.opencode/commands/audit-readme.md`               | lifecycle                                                                                 |
| `.opencode/commands/audit-specs.md`                | lifecycle, spec-delta                                                                     |
| `.opencode/commands/diagram.md`                    | lifecycle, review-gate                                                                    |
| `.opencode/commands/flag-process-defect.md`        | lifecycle, review-gate, process-defect-records                                            |
| `.opencode/commands/generalize-process-defects.md` | lifecycle, process-defect-records                                                         |
| `.opencode/commands/gradle-update.md`              | lifecycle, review-gate                                                                    |
| `.opencode/commands/ideas.md`                      | lifecycle, review-gate                                                                    |
| `.opencode/commands/opsx-tool-update.md`           | lifecycle, review-gate                                                                    |
| `.opencode/commands/pause-unit.md`                 | lifecycle, review-gate                                                                    |
| `.opencode/commands/resume-unit.md`                | lifecycle, review-gate                                                                    |
| `.opencode/commands/retrospective.md`              | lifecycle, review-gate, process-defect-records                                            |
| `.opencode/commands/review-thorough.md`            | review-ledger                                                                             |
| `.opencode/commands/sync-branch.md`                | lifecycle, review-gate                                                                    |
| `.opencode/skills/pause-resume-unit/SKILL.md`      | lifecycle, review-gate, review-ledger, workspace-record, change-artifacts, prior-findings |
| `.opencode/skills/review-gate/SKILL.md`            | review-ledger                                                                             |

The non-adopters, by class:

- the target's own governance and its spec corpus — `AGENTS.md` and `openspec/specs/` state the values rather than
  consuming them;
- this declaration itself, whose role table carries the values;
- the tooling that embeds a value — `scripts/commit-hygiene.py` embeds the ledger's path in its checks,
  `scripts/review-rounds.py` defaults to it and accepts `--record`, `scripts/experience-analysis.sh` embeds the records
  directory in its git log filters, and the build declares the scripts as inputs. A target that moves the ledger or the
  records directory edits the tooling, so those values are the tooling's rather than a role;
- the dated and normative records that quote the values as they were when the record was written —
  `docs/process-defects/`, `docs/decisions/`, `docs/retrospectives/`, `docs/audits/`, `docs/adr/`, and `docs/ideas.md`;
- a change's own artifacts, while that change is working on the declaration or an adopting component;
- the `.opencode/` components that name no binding.

An adopting component's own entry pointer stays explicit: `review-gate`'s `Source:` line names the section whose
procedure it is.

## A missing binding

A role that names a location does not create it: a target without the artifact a role names treats that as a missing
contract, never as an instruction to invent the artifact.

A component that cannot resolve a binding — missing, ambiguous, or semantically incompatible — stops the affected
operation and reports the specific dependency. It does not invent a value, delete the obligation, or weaken its policy.

## What stays explicit

Component entry paths and command names, the `Resume checkpoint` field set, the concrete values at their authoritative
owners, script arguments, generated OpenSpec paths, the enforcement mechanisms, and the lesson destinations and
recurrence routing. The declaration binds locations and authorities, not those.
