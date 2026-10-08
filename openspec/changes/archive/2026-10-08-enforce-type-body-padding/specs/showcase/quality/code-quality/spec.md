# Spec Delta

## ADDED Requirements

### Requirement: Type body padding is enforced by the build

A type body whose opening brace ends its line SHALL be followed by a blank line or a comment, so its first member is
never glued to the brace. An enum body, an annotation-type body, and an empty body SHALL be exempt.

#### Scenario: A first member glued to the opening brace is rejected

- **WHEN** a type body's opening brace ends its line and the next line is an annotation or a member with neither a blank
  line nor a comment
- **THEN** the style check fails and reports the offending file

#### Scenario: A blank line or a comment satisfies the padding

- **WHEN** the next line after the opening brace is a blank line, or a line comment, block comment, or Javadoc
- **THEN** the style check accepts it

#### Scenario: Enum, annotation-type, and empty bodies are exempt

- **WHEN** the body is an enum body, an annotation-type body, or an empty body
- **THEN** the style check accepts it
