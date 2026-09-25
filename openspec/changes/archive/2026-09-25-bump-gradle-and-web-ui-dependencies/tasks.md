# Tasks

## 1. Implementation

- [x] 1.1 Bump the Gradle wrapper: run `./gradlew wrapper --gradle-version 9.8.0 --distribution-type all` **twice** —
      the first run rewrites `distributionUrl`, the second (running under `9.8.0`) regenerates the wrapper jar and
      `gradlew`/`gradlew.bat`. Verify `./gradlew --version` reports `9.8.0`, `gradle-wrapper.properties` keeps the
      `-all` distribution (`gradle-9.8.0-all.zip`), and the changes are confined to the wrapper files
      (`gradle-wrapper.properties`, `gradle-wrapper.jar`, `gradlew`, `gradlew.bat`). — Done: `Gradle 9.8.0`;
      `distributionUrl` is `gradle-9.8.0-all.zip`; all four wrapper files changed.
- [x] 1.2 Bump the ten in-range npm packages: `cd showcase-web-ui && npm update`, then confirm
      `showcase-web-ui/build/npm-outdated.txt` (via `./gradlew :showcase-web-ui:npmOutdated`) no longer lists
      `@playwright/test`, `@tanstack/react-query`, `@types/node`, `@typescript-eslint/eslint-plugin`,
      `@typescript-eslint/parser`, `typescript-eslint`, `eslint`, `prettier`, `react-hook-form`, or `zod` — while the
      nine majors remain listed — and that only `package-lock.json` changed (not `package.json`). — Done: only
      `package-lock.json` changed (which also gains npm 11's `license` fields as normalization); the report now lists
      just the nine majors.
- [x] 1.3 `.opencode/commands/gradle-update.md`: make the prescribed `./gradlew wrapper --gradle-version=<latest>`
      invocation pass `--distribution-type all` **and** run the `wrapper` task twice (as task 1.1) — a single run leaves
      the wrapper jar and `gradlew`/`gradlew.bat` at the old version, and without the flag it flips `-all` to `-bin`.
      Verify by reading the command that the invocation carries the flag and runs twice. — Done: the command prescribes
      both.

## 2. Docs

- [x] 2.1 Refresh the pinned Gradle version to `9.8.0` in `AGENTS.md`, `README.md`, and `openspec/config.yaml`'s
      `context:` block. Verify `grep -rn "9\.7\.1" AGENTS.md README.md openspec/config.yaml` returns nothing. — Done:
      all three say `9.8.0`; the grep is clean.

## 3. Verification

- [x] 3.1 Run `./gradlew :showcase-web-ui:check` (lint, format-check, Vitest) and `./gradlew :showcase-web-ui:build`
      (`tsc && vite build`, the type-check path `check` does not run) — both pass against the bumped npm set. — Done:
      both BUILD SUCCESSFUL.
- [x] 3.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` — it passes against Gradle `9.8.0`; read the
      output for any new deprecation warning. — Done: BUILD SUCCESSFUL (352 tasks); `--warning-mode all` shows the
      pre-existing `Project.getProperties()` plus the `Configuration.visible`/`setVisible` deprecation emitted by
      several third-party plugins (all plugin-side; recorded in the `AGENTS.md` helm-plugin gotcha). (A first run failed
      on `spotlessMarkdownCheck` for the unformatted command edit; `spotlessApply` cleared it.)
- [x] 3.3 Run `openspec validate --changes` and `./gradlew spotlessApply` followed by `./gradlew spotlessCheck`; both
      pass. The change-dir markdown and `AGENTS.md`/`README.md` are markdown-formatter-gated; `openspec/config.yaml` is
      not, so check it manually (`perl -CSD -lne 'print if length > 120'`); `package.json` is Prettier-gated but
      untouched, and `package-lock.json` is in `showcase-web-ui/.prettierignore`, so no manual pass is owed for either.
      — Done: validate and spotless pass; the `config.yaml` manual check is clean.
- [x] 3.4 Confirm the tracker's actionable in-range set is cleared: read `./gradlew dependencyUpdates` (the Gradle
      wrapper section shows `UP-TO-DATE`) and `:showcase-web-ui:npmOutdated` (only the nine deferred majors remain). —
      Done: `Gradle: [9.8.0: UP-TO-DATE]`; the npm report lists exactly the nine majors.
