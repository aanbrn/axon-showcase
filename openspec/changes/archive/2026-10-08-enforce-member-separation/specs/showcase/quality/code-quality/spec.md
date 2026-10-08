# Spec Delta

## ADDED Requirements

### Requirement: Member separation is enforced by the build

Consecutive member declarations in a type body SHALL be separated by a blank line, so the build rejects two members that
are adjacent with no blank line between them. When the first of the two is an enum's constant, an annotation type's
declaration, or an annotation type's member, no blank line is required after it.

#### Scenario: Two adjacent members with no blank line are rejected

- **WHEN** two members of a type body are adjacent with no blank line between them, and the first is not an enum
  constant, an annotation type declaration, or an annotation type member
- **THEN** the style check fails and reports the offending file

#### Scenario: A blank line between members satisfies the separation

- **WHEN** consecutive members are separated by a blank line
- **THEN** the style check accepts them

#### Scenario: No blank line is required after an enum constant or an annotation type's declaration or member

- **WHEN** the first of two adjacent members is an enum's constant, an annotation type's declaration, or an annotation
  type's member
- **THEN** the style check accepts them
