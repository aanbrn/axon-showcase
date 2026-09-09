## 1. Add the actionlint gate

- [x] 1.1 Add a `workflow-lint-conventions.gradle.kts` in `build-logic` registering a `workflowLint` `Exec` task that
      lints `.github/workflows/*.yml` with `actionlint` (resolved from PATH like `snykExecutable()`/`packCli()`), and
      wire it into the root `check`.
- [x] 1.2 Run `./gradlew workflowLint` and confirm it lints all workflows.
- [x] 1.3 Add an actionlint install step to `ci.yml` (GitHub-hosted runners don't ship it), so the `check`-wired gate
      passes in CI — use actionlint's official download script (positional args `latest <dir>`, `<dir>` pre-created),
      which works on `ubuntu-latest` and makes the executable available to the `workflowLint` task.

## 2. Verify

- [x] 2.1 Run `./gradlew check` (or `workflowLint` + spotless) and confirm all gates pass.
- [x] 2.2 Run `openspec validate --all` and confirm the change passes.
- [x] 2.3 Update `AGENTS.md` and `README.md` — add `actionlint` to the Prerequisites lists and note the workflow-lint
      gate.
- [x] 2.4 Confirm the `ci.yml` install step and the new gate both run green in a CI run.

> The two dead `created=` assignments actionlint found were fixed separately in PR #92 (fix-workflow-unused-created), so
> the workflows are already clean when this gate lands.
