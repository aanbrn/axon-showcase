# Spec Delta

## MODIFIED Requirements

### Requirement: Captured marker placement is verified by the build

The standard `check` task SHALL verify that each `captured:` provenance marker in `AGENTS.md` and in the
project-authored guidance files (`.opencode/agent/*.md` and `.opencode/skills/*/SKILL.md`) lies within a rule block — a
bold-lead bullet or a bold-lead paragraph — and ends it, so no rule text of that block follows the marker; the block
ends at the next bullet, numbered step, bold-lead paragraph, heading, table row, fence, or blank line. The rest of the
marker's chain, a name wrapped onto the next line, a citation after a name, and an `evidence:` tag line are its own
text, not rule text that follows it. A marker that ends the first paragraph of a rule whose second paragraph continues
it passes, because the check judges the block and not the whole rule. A marker inside a fenced block is not rule prose
and SHALL NOT be judged, whether it annotates a command example or records the origin of a command block's comment
guidance. A marker SHALL never sit on a plain `- Text` bullet, so a misplaced marker fails the CI `build` gate even when
no local hook is installed or the hook was bypassed. A backticked mention of the token in prose (`` `captured:` ``) is
not a marker and SHALL NOT be treated as one. The marker's existence rule (each captured rule carries a greppable
`captured: <unit>` origin) is specified in `showcase/quality/agent-skills`.

#### Scenario: The standard check verifies marker placement

- **WHEN** the standard `check` task runs
- **THEN** it includes the `captured:` marker-placement verification for `AGENTS.md` and each project-authored guidance
  file

#### Scenario: A marker outside a rule block fails the build

- **WHEN** a `captured:` marker in `AGENTS.md` or a project-authored guidance file sits on a plain `- Text` bullet
  rather than inside a rule block
- **THEN** the marker-placement verification fails and reports the offending location

#### Scenario: A backticked prose mention is not a marker

- **WHEN** a guidance file mentions the token in prose as a backticked `` `captured:` ``
- **THEN** the marker-placement verification does not treat that mention as a marker

#### Scenario: A marker inside a fenced block is not judged

- **WHEN** a guidance file carries a `captured:` marker inside a fenced code block, as a command example or as the
  origin of a command block's comment guidance
- **THEN** the marker-placement verification does not judge that marker's placement

#### Scenario: Correctly placed markers pass

- **WHEN** every `captured:` marker in `AGENTS.md` and the project-authored guidance files lies within a rule block and
  nothing but its chain follows it
- **THEN** the marker-placement verification passes

#### Scenario: A marker followed by rule text fails the build

- **WHEN** a `captured:` marker sits inside a rule block with rule text of that block after it, so the marker reads as
  part of the sentence around it rather than as the rule's trailing provenance
- **THEN** the marker-placement verification fails and reports the offending location

#### Scenario: A misplaced marker in a project guidance file fails the build

- **WHEN** a `captured:` marker in a project-authored `.opencode/agent/*.md` or `.opencode/skills/*/SKILL.md` file sits
  on a plain `- Text` bullet rather than inside a rule block
- **THEN** the marker-placement verification fails and names that file and the offending location
