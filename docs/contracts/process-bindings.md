# Process bindings

The role-based bindings an adopting process component resolves. A target maintains its own copy with its own values.

- Read this declaration before using a binding, and read the referenced owner before relying on that owner's contract.
- A binding names a role, not a string.

Provenance: introduced by ADR-0026, and the portable contract for the `agent-skills` requirement "Adopting process
components use the declared bindings"; the first adopting slices are the pause/resume procedure and the `lesson-capture`
analysis.

## The roles

| Canonical name              | This repository's value                                                                                                                                                                                                                                       | Referenced owner                                                                                                                                                           |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| review-ledger               | `docs/units/<unit>/rounds.tsv`                                                                                                                                                                                                                                | the record format (`showcase/quality/commit-hygiene`, "The review-round record format is stated once")                                                                     |
| lifecycle                   | `AGENTS.md`, the "Workflow: Unit lifecycle" heading                                                                                                                                                                                                           | the lifecycle rules in that section                                                                                                                                        |
| review-gate                 | `AGENTS.md`, the "Workflow: The review gate" heading                                                                                                                                                                                                          | the approval stop and the gate entry point; the procedure is `.opencode/skills/review-gate/SKILL.md`                                                                       |
| workspace-record            | `docs/units/<unit>/unit.md`, and the change's `tasks.md` reference to it                                                                                                                                                                                      | the workspace rule (`AGENTS.md`, "Workflow: Unit lifecycle")                                                                                                               |
| change-artifacts            | `openspec/changes/<change>/`                                                                                                                                                                                                                                  | the OpenSpec change directory and its schema                                                                                                                               |
| process-defect-records      | `docs/units/<unit>/process-defects.md`                                                                                                                                                                                                                        | the record's field set (`AGENTS.md`, "Workflow: Record process defects")                                                                                                   |
| generalization-dispositions | `AGENTS.md`, the "Where the repository already records a recurrence decides its disposition" bullet in "Workflow: Generalize process defects" → "Decision points"                                                                                             | the recurrence dispositions                                                                                                                                                |
| extraction-procedure        | `AGENTS.md`, the "Workflow: Extract an `AGENTS.md` section" heading                                                                                                                                                                                           | the extraction procedure                                                                                                                                                   |
| branch-refresh              | `AGENTS.md`, the "Refreshing a branch" bullet under "Workflow: Unit lifecycle" → "Failure handling"                                                                                                                                                           | the branch-refresh rule                                                                                                                                                    |
| spec-delta                  | `AGENTS.md`, the "Workflow: Author a spec delta and archive" heading                                                                                                                                                                                          | the spec-delta-and-archive workflow                                                                                                                                        |
| prior-findings              | `docs/units/<unit>/findings/round-<N>.md`, or — for a round that predates the store — the `Resume checkpoint`'s "Findings no record holds" field or the review hand-over, at `docs/units/<unit>/review-handover.md` or in the change directory or its archive | the review-gate handover rule, and the store's format (`showcase/quality/commit-hygiene`)                                                                                  |
| agent-session-record        | `docs/units/<unit>/sessions.tsv`                                                                                                                                                                                                                              | the record's format (`showcase/quality/commit-hygiene`, "The agent-session record is stated once")                                                                         |
| deep-review-record          | `docs/units/<unit>/deep-review.md`                                                                                                                                                                                                                            | the record's format (`/review-thorough`), and the requirement that routes and bounds the review (`showcase/quality/agent-skills`, "The deep review is routed and bounded") |

`agent-session-record` is the unit's review-event spine: its rows carry the unit-wide event sequence, and for a unit
whose first round is dated on or after `2026-11-09` its quick rows carry the round each invocation attempts or reports.
`review-ledger` keeps the quick loop's outcome — the round, the classes it raised, the repeats, and the compliance-miss
count — and a pre-retirement unit's session fields with it. `deep-review-record` holds the routing decision, each pass,
and its findings' statuses, while the change's review task or pull-request checklist line carries only the obligation
and a pointer.

