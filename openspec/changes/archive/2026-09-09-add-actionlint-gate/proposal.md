# Proposal: Lint GitHub Actions workflows with actionlint

## Why

The GitHub workflows (`.github/workflows/*.yml`, 6 files) are the only YAML population without a local verification
gate. Today they are validated only by GitHub itself **after push**: a workflow syntax error fails the run remotely with
a confusing "workflow failed to render" instead of a clean `check` failure. Worse, nothing catches semantic errors in
`run:` scripts — an exploration run of actionlint immediately found two genuinely dead variables
(`created="$(gh issue create ...)"` in `dependency-updates.yml` and `helm-updates.yml`, assigned but never read) that
the workflows have carried unnoticed. This adds an actionlint gate to `check`, matching the repo's "verify locally, not
at the remote" convention (the same rationale that gates Java/markdown/Kotlin formatting and the exec tools).

## What Changes

- Add an **actionlint** gate to the build that lints all `.github/workflows/*.yml` files, wired into `check`. It follows
  the existing exec-tool convention (`docker`/`pack`/`snyk`): a Gradle `Exec` task resolving the `actionlint` binary's
  absolute path from `System.getenv("PATH")` (avoiding the daemon JVM's frozen PATH cache), failing the build with a
  clear message when `actionlint` is not installed.
- actionlint validates workflow syntax (`on`, `jobs`, `runs-on`, `needs`, `env`, `permissions`), action references, and
  shells out to shellcheck for `run:` scripts — so both the YAML shape and the embedded shell are checked.
- **Prerequisite**: `actionlint` becomes a build-time tool like `helm`/`snyk`/`pack` (not a Gradle plugin or downloaded
  artifact) — documented in `AGENTS.md`'s Prerequisites.

## Capabilities

### New Capabilities

- None (this is tooling, not a runtime/deployment behavior).

### Modified Capabilities

- `showcase/quality/code-quality` — the verification gate grows to include GitHub workflow linting.

## Impact

- **Build**: a `workflowLint` `Exec` task in a build-logic convention, wired into `check`; fails with a helpful message
  if `actionlint` is missing.
- **CI**: an actionlint install step added to `ci.yml` (GitHub-hosted runners don't ship it), so the `check`-wired gate
  passes in CI.
- **Code**: the two dead `created=` assignments actionlint found were already fixed separately in PR #92
  (fix-workflow-unused-created); no workflow changes in this change.
- **Docs**: `AGENTS.md` and `README.md` — add `actionlint` to the Prerequisites lists; note the gate in `AGENTS.md`'s
  CI/verification section.
- **Behavior**: no runtime change; CI gates workflow correctness locally.
