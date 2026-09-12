## MODIFIED Requirements

### Requirement: Line length limit matches the project convention

Source lines SHALL NOT exceed 120 characters, matching the project's documented wrapping convention.

#### Scenario: Over-long line is rejected

- **WHEN** a source line exceeds 120 characters
- **THEN** the style check fails on that line

#### Scenario: Boundary line is accepted

- **WHEN** a source line is exactly 120 characters
- **THEN** the style check accepts it

### Requirement: Naming and import conventions are enforced

Type, method, and constant naming and import hygiene SHALL follow the project's conventions, including the test-tier
suffixes (`Tests`, `CT`, `IT`, `E2E`).

#### Scenario: Non-conforming type name is rejected

- **WHEN** a type name does not conform to the configured naming rules
- **THEN** the style check fails on that type

#### Scenario: Test-tier suffix is accepted

- **WHEN** a test class uses one of the project's test-tier suffixes (`Tests`, `CT`, `IT`, `E2E`)
- **THEN** the style check accepts the type name
