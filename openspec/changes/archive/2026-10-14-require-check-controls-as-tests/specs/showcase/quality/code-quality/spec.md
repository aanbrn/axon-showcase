## ADDED Requirements

### Requirement: A check's control is a committed test

A change that adds or changes a `check` gate SHALL include a committed test asserting the gate fails on a known-bad
input and passes on a known-good one — a gate whose logic lives in a rules object carries the case in that object's
`*RulesTests`, and a gate that works inline carries it in its consuming module — rather than a checklist task asserting
the control was run.

#### Scenario: A new or changed gate ships with its control

- **WHEN** a change adds or changes a `check` gate
- **THEN** its tests include a known-bad case that fails the gate and a known-good case that passes, so a defect in the
  gate is red in the author's own run rather than a ticked box

#### Scenario: A gate with a rules object carries the case there

- **WHEN** the gate's decision lives in a rules object
- **THEN** that object's `*RulesTests` carries the known-bad and known-good cases, so the case runs wherever the object
  is used

#### Scenario: A gate whose logic is inline is controlled in its consuming module

- **WHEN** a gate does its work inline, with no separate rules object
- **THEN** its consuming module's tests carry the known-bad and known-good cases, and the gate's registration is
  verified through that module
