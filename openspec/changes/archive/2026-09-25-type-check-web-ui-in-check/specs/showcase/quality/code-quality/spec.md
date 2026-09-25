# code-quality — Delta

## ADDED Requirements

### Requirement: The web UI is type-checked by the build

The build SHALL type-check the web UI (`tsc --noEmit`) as part of the standard `check` task, so a `tsconfig.json` or
source type error fails `check` rather than only the bundle build (`npmBuild`, under `assemble`). Building the
production bundle SHALL remain on `assemble`.

#### Scenario: A type error fails the standard check

- **WHEN** a web UI source file or `tsconfig.json` contains a type error
- **THEN** the standard `check` task fails and reports the TypeScript diagnostics

#### Scenario: The bundle build stays on assemble

- **WHEN** the standard `check` task runs
- **THEN** it type-checks the web UI without building the production bundle
