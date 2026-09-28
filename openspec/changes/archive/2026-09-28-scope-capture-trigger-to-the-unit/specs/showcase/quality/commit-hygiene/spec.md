## MODIFIED Requirements

### Requirement: Captured marker placement is verified by the build

The standard `check` task SHALL verify that each `captured:` provenance marker in `AGENTS.md` lies within a rule block —
a bold-lead bullet or a bold-lead paragraph — and never on a plain `- Text` bullet, so a misplaced marker fails the CI
`build` gate even when no local hook is installed or the hook was bypassed. A backticked mention of the token in prose
(`` `captured:` ``) is not a marker and SHALL NOT be treated as one. The marker's existence rule (each captured rule
carries a greppable `captured: <unit>` origin) is specified in `showcase/quality/agent-skills`.

#### Scenario: The standard check verifies marker placement

- **WHEN** the standard `check` task runs
- **THEN** it includes the `captured:` marker-placement verification for `AGENTS.md`

#### Scenario: A marker outside a rule block fails the build

- **WHEN** a `captured:` marker in `AGENTS.md` sits on a plain `- Text` bullet rather than inside a rule block
- **THEN** the marker-placement verification fails and reports the offending location

#### Scenario: A backticked prose mention is not a marker

- **WHEN** `AGENTS.md` mentions the token in prose as a backticked `` `captured:` ``
- **THEN** the marker-placement verification does not treat that mention as a marker

#### Scenario: Correctly placed markers pass

- **WHEN** every `captured:` marker in `AGENTS.md` lies within a rule block
- **THEN** the marker-placement verification passes
