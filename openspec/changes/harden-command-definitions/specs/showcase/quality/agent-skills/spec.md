# Spec Delta

## MODIFIED Requirements

### Requirement: Audit reduction candidates are applied by a standing owner-gated unit

The repository SHALL provide a standing, owner-gated unit, invoked by an `/apply-audit-reductions` command, that applies
an audit report's reduction candidates (its merge, removal, route, and extraction findings), routing a content-move to
the change workflow, so they do not age. It SHALL never apply an **extraction opportunity**, which is not a candidate:
an opportunity is a design recommendation the owner carries out in a dedicated change. It SHALL present the candidates
for the owner's selection, apply only the approved ones, and pass the per-unit review gate before the change ships; it
SHALL NOT run automatically.

#### Scenario: A report's reduction candidates are presented and applied

- **WHEN** the owner invokes the unit for the newest report under `docs/audits/` (a date carrying more than one report
  is offered for the owner to choose), or for one they name
- **THEN** it presents that report's reduction candidates and applies only the ones the owner approves

#### Scenario: An applied merge applies the candidate's merged text

- **WHEN** the unit applies an approved merge candidate
- **THEN** it applies the candidate's merged text, or deletes the duplicate the candidate flags

#### Scenario: An applied route reduces the rule to a verified pointer

- **WHEN** the unit applies an approved route candidate that reduces the rule to a pointer
- **THEN** it reduces the rule to a pointer at the target the candidate names, following the capture bullet's
  pointer/trim rule

#### Scenario: A route candidate that moves content into a spec or ADR is routed to a change

- **WHEN** an approved route candidate proposes moving the rule's content into the spec or ADR that owns it
- **THEN** the unit routes that edit through the normal change workflow rather than editing the spec or ADR inline

#### Scenario: An applied removal deletes a rule that governs no decision

- **WHEN** the unit applies an approved removal candidate
- **THEN** it deletes the rule the candidate names, recording what the deletion loses and whether git preserves it

#### Scenario: An applied reduction clears its parked entry

- **WHEN** the report's candidate was parked in `docs/ideas.md`
- **THEN** the unit removes the applied entry

#### Scenario: Application is owner-gated and reviewed

- **WHEN** the unit applies reductions
- **THEN** it does so only on the owner's selection, and it runs the full per-unit review gate over the result — the
  pre-write pass and its claims table, the formatter and mechanical checks, the `review-quick` loop with the
  session-continuity preflight, the deep-review routing decision, a required deep review, the capture decision, and the
  owner's manual review pass — before the change ships

#### Scenario: An extraction candidate's application is routed to a change

- **WHEN** the owner approves an extraction candidate
- **THEN** the unit routes the move to the normal change workflow rather than editing the destination inline, since the
  move creates or changes a destination that the change owns

#### Scenario: An extraction opportunity is never applied

- **WHEN** the report carries an extraction opportunity
- **THEN** the unit does not present or apply it, because an opportunity is a design recommendation rather than a
  candidate

## ADDED Requirements

### Requirement: An audit's approved findings are delivered as separate units

An audit command SHALL separate report generation from applying findings: the report is presented and the owner selects,
and the applying is a unit of its own. A run with nothing actionable or approved SHALL report that and stop, creating no
unit. Approved findings whose routes or scopes differ SHALL be split into separate units, and unrelated fixes SHALL NOT
be batched because one report carried them.

#### Scenario: Report generation is separated from applying findings

- **WHEN** an audit command has presented its findings and the owner has selected from them
- **THEN** applying the selection is a unit of its own, not a continuation of the reporting run

#### Scenario: A run with nothing actionable stops without a unit

- **WHEN** an audit reports no finding the owner approves, or none that is actionable
- **THEN** the command reports that and stops, creating no unit

#### Scenario: Findings with different routes are separate units

- **WHEN** approved findings differ in delivery route or in scope
- **THEN** they are split into separate units, each scoped to its own artifacts and diff

#### Scenario: Unrelated fixes are not batched

- **WHEN** one report carries findings of unrelated subjects
- **THEN** they are not batched into one unit merely because the same report carried them

#### Scenario: An applying unit runs the full review gate

- **WHEN** a unit applies approved audit findings
- **THEN** it runs the full per-unit review gate, not the formatter and a quick review alone

### Requirement: An audit finding's delivery route follows its kind

An approved audit finding SHALL be delivered by the route its kind requires. The main specs SHALL stay immutable outside
archive: a spec change routes through an OpenSpec change with a delta spec, and a code change, or a command or
subagent-definition change altering spec-covered behavior, through the normal change workflow. An advisory observation
is the owner's decision, never applied automatically.

#### Scenario: A spec change routes through a change with a delta

- **WHEN** an approved finding changes a capability's behavior
- **THEN** it routes through an OpenSpec change carrying a delta spec, and the main spec is edited only at archive

#### Scenario: A definition change altering spec-covered behavior routes through a change

- **WHEN** an approved finding edits a command or subagent definition whose behavior a capability spec describes
- **THEN** it routes through the normal change workflow with that capability's delta, not a docs edit

#### Scenario: An advisory observation is the owner's decision

- **WHEN** a report carries an advisory observation
- **THEN** it is presented for the owner's decision and is never applied automatically

#### Scenario: A reduction candidate keeps its own route

- **WHEN** an approved finding is a merge, removal, route, or extraction candidate
- **THEN** it is applied by that kind's route, and an extraction opportunity is never applied at all

### Requirement: Project-authored command definitions that start a mutating unit carry the lifecycle preflight

A project-authored command that starts a mutating unit that ships SHALL state the lifecycle its unit owes: its own
branch from a freshly fetched `origin/main`; unrelated dirty changes preserved; work uncommitted until the manual
approval pass; and no commit, push, pull request, archive, or merge without that approval. It SHALL point at the
workflow owning the lifecycle and the review gate, and every `review-quick` invocation SHALL carry the
session-continuity preflight.

#### Scenario: A mutating command states the branch rule

- **WHEN** a project-authored command starts a mutating unit that ships
- **THEN** it states that the unit starts on its own branch from a freshly fetched `origin/main`

#### Scenario: A mutating command preserves unrelated work

- **WHEN** a unit the command starts finds unrelated dirty-worktree changes
- **THEN** the command preserves them rather than reverting or committing them

#### Scenario: Work stays uncommitted until approval

- **WHEN** a mutating command's unit has finished its edits
- **THEN** the work stays uncommitted until the owner's explicit approval, and no commit, push, pull request, archive,
  or merge happens before it

#### Scenario: A review invocation carries the session preflight

- **WHEN** a mutating command invokes `review-quick`
- **THEN** the invocation carries the session-continuity preflight the review gate's procedure owns
