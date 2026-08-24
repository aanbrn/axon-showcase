## 1. Re-scope the check task

- [x] 1.1 In `java-conventions.gradle.kts`, replace `check dependsOn(testing.suites)` with a membership filter:
      `test` and `componentTest` always; `integrationTest` only when `-PskipITs` is absent; `e2eTest` never

## 2. Re-scope the coverage inputs

- [x] 2.1 In `code-coverage-conventions.gradle.kts`, apply the same filter to `allSuiteTestTasks` (exclude `e2eTest`
      always, exclude `integrationTest` when `-PskipITs`) so `jacocoTestReport`/`jacocoTestCoverageVerification` do
      not re-pull skipped suites into `check`

## 3. Verify the topology

- [x] 3.1 Confirm `./gradlew check` runs `integrationTest`; `./gradlew check -PskipITs` skips it without the
      coverage gate re-pulling it; `./gradlew e2eTest` still runs e2e with image builds; `./gradlew build` runs no
      e2e and builds no images

## 4. Refresh the docs

- [x] 4.1 Update `AGENTS.md`: the `check` pipeline runs unit + component + integration by default, `-PskipITs`
      drops integration for Docker-free runs, and e2e is a separate opt-in task (`./gradlew e2eTest`)
- [x] 4.2 Update `README.md` Testing notes accordingly

## 5. Verify the change artifacts

- [x] 5.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors