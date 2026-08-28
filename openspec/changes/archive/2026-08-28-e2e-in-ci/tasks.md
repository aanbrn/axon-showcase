# Tasks

## 1. Add the e2e workflow

- [x] 1.1 Add `.github/workflows/e2e.yml` with a single `e2e` job on `ubuntu-latest` (Temurin JDK 21) triggering on
      a nightly `schedule` cron and `workflow_dispatch`, and verify the workflow file parses (YAML 1.2 validation)
- [x] 1.2 Wire the job to run `./gradlew :showcase-api-gateway:e2eTest` after `actions/checkout`,
      `actions/setup-java`, and `gradle/actions/setup-gradle`, and verify it does NOT trigger on `pull_request` or
      `push` to `main` (the e2e run must never be a merge gate)
- [x] 1.3 Verify the workflow declares no secrets and no image-push step, and that the Gradle cache path excludes
      workspace `build/` directories (stale `jacoco` exec data rule)

## 2. Verify the workflow

- [x] 2.1 Run the workflow once via `workflow_dispatch` on the merged branch and confirm the `e2e` job completes
      successfully (builds all four images and boots the full pipeline), checking the run log for the four service
      image tags resolving to `project.version`
- [x] 2.2 Run `openspec validate e2e-in-ci` and confirm the change is valid with all artifacts consistent

## 3. Docs refresh

- [x] 3.1 Update `AGENTS.md` and `README.md` to mention the scheduled e2e workflow alongside `ci.yml`, and verify the
      edited files respect the 120-character line limit