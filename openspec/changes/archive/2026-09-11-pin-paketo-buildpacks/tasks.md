## 1. Pin the buildpacks

- [x] 1.1 Add `paketo-nginx = "1.2.0"` and `paketo-procfile = "5.14.0"` to `gradle/libs.versions.toml` `[versions]`
- [x] 1.2 In `build-logic/src/main/kotlin/frontend-conventions.gradle.kts`, build the `dockerBuildImage` buildpacks from
      the catalog: `"paketo-buildpacks/nginx@" + libs.versions.paketo.nginx.get()` and
      `"paketo-buildpacks/procfile@" + libs.versions.paketo.procfile.get()`
- [x] 1.3 Add a short note to `AGENTS.md` (the web-UI image build description) recording the pinned buildpacks and why
      the versions are explicit (the floating builder + unversioned reference ambiguity), and that keeping them current
      is not covered by an update check

## 2. Verify

- [x] 2.1 Build the web-UI image locally (`./gradlew :showcase-web-ui:dockerBuildImage`) and confirm it succeeds (the
      exact task that failed in CI); run `./gradlew spotlessCheck` and `openspec validate --changes` cleanly
