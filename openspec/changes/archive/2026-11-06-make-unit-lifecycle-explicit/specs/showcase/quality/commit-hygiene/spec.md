# Spec Delta — showcase/quality/commit-hygiene

## ADDED Requirements

### Requirement: The command lifecycle projection is verified

The standard `check` task SHALL verify every project-authored command's lifecycle declaration: its presence, its
recognized stage, invocation, and effect values, that every named owning procedure and bound heading resolves, that a
unit-starting branch carries the preflight its owning requirement states, and that the generated entry mapping covers
every generated command.

#### Scenario: An undeclared command fails

- **WHEN** a project-authored command carries no lifecycle declaration
- **THEN** the verification fails and names the command and the missing declaration

#### Scenario: An unrecognized value fails

- **WHEN** a declaration names a stage, invocation, or effect outside the published vocabulary
- **THEN** the verification fails and names the value

#### Scenario: A broken owner reference fails

- **WHEN** a declaration names an owning procedure or binding heading that does not resolve
- **THEN** the verification fails and names the unresolved reference

#### Scenario: A unit-starting branch lacks its preflight anchors

- **WHEN** a command declared as unit-starting omits the anchors its owning requirement names
- **THEN** the verification fails and names the missing anchor

#### Scenario: A generated command is unmapped

- **WHEN** a generated command has no entry in the lifecycle section's mapping
- **THEN** the verification fails and names the command

### Requirement: The projection check derives its coverage from the tracked set

The verification SHALL derive its coverage from the tracked command set rather than from a hand-maintained list, so an
added undeclared command fails rather than passing as an empty extraction.

#### Scenario: An empty extraction fails

- **WHEN** the verification's derived command set is empty, or its declared model is absent
- **THEN** the verification fails rather than reporting a clean empty set

### Requirement: The projection check states what it proves

The verification SHALL prove declaration completeness, permitted values, reference resolution, the required anchors, and
mapping coverage, and SHALL NOT be read as proof of a command's behaviour or of any owner decision.

#### Scenario: The controls prove the check can fail

- **WHEN** the verification's tests run
- **THEN** each predicate carries a known-bad case and a known-good one
