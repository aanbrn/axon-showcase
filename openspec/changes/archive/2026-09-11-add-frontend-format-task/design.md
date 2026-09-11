## Context

`frontend-conventions` registers `npmFormatCheck` (npm `format:check` → `prettier --check .`, gated in `check`) but no
write task, while `showcase-web-ui/package.json` already has `format` (`prettier --write .`). The JVM side's canonical
format command is `./gradlew spotlessApply`; the frontend has no counterpart, so applying Prettier means running `npm`
inside the module. `AGENTS.md`'s Frontend convention says "Format with Prettier" without naming a command, and the
README advises preferring Gradle tasks over raw npm invocations.

## Goals / Non-Goals

**Goals:**

- One Gradle command formats the web module, matching what the formatting check verifies.
- The command is documented where contributors and agents look (`AGENTS.md`, `README.md`).

**Non-Goals:**

- Changing the gates (Prettier/ESLint remain the source of truth) or what `check` runs.
- Adding an OpenCode command/skill trigger — the Gradle task is the entry point.

## Decisions

**D1: Wrap the existing `format` npm script.** The task runs `npm run format` (via the existing `NpmTask`), so
`package.json` stays the single source of the Prettier invocation; the Gradle task adds the conventional entry point,
not a second formatting configuration. _Alternative considered:_ invoking `prettier` directly from Gradle (rejected —
duplicates the script and can drift from `prettier --write .`).

**D2: Name it `npmFormat`.** The existing tasks are `npmCi`/`npmBuild`/`npmLint`/`npmFormatCheck`/`npmTest`; `npmFormat`
is the obvious write counterpart and keeps the naming scheme.

**D3: Keep it out of `check`, and never up-to-date.** `check` verifies formatting through `npmFormatCheck`; a writing
task must not run during verification. The task declares no `outputs`, so it always executes (a format task cannot be
skipped as up-to-date).

**D4: Document it with the existing formatting guidance.** The task is a developer/agent entry point, so it goes into
`AGENTS.md`'s Formatting/Frontend conventions and the README rather than a new section.

## Risks / Trade-offs

- [The task rewrites sources, so it must never run during verification] → it is not wired into `check` and declares no
  `outputs` (never skipped as up-to-date); it wraps the module's own `format` script, which respects `.prettierignore`.
