# Tasks

## 1. Apply the bumps

- [x] 1.1 Bump the JVM catalog pins in `gradle/libs.versions.toml`: `lz4-java` 1.12.0, `logback` 1.6.4, `zstd-jni`
      1.5.7-20, `nullaway` 0.14.2, `spotless-plugin` 8.10.3. Verify by reading the catalog.
- [x] 1.2 Bump the web UI npm dependencies in `showcase-web-ui/package.json` (`@reduxjs/toolkit` 2.13.0,
      `@tanstack/react-query` 5.104.0, `@types/node` 26.6.3,
      `@typescript-eslint/eslint-plugin`/`parser`/`typescript-eslint` 8.71.0, `react-hook-form` 7.89.0) and reconcile
      the lockfile with a clean `npm install`. Verify with `npm outdated` (only the deferred `typescript` 7 remains).

## 2. Verification

- [x] 2.1 Run the full `./gradlew check` (with integration tests — Docker is required) and confirm it passes; a
      runtime-path bump can be binary-incompatible while the Docker-free check passes.
- [x] 2.2 Run `./gradlew :showcase-web-ui:check` (lint, format, type-check, Vitest) after the npm bump.
- [x] 2.3 Read the lockfile diff and describe it by what moved (the direct bumps plus the `@typescript-eslint` family
      and any transitive the clean reinstall reconciled), not as "patches/minors"; confirm no unexpected major moved.
      Done: the direct bumps plus the @typescript-eslint family (8.70.1→8.71.0), patch-level transitive reconciliations
      (e.g. @csstools/css-tokenizer 4.0.1→4.0.2, tldts 7.4.15→7.4.16), and npm re-hoisting (picomatch/ansi-styles
      nesting); no major moved, and the three `extraneous` entries the clean install re-added (ajv, fast-uri,
      json-schema-traverse) were pruned with `npm prune`.
- [x] 2.4 Run the implementation `review-quick` loop over the diff; fix its findings and re-run until it reports nothing
      new.
- [x] 2.5 Run the per-unit `lesson-capture` over this change and apply its durable proposals. Done: one addition to the
      Formatting convention (a `spotless-plugin` bump reflows sources), +3 lines.
- [x] 2.6 Run `./gradlew spotlessApply` after the last edit and confirm `spotlessCheck` passes; run the manual
      120-character check over the lines this change introduces in the non-formatter-owned files
      (`gradle/libs.versions.toml`, `showcase-web-ui/package.json`/`package-lock.json`, the change dir's
      `.openspec.yaml`) — the pre-existing long lines in the catalog are not this change's.
- [x] 2.7 Request the user's manual review pass — the step before committing.
