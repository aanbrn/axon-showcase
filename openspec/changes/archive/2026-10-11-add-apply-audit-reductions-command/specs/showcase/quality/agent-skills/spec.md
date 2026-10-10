# Spec Delta

## ADDED Requirements

### Requirement: Audit reduction candidates are applied by a standing owner-gated unit

The repository SHALL provide a standing, owner-gated unit, invoked by an `/apply-audit-reductions` command, that applies
an audit report's reduction candidates (its merge, removal, and route findings), routing a content-move to the change
workflow, so they do not age. It SHALL present the candidates for the owner's selection, apply only the approved ones,
and pass the per-unit review gate before the change ships; it SHALL NOT run automatically.

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
- **THEN** it does so only on the owner's selection, runs the `review-quick` subagent over the result until clean, and
  asks for the owner's manual review before the change ships
