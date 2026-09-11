## Why

The JVM side has one canonical format command — `./gradlew spotlessApply` — but the web module has only a _check_
(`:showcase-web-ui:npmFormatCheck`, gated in `check`). To apply Prettier a contributor must remember to run
`npm run format` inside `showcase-web-ui/`, and `AGENTS.md` names only the `format:check` script there, not a Gradle
command. The convention is to lead with Gradle tasks over raw `npm`/`docker` invocations, so the frontend is missing its
`spotlessApply` counterpart — a gap that also leaves agents without a first-class way to format the web module.

## What Changes

- Add a `npmFormat` Gradle task to `frontend-conventions` that wraps the module's existing `format` npm script
  (`prettier --write .`), so `./gradlew :showcase-web-ui:npmFormat` formats the web module.
- Document the task in `AGENTS.md` (the Frontend/Formatting conventions) and `README.md`, framing it as the frontend
  analog of `spotlessApply`.
- No change to the existing gates (`npmFormatCheck`/`npmLint`) or to what `check` runs.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/code-quality`: add a requirement that the build provides a task that _applies_ the web module's
  Prettier formatting (the write counterpart to the existing formatting check), runnable without an IDE and outside
  `check`.

## Impact

- **Build**: `build-logic/src/main/kotlin/frontend-conventions.gradle.kts` (new `npmFormat` task).
- **Docs**: `AGENTS.md`, `README.md`.
- **Spec**: `showcase/quality/code-quality` delta.
- **Out of scope**: replacing Prettier or ESLint (they remain the gates), changing the `check` task, or adding an agent
  command/skill for formatting.
