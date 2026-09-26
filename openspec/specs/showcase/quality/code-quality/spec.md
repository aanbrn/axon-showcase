# showcase/quality/code-quality Specification

## Purpose

Enforces the project's code style, static quality conventions, and structural shape through the build — the checks are
uniform and independent of any developer IDE, and the build also provides the commands that apply formatting (e.g.
`spotlessApply`, and the web module's `npmFormat`) — so a contributor never needs an IDE to make or verify a style
change, and a forbidden module dependency is caught where it is introduced rather than by a later audit.

## Requirements

### Requirement: Code style is enforced by the build

The build SHALL run a code-style check as part of the standard `check` task across all modules.

#### Scenario: Standard check runs the style check

- **WHEN** the standard `check` task runs
- **THEN** it includes a code-style check for every module

#### Scenario: Style violation fails the build

- **WHEN** a source file violates the configured style rules
- **THEN** the style check fails and reports the offending file and rule

#### Scenario: Style check runs without an IDE

- **WHEN** the style check runs on a machine with no IDE installed
- **THEN** it executes entirely within the Gradle build

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

### Requirement: License headers are enforced

Each Java source file SHALL carry the repository's license header, matching the MIT license declared in the LICENSE
file.

#### Scenario: Header present is accepted

- **WHEN** a source file carries the SPDX license header
- **THEN** the build accepts it

#### Scenario: Missing header fails the build

- **WHEN** a source file does not carry the SPDX license header
- **THEN** the formatting check fails on that file

### Requirement: Quality verification does not require an IDE

The standard `check` task SHALL verify all code-quality gates entirely within the Gradle build, so a contributor never
needs an IDE to validate a change.

#### Scenario: Standard check verifies every gate without an IDE

- **WHEN** the standard `check` task runs on a machine with no IDE installed
- **THEN** it runs all code-quality gates and reports success or violations

#### Scenario: Change is verified without opening an IDE

- **WHEN** a contributor verifies a change
- **THEN** `./gradlew check` is sufficient — no IDE step is required

### Requirement: GitHub workflows are linted by the build

The build SHALL lint every GitHub Actions workflow (`.github/workflows/*.yml`) with actionlint as part of the standard
`check` task, so workflow syntax and `run:` script errors surface locally instead of failing remotely after push. If
`actionlint` is not installed, the gate SHALL fail with a clear message naming the tool.

#### Scenario: Workflow lint runs in the standard check

- **WHEN** the standard `check` task runs
- **THEN** it lints every `.github/workflows/*.yml` file with actionlint

#### Scenario: Workflow syntax error fails the build

- **WHEN** a workflow file has a syntax error (e.g. malformed `on` or job definition)
- **THEN** the lint check fails and reports the offending workflow and line

#### Scenario: Broken run script fails the build

- **WHEN** a `run:` script in a workflow contains a shell error that shellcheck reports
- **THEN** the lint check fails and reports the offending script

#### Scenario: Missing actionlint is reported clearly

- **WHEN** the lint check runs on a machine without `actionlint` installed
- **THEN** it fails with a message naming `actionlint` as the required tool

### Requirement: The web module's formatting is applied by the build

The build SHALL provide a Gradle task that applies the web module's Prettier formatting (`prettier --write`), so a
contributor or agent formats the frontend with `./gradlew :showcase-web-ui:npmFormat` instead of invoking npm directly.
The task SHALL apply the same Prettier invocation the formatting check verifies (so the two agree), SHALL run entirely
within the Gradle build with no IDE required, and SHALL NOT be part of the standard `check` task — which verifies
formatting through the existing `npmFormatCheck` task instead.

#### Scenario: The format task applies Prettier

- **WHEN** a contributor runs the web module's format task
- **THEN** the module's sources are rewritten to Prettier's canonical form (matching the formatting check)

#### Scenario: Verification does not run the writing task

- **WHEN** the standard `check` task runs
- **THEN** it verifies formatting with the format-check task (`npmFormatCheck`) and does not invoke the writing format
  task

#### Scenario: Formatting runs without an IDE

- **WHEN** the format task runs on a machine with no IDE installed
- **THEN** it executes entirely within the Gradle build

### Requirement: The module dependency graph is enforced by the build

The build SHALL verify the modules' declared project dependencies against the structure's sanctioned shape and fail
`check` when a module acquires a forbidden edge, naming the offending edge. The forbidden edges SHALL be: a module
depending on a service application (`showcase-command-service`, `showcase-query-service`, `showcase-projection-service`,
`showcase-api-gateway`); an `-extension` module depending on anything other than `platform` and the `showcase-test`
dependency; and a contract module (`showcase-command-api`, `showcase-query-api`, `showcase-projection-model`,
`showcase-query-proto`) depending on a `-client` module or a service application. The verification SHALL inspect each
module's production source sets — `main`, which ships, and `testFixtures`, which other modules consume as an artifact;
test suites are deliberately out of scope, since a suite's edges reach no artifact and a suite depending on a service
application is a legitimate way to exercise it.

#### Scenario: A forbidden edge fails the build

- **WHEN** a module declares a dependency on a service application, an `-extension` gains a non-platform dependency, or
  a contract module depends on a `-client` or a service application
- **THEN** the verification fails and reports the offending edge (its source and target modules), so the drift is caught
  where it is introduced rather than by a later audit

#### Scenario: The current graph passes

- **WHEN** the verification runs against the repository's modules as they stand
- **THEN** it passes, because every rule asserts a property the graph already has — the check is containment, not new
  capability

#### Scenario: A dependency's kind and scope are respected

- **WHEN** the verification walks a module's dependencies
- **THEN** it inspects every declaration configuration of that module's production source sets (`main` and
  `testFixtures`), excluding the `platform` BOM by its declared target, and ignores any edge whose source and target are
  the same module when deciding violations — neither is a structural dependency between modules

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

### Requirement: The web module's import boundaries are enforced by the build

The web module's Feature-Sliced structure SHALL be enforced by the standard check: a layer SHALL import only from layers
below it (`app` > `pages` > `widgets` > `features` > `entities` > `shared`), a slice SHALL NOT import a sibling slice
except through an explicitly declared `@x` cross-import API, every cross-slice import SHALL target the target slice's
public API rather than its internals, and a source file that matches no layer or slice SHALL be rejected. The ignored
out-of-slice files — the composition entry, the test setup, the type declarations, and test files — are outside the
layer graph and excluded.

#### Scenario: A deep cross-slice import is rejected

- **WHEN** a module imports a file inside another slice rather than that slice's public API
- **THEN** the lint check fails on that import

#### Scenario: An upward-layer import is rejected

- **WHEN** a module imports from a layer above its own
- **THEN** the lint check fails on that import

#### Scenario: An undeclared sibling-slice import is rejected

- **WHEN** a module imports a sibling slice without a declared `@x` cross-import API
- **THEN** the lint check fails on that import

#### Scenario: A source file outside the layer graph is rejected

- **WHEN** a source file matches no layer or slice and is not one of the ignored out-of-slice files
- **THEN** the lint check fails on that file

#### Scenario: A public-API downward import is accepted

- **WHEN** a module imports a lower slice's public API
- **THEN** the lint check accepts it

#### Scenario: A declared cross-import is accepted

- **WHEN** a module imports a sibling through the target's declared `@x` cross-import API
- **THEN** the lint check accepts it

#### Scenario: The ignored out-of-slice files are accepted

- **WHEN** the composition entry, the test setup, a type declaration, or a test file imports across boundaries
- **THEN** the lint check accepts it

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
