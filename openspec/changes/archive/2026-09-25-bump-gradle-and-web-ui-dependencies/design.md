# Design

## Context

See `proposal.md` — Why. The `dependency-updates` tracker (issue #13) reports the Gradle wrapper update and ten in-range
web UI npm updates. Its catalog section is empty, and the root is a shared premise rather than this bump: the build's
report labels its catalog candidates under `have later milestone versions:` (the plugin's section for its unset default
`revision`), while the tracker's extraction, `merge-governance`'s "no stable catalog updates" scenario, and
`.opencode/commands/dependency-updates.md` all key on a catalog `newer versions` section the report never emits — so a
stable row such as `caffeine [3.2.4 -> 3.3.0]` reads as non-actionable and no catalog update reaches the issue. That gap
is pre-existing and out of scope for this bump (parked in `docs/ideas.md`). The web UI's npm report is the newly added
`npmOutdated` check, and the web UI has no major-suppression mechanism (unlike the JVM's
`config/dependency-updates/major-disabled.properties`), so that report lists the majors too.

## Goals / Non-Goals

**Goals:**

- Take the in-range updates: the Gradle wrapper `9.8.0` (keeping the `-all` distribution) and the ten npm
  patches/minors.
- Keep the app's declared dependency majors unchanged (each bump stays within its caret range).

**Non-Goals:**

- The nine npm majors (`react`/`react-dom` 19, `@types/react(-dom)` 19, `@vitejs/plugin-react` 6, `vite` 8, `vitest` 5,
  `jsdom` 30, `typescript` 7) — each a migration.
- Adding a web UI major-suppression mechanism, or fixing the tracker's catalog extraction: both are separate concerns
  (parked in `docs/ideas.md`), and until a suppression mechanism (or the migration) lands the majors keep re-appearing
  in the weekly tracker.

## Decisions

- **Take the npm updates with `npm update`.** All ten are inside their declared caret ranges (`wanted == latest`), so a
  single `npm update` moves exactly that set and rewrites `package-lock.json` — `package.json` is unchanged. `npm`
  resolves the transitive tree too, so the lockfile also moves transitive packages, including transitives eslint 10.11
  now requires at a new major (`file-entry-cache` 8 → 11, `flat-cache` 4 → 6, `keyv` 4 → 5), and fills in npm 11's
  `license` fields as normalization. Rejected: installing each at `@latest`, which would cross major lines for the
  deferred set.
- **Bump the Gradle wrapper with two `wrapper` runs.**
  `./gradlew wrapper --gradle-version 9.8.0 --distribution-type all` first rewrites `gradle-wrapper.properties`
  (preserving the `-all` distribution); re-running it under `9.8.0` then regenerates the wrapper jar and
  `gradlew`/`gradlew.bat` at the new version — a single run leaves them stamped with the running (old) version.
- **Defer the majors.** They cross major lines and need a migration; without a web UI suppression mechanism they remain
  listed in the tracker.

## Risks / Trade-offs

- **A minor bump can still change lint/format behavior** (`eslint` 10.9 → 10.11, `prettier` 3.9.6 → 3.9.9) → run
  `:showcase-web-ui:check` (lint, format-check, Vitest).
- **Gradle `9.8.0` may surface deprecations** (the repo already logs gradle-helm-plugin deprecation warnings) → run the
  Docker-free `check` and read the output.

## Migration Plan

- Apply: the wrapper task and `npm update`, plus the docs refresh; no code change to sequence.
- Rollback: revert the wrapper files and `package-lock.json`.
