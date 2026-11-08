# showcase/quality/commit-hygiene Specification

## ADDED Requirements

### Requirement: A quick reviewer's hand-over section is checked before the delegation

`scripts/commit-hygiene.py --handover <unit>:<round>` SHALL fail when the unit's `docs/units/<unit>/review-handover.md`
carries no second-level heading beginning with `Round` and the named round's exact token, when the file is missing or
unreadable, or when the argument is malformed.

`<unit>` SHALL be one directory name under `docs/units/`: non-empty, with no path separator, no `.` or `..` component,
no whitespace, and no extra colon. `<round>` SHALL be a canonical positive decimal integer: no sign, no leading zero,
and not zero. The heading's text SHALL begin with `Round`, then the named round's exact decimal token, and that token
SHALL be followed by the end of the text or by whitespace separating a descriptive suffix, so neither `Round 30` nor
`Round 3x` SHALL match round 3. The round token SHALL NOT be wrapped in backticks. The check SHALL read the working
tree's file: it SHALL NOT require the file to be staged, a review-ledger row, or an agent-session row.

The check SHALL be run after the section is written and before the delegation, and its failure SHALL stop the call.

The check SHALL establish presence at inspection only. It SHALL NOT establish the section's completeness, its
attribution to the invocation, its delivery, or that it was written before the delegation: copied content beneath a
matching current-round heading SHALL pass, and a heading written after the call SHALL pass, while a heading naming a
different round SHALL fail. Passing the check SHALL NOT attest that the delegation happened or that its hand-over was
adequate.

#### Scenario: A present heading passes

- **WHEN** the unit's file carries `## Round 3` and the check names round 3
- **THEN** the check passes

#### Scenario: A descriptive suffix passes

- **WHEN** the heading is `## Round 3 — proposal screen` and the check names round 3
- **THEN** the check passes

#### Scenario: A round token is matched exactly

- **WHEN** the file carries `## Round 30` and the check names round 3
- **THEN** the check fails, naming the unit and the round

#### Scenario: A different round fails

- **WHEN** the file carries only `## Round 2` and the check names round 3
- **THEN** the check fails, naming the unit and the round

#### Scenario: A missing file fails

- **WHEN** the unit's `review-handover.md` does not exist
- **THEN** the check fails, naming the missing file

#### Scenario: An unreadable file fails

- **WHEN** the file exists but cannot be decoded as UTF-8
- **THEN** the check fails, naming the file

#### Scenario: A malformed argument fails

- **WHEN** the argument is not `<unit>:<round>` with a canonical positive round number
- **THEN** the check fails, naming what it expected

#### Scenario: A unit that is not one directory name fails

- **WHEN** the unit part is empty, contains a path separator, is `.` or `..`, contains whitespace, or adds a second
  colon
- **THEN** the check fails, naming the unit it received

#### Scenario: A third-level heading is not a section

- **WHEN** the file carries `### Round 3` rather than a second-level heading
- **THEN** the check fails

#### Scenario: Copied content passes beneath a matching heading

- **WHEN** the round's heading matches and its body was copied from an earlier round
- **THEN** the check passes, reporting only that a matching heading was present when it ran

#### Scenario: The check does not establish delivery or ordering

- **WHEN** the check passes for a section written after the delegation
- **THEN** it reports only that a matching heading was present when it ran
