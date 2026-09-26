## ADDED Requirements

### Requirement: The web module's naming conventions are enforced by the build

Shipped web-UI source SHALL follow the module's naming conventions, and the lint check SHALL reject a violation: an
uppercase-leading function is `StrictPascalCase` and an uppercase-leading variable is `UPPER_CASE` or
`StrictPascalCase`; a lowercase-leading function or variable is `strictCamelCase`; a parameter is `strictCamelCase`, and
an unused one may carry a single leading underscore; a type alias, interface, or type parameter is `StrictPascalCase`; a
function that uses a hook is named `use*`; and a unit test file under `src` is named `*.test.ts(x)` so the Vitest runner
collects it (the Playwright `e2e/` suite keeps its own `.spec.ts` convention).

#### Scenario: An uppercase-leading identifier in neither form is rejected

- **WHEN** an uppercase-leading function is not `StrictPascalCase`, or an uppercase-leading variable is neither
  `UPPER_CASE` nor `StrictPascalCase`
- **THEN** the lint check fails on that identifier

#### Scenario: A lowercase-leading identifier not in strictCamelCase is rejected

- **WHEN** a lowercase-leading function or variable name does not follow `strictCamelCase`
- **THEN** the lint check fails on that identifier

#### Scenario: A parameter not in strictCamelCase is rejected

- **WHEN** a parameter name does not follow `strictCamelCase` (including a used `_`-prefixed one)
- **THEN** the lint check fails on that identifier

#### Scenario: A type name not in StrictPascalCase is rejected

- **WHEN** a type alias, interface, or type parameter name does not follow `StrictPascalCase`
- **THEN** the lint check fails on that identifier

#### Scenario: A hook used outside a `use*` function is rejected

- **WHEN** a function that is not named `use*` calls a hook
- **THEN** the lint check fails on that call

#### Scenario: A unit test file the Vitest runner will not collect is rejected

- **WHEN** a unit test file under `src` is named `*.spec.ts` or `*.spec.tsx`
- **THEN** the lint check fails on that file

#### Scenario: Conforming names, an unused `_` parameter, and a test file are accepted

- **WHEN** identifiers follow their conventions, an unused parameter is named `_`, and a test file is named
  `*.test.ts(x)`
- **THEN** the lint check accepts them