## The consumer trace

An adopting component resolves the roles it uses here. A component that does not adopt them is named below with its
reason, so the set is explicit rather than inferred.

| Component                                          | Roles it uses                                                                                                   |
| -------------------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| `.opencode/agent/experience-analyzer.md`           | review-ledger, process-defect-records                                                                           |
| `.opencode/agent/lesson-capture.md`                | review-ledger, change-artifacts, process-defect-records                                                         |
| `.opencode/agent/review-quick.md`                  | review-ledger, process-defect-records, agent-session-record                                                     |
| `.opencode/commands/apply-audit-reductions.md`     | lifecycle, extraction-procedure                                                                                 |
| `.opencode/commands/begin-unit.md`                 | lifecycle, workspace-record                                                                                     |
| `.opencode/commands/consult.md`                    | agent-session-record                                                                                            |
| `.opencode/commands/audit-agents.md`               | lifecycle, spec-delta                                                                                           |
| `.opencode/commands/audit-architecture.md`         | lifecycle, spec-delta                                                                                           |
| `.opencode/commands/audit-readme.md`               | lifecycle                                                                                                       |
| `.opencode/commands/audit-specs.md`                | lifecycle, spec-delta                                                                                           |
| `.opencode/commands/diagram.md`                    | lifecycle, review-gate                                                                                          |
| `.opencode/commands/flag-process-defect.md`        | lifecycle, review-gate, process-defect-records                                                                  |
| `.opencode/commands/generalize-process-defects.md` | lifecycle, process-defect-records, generalization-dispositions                                                  |
| `.opencode/commands/gradle-update.md`              | lifecycle, review-gate                                                                                          |
| `.opencode/commands/ideas.md`                      | lifecycle, review-gate                                                                                          |
| `.opencode/commands/opsx-tool-update.md`           | lifecycle, review-gate                                                                                          |
| `.opencode/commands/pause-unit.md`                 | lifecycle, review-gate                                                                                          |
| `.opencode/commands/resume-unit.md`                | lifecycle, review-gate                                                                                          |
| `.opencode/commands/retrospective.md`              | lifecycle, review-gate, process-defect-records                                                                  |
| `.opencode/commands/review-thorough.md`            | review-ledger, agent-session-record, deep-review-record                                                         |
| `.opencode/commands/sync-branch.md`                | lifecycle, review-gate, branch-refresh                                                                          |
| `.opencode/skills/begin-unit/SKILL.md`             | lifecycle, workspace-record                                                                                     |
| `.opencode/skills/pause-resume-unit/SKILL.md`      | lifecycle, review-gate, review-ledger, agent-session-record, workspace-record, change-artifacts, prior-findings |
| `.opencode/skills/review-gate/SKILL.md`            | review-ledger, agent-session-record, deep-review-record, prior-findings                                         |

The non-adopters, by class:

- the target's own governance and its spec corpus — `AGENTS.md` and `openspec/specs/` state the values rather than
  consuming them;
- this declaration itself, whose role table carries the values;
- the tooling that embeds a value — `scripts/commit-hygiene.py` embeds the round-record and agent-session-record
  directories in its checks, `scripts/review-rounds.py` defaults to the ledger and accepts `--record`, and reads each
  unit's agent-session register to echo a six-field row's session, `scripts/review-routing.py` defaults to it and
  accepts `--ledger`, `scripts/experience-analysis.sh` embeds the process-defect records directory in its git log
  filters, and the build declares the scripts as inputs. A target that moves the ledger or the records directory edits
  the tooling, so those values are the tooling's rather than a role;
- the dated and normative records that quote the values as they were when the record was written —
  `docs/units/*/process-defects.md`, `docs/decisions/`, `docs/retrospectives/`, `docs/audits/`, `docs/adr/`, and
  `docs/ideas.md`;
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
