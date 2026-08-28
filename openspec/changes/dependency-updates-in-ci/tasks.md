# Tasks

## 1. Add the dependency-updates workflow

- [x] 1.1 Add `.github/workflows/dependency-updates.yml` with a single `dependency-updates` job on `ubuntu-latest`
      (Temurin JDK 21) triggering on a weekly `schedule` cron and `workflow_dispatch`, and verify the workflow file
      parses (YAML 1.2 validation)
- [x] 1.2 Wire the job to run `./gradlew dependencyUpdates` after `actions/checkout`, `actions/setup-java`, and
      `gradle/actions/setup-gradle`, with the `GITHUB_TOKEN` granted `contents: read` and `issues: write`, and verify
      it does NOT trigger on `pull_request` or `push` to `main`
- [x] 1.3 Add a step that opens or updates the "Dependency updates" issue from
      `build/dependencyUpdates/report.txt` (available catalog updates + the Gradle wrapper section), mentioning the
      repository owner (`cc @<owner>`) so the owner is notified on creation and update, and verify it uses only the
      `GITHUB_TOKEN` (no PAT, no extra secrets) and caches only the Gradle User Home (no workspace `build/`
      directories)

## 2. Verify the workflow

- [x] 2.1 Run the workflow once via `workflow_dispatch` on the merged branch and confirm the `dependency-updates` job
      completes successfully and a "Dependency updates" issue is created with the report summary mentioning the
      repository owner
- [x] 2.2 Run the workflow a second time and confirm it updates the existing issue in place (no duplicate issues)
- [x] 2.3 Run `openspec validate dependency-updates-in-ci` and confirm the change is valid with all artifacts
      consistent

## 3. Docs refresh

- [x] 3.1 Update `AGENTS.md` and `README.md` to mention the scheduled dependency-updates workflow alongside `ci.yml`,
      `e2e.yml`, and `snyk.yml`, and verify the edited files respect the 120-character line limit