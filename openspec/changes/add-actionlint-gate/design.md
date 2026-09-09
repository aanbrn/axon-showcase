## Context

The repo's exec-tool conventions (`docker`, `pack`, `snyk`) resolve the tool's absolute path from
`System.getenv("PATH")` and pass it in the `commandLine`, because the daemon JVM caches PATH for native process spawning
at daemon start (ignoring later changes). The same pattern applies to `actionlint`. The GitHub workflows (6 files) are
currently verified only by GitHub after push; actionlint adds a local gate.

## Goals / Non-Goals

**Goals:**

- Lint all `.github/workflows/*.yml` with actionlint, wired into `check`, following the repo's exec-tool convention.
- Document `actionlint` as a build prerequisite.

**Non-Goals:**

- No Gradle plugin or downloaded binary — `actionlint` is a local tool like `helm`/`snyk`/`pack`.
- No changes to the workflows' behavior (the dead `created=` fixes shipped separately in PR #92).
- No YAML formatting (see the explored-and-rejected YAML formatting decision) — linting correctness, not style.

## Decisions

### D1: A build-logic convention registering an actionlint Exec task

Add a small convention in `build-logic` (e.g. `workflow-lint-conventions.gradle.kts` applied to the root project) that
registers an `Exec` task `workflowLint`:

```kotlin
tasks.register<Exec>("workflowLint") {
    group = "verification"
    description = "Lints GitHub Actions workflows with actionlint"
    workingDir = rootProject.layout.projectDirectory.asFile
    val workflowFiles = fileTree(".github/workflows") { include("*.yml") }
    inputs.files(workflowFiles)
    outputs.upToDateWhen { true } // fast, no output dir; correctness gate
    commandLine(actionlintExecutable(), *workflowFiles.files.map { it.absolutePath }.toTypedArray())
    doFirst {
        if (actionlintExecutable().isEmpty()) {
            throw GradleException(
                "actionlint is required to lint GitHub workflows. Install it from https://github.com/rhysd/actionlint (brew, go install, or a release binary)"
            )
        }
    }
}

fun actionlintExecutable(): String {
    // resolve absolute path from System.getenv("PATH"), same as snykExecutable()/packCli()/dockerCli()
}
```

`check` depends on `workflowLint` via the convention (or the root `check` task's dependencies), so the gate runs locally
and in CI.

### D2: The dead `created=` assignments are already fixed

The two dead `created="$(gh issue create ...)"` assignments actionlint found (in `dependency-updates.yml` and
`helm-updates.yml`) were fixed separately in PR #92 (`fix-workflow-unused-created`): the assignment was replaced with a
bare `gh issue create ...`, preserving the side effect. So when this gate lands, the workflows are already clean — no
workflow changes are part of this change.

### D3: Prerequisite documentation

Add `actionlint` to the Prerequisites lists in both `AGENTS.md` and `README.md` (alongside `pack`/`snyk`/`helm`), and
mention the `workflowLint` gate in the CI section of `AGENTS.md`.

## Risks / Trade-offs

- **Tool availability** → `actionlint` must be installed locally and on CI. GitHub-hosted runners don't ship it, so
  `ci.yml` gains an install step using actionlint's official **download script**
  (`bash <(curl https://raw.githubusercontent.com/rhysd/actionlint/main/scripts/download-actionlint.bash)`, which works
  on ubuntu-latest and prints the executable path) alongside the existing openspec CLI install — see task 1.3.
- **shellcheck dependency** → actionlint shells out to shellcheck for `run:` script linting. This is the part that found
  real bugs (the dead vars); keep it on. shellcheck is pre-installed on GitHub-hosted ubuntu runners; locally it ships
  with the Homebrew actionlint formula or as its own package.
- **False positives** → actionlint is mature and low-noise; any new findings are real workflow issues (as the two dead
  vars demonstrate).
- **First external-tool gate inside `check`** → `dependencySecurityCheck` (snyk) is deliberately outside `check`; this
  gate is in `check` because workflow correctness should fail the build locally. The code-quality "Quality verification
  does not require an IDE" requirement concerns IDE-independence, not tool-independence — a missing `actionlint` is
  handled by the clear `doFirst` failure (D1), consistent with how `dependencySecurityCheck` reports a missing snyk.
