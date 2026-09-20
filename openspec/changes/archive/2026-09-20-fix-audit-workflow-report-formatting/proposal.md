# Fix the scheduled audit workflow's unformatted report

## Why

The `audit` workflow shipped in #320 produces a report that fails its own pull request's `build`: CI runs
`spotlessCheck` over `docs/**/*.md`, and the agent commits `docs/audits/<date>.md` unformatted, so PR #321's `build`
failed at `:spotlessMarkdownCheck` (114 lines of Prettier re-wrapping). The workflow's first real run therefore opened a
PR that could not pass CI — the deferred verification in #320 was a dispatch that checked the run succeeded, not that
its output passed the build.

## What Changes

- **`.github/workflows/audit.yml`** — provision `actions/setup-java@v6` (Temurin 21) and
  `gradle/actions/setup-gradle@v6` before the action step, as the sibling Gradle-running workflows do, and restore the
  prompt's `./gradlew spotlessApply` instruction so the committed report is formatted. (An earlier revision removed the
  instruction to resolve a missing-JDK finding; the correct fix is to provision the JDK and keep it, since the audit PR
  does get a `build` check.)

## Impact

- **Build**: none — the workflow file only.
- **Tests**: none.
- **Workflow**: audit PRs now pass their own `build`; the run costs a Gradle setup and a `spotlessApply` invocation.

## New Capabilities

None.

## Modified Capabilities

None — the workflow's behavior and the spec requirements it satisfies are unchanged; only its environment provisioning
and prompt are corrected, so this change is `skip_specs`.
