## MODIFIED Requirements

### Requirement: Source formatting is enforced by the build

The build SHALL format Java and Kotlin DSL (`.gradle.kts`) sources to a canonical style — including removal of unused
imports — and verify formatting as part of the standard `check` task, with no IDE required. The build SHALL also format
build-logic Kotlin (`build-logic/src/**/*.kt`) with ktfmt matching the Gradle-DSL style, root markdown (`docs/`,
`AGENTS.md`, `README.md`, `SECURITY.md`, `openspec/specs/`, active `openspec/changes/*/`, the `.github/` markdown, and
the project-authored `.opencode/` markdown — the agent definitions, the project-authored commands, and the project
skills) with Prettier at `printWidth: 120` with `proseWrap: "always"`, and the project-owned `.opencode/opencode.json`
with Prettier at `printWidth: 120`, and verify each in `check` — ending the manual 120-char wrapping convention. The
`openspec/changes/archive/` historical record is not reformatted, and neither are the generated OpenSpec instruction
files (the six `opsx-*` commands — the project-authored `opsx-tool-update.md` is in scope — and the `openspec-*` skills)
nor the vendored `axon4to5-*` skills, which are copied verbatim from upstream.

#### Scenario: Formatting check runs in the standard check

- **WHEN** the standard `check` task runs
- **THEN** it includes a formatting check for every module

#### Scenario: Unformatted source fails the build

- **WHEN** a source file does not conform to the canonical formatting
- **THEN** the formatting check fails and reports the offending file

#### Scenario: Unused import fails the build

- **WHEN** a Kotlin DSL (`.gradle.kts`) source file contains an unused import
- **THEN** the formatting check fails and reports the offending file

#### Scenario: Formatting runs without an IDE

- **WHEN** the formatting check runs on a machine with no IDE installed
- **THEN** it executes entirely within the Gradle build

#### Scenario: Build-logic Kotlin is format-gated

- **WHEN** a `build-logic/src/**/*.kt` file does not conform to the ktfmt style
- **THEN** the formatting check fails and reports the offending file

#### Scenario: Unformatted markdown fails the build

- **WHEN** a file in the markdown scope (e.g. under `docs/`, `AGENTS.md`, `README.md`, `SECURITY.md`, `openspec/specs/`,
  an active `openspec/changes/*/`, the `.github/` markdown, or the project-authored `.opencode/` markdown) is not
  Prettier-formatted
- **THEN** the root markdown formatting check fails and reports the offending file

#### Scenario: Archived change markdown is not reformatted

- **WHEN** the root markdown formatting check runs
- **THEN** it does not check `openspec/changes/archive/` (the historical record is left as recorded)

#### Scenario: Project-authored .opencode markdown is format-gated

- **WHEN** a project-authored `.opencode/` markdown file — an agent definition, a project-authored command, or a project
  skill — is not Prettier-formatted
- **THEN** the markdown formatting check fails and reports the offending file

#### Scenario: Generated and vendored .opencode files are not reformatted

- **WHEN** the markdown formatting check runs
- **THEN** it does not check the generated `opsx-*` commands (except the project-authored `opsx-tool-update.md`), the
  generated `openspec-*` skills, or the vendored `axon4to5-*` skills

#### Scenario: The .opencode configuration JSON is format-gated

- **WHEN** `.opencode/opencode.json` is not Prettier-formatted
- **THEN** the JSON formatting check fails and reports the file
