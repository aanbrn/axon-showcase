# Tasks

## 1. Add the snyk workflow

- [x] 1.1 Add `.github/workflows/snyk.yml` with a single `snyk` job on `ubuntu-latest` (Temurin JDK 21) triggering on
      a weekly `schedule` cron and `workflow_dispatch`, and verify the workflow file parses (YAML 1.2 validation)
- [x] 1.2 Wire the job to run `./gradlew dependencySecurityCheck` after `actions/checkout`, `actions/setup-java`,
      `gradle/actions/setup-gradle`, and `snyk/actions/setup` (which installs the Snyk CLI the task's
      `snykExecutableOnPath()` guard requires), exporting the `SNYK_TOKEN` secret as an environment variable, and
      verify it does NOT trigger on `pull_request` or `push` to `main`
- [x] 1.3 Verify the workflow caches only the Gradle User Home (no workspace `build/` directories) and that the only
      secret used is `SNYK_TOKEN`

## 2. Verify the workflow

- [x] 2.1 Set the `SNYK_TOKEN` repository secret, run the workflow once via `workflow_dispatch` on the merged branch,
      and confirm the `snyk` job completes successfully (scan runs with the root `.snyk` policy applied)
- [x] 2.2 Run `openspec validate snyk-in-ci` and confirm the change is valid with all artifacts consistent

## 3. Docs refresh

- [x] 3.1 Update `AGENTS.md` and `README.md` to mention the scheduled snyk workflow alongside `ci.yml` and `e2e.yml`,
      and verify the edited files respect the 120-character line limit