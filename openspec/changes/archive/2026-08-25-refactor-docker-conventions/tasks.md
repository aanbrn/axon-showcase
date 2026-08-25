## 1. Refactor the convention

- [x] 1.1 Replace the six `Exec` registrations with a `registerComposeTask` helper (group, description, `buildFirst`
      `dependsOn`, structured `commandLine`, `workingDir`, environment, lock, and `onlyIf`), add a
      `dockerCliOnPath()` guard throwing a `GradleException` in `doFirst` when `docker` is missing, and verify
      `build-logic` compiles

## 2. Verify task behavior is unchanged

- [x] 2.1 Verify the same `composeUp`, `composeRestart`, `composeStop`, `composeDown`, `composeBuildAndUp`,
      `composeBuildAndRestart` tasks exist with their descriptions, and run `openspec validate` on the change