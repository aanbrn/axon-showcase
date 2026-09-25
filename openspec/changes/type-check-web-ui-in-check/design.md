# Design

## Context

See `proposal.md` — Why. Current state that shapes the approach:

- `frontend-conventions.gradle.kts` registers the frontend's npm tasks as `NpmTask`s (dependency install, build, lint,
  format and format-check, test, the outdated/audit reports, the dev server, and the e2e suite); `check` depends on
  `npmLint`/`npmFormatCheck`/`npmTest`, and `assemble` depends on `npmBuild`.
- `npmBuild` runs `npm run build` = `tsc && vite build`; `tsc` uses `tsconfig.json`'s `noEmit: true`, so the type-check
  and the bundle build are coupled in one script.
- A type-check alone is cheap: `tsc --noEmit` over `showcase-web-ui` takes ~1.1 s on the pinned Node/TypeScript.
- The `code-quality` spec owns the build gates (code style, line length, naming, formatting, license headers, workflow
  lint, module graph), but no requirement covers type-checking the web UI, and none enumerates the frontend `check`'s
  members.

## Goals / Non-Goals

**Goals:**

- Type-check the web UI in the standard `check` (the PR gate), so a type error fails the merge gate.
- Keep the bundle build in `assemble`; keep the check in-build with no IDE.

**Non-Goals:**

- Building the bundle in `check` (the deployment artifact stays on `assemble`).
- Any JVM-side change (javac already type-checks the JVM modules); any UI behavior change.

## Decisions

- **A dedicated `npmTypeCheck` task, not `npmBuild` in `check`.** Wiring `npmBuild` in would run `vite build` on every
  `check` (slower, and it produces the deployment bundle), whereas the gate needs only `tsc --noEmit`. Rejected:
  `dependsOn(npmBuild)` in `check`.
- **Add a `typecheck` npm script (`tsc --noEmit`).** It keeps the invocation single-sourced (the task runs
  `npm run typecheck`) and leaves `npmBuild`'s `tsc` untouched. Rejected: invoking `tsc` inside the Gradle task, which
  would split the frontend command surface between `package.json` and build-logic.
- **Add a `code-quality` requirement.** No existing requirement covers type-checking the web UI, so the gate is new
  behavior the corpus should state. Rejected: `skip_specs` (it adds a gate, not a version bump).

## Risks / Trade-offs

- **`check` slows by ~1 s** → negligible against the existing frontend tasks.
- **A pre-existing type error would now fail `check`** → the tree is clean today (`tsc --noEmit` passes), and the
  change's positive control exercises the failure path.
- **The `typecheck` script and `npmBuild`'s `tsc` could drift** (e.g. one gains `--noEmit` flags) → keep the shared
  `tsconfig.json` as the single configuration; the script is a thin `tsc --noEmit`.

## Migration Plan

- Apply: add the script and the task, wire it into `check`.
- Rollback: revert the two edits (the task and script are additive).
