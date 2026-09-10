## 1. Fix the local deploy image build

- [x] 1.1 Add `:showcase-web-ui:dockerBuildImage` to the app release's `installDependsOn` in `build.gradle.kts`, so
      `helmInstallToLocal` builds all five service images (four JVM + web UI).
- [x] 1.2 Update `AGENTS.md`'s per-release-tasks note (the app-release install task now depends on the four
      `bootBuildImage` tasks plus the web-UI `dockerBuildImage`).

## 2. Verify

- [x] 2.1 Confirm the web-UI `dockerBuildImage` task exists and is wired as a dependency:
      `./gradlew     helmInstallToLocal --dry-run` shows the web-UI image task in the graph.
- [x] 2.2 Run `openspec validate --all` and `openspec validate --changes`.
- [x] 2.3 Run `./gradlew spotlessApply` / `spotlessCheck` (change docs are in the markdown target).
