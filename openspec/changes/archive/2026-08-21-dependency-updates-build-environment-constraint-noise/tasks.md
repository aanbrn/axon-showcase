## 1. Document the known limitation in AGENTS.md

- [x] 1.1 In `AGENTS.md`, add a note next to the `dependencyUpdates` command explaining that build-environment
      constraints (from build tooling such as SpotBugs) can surface as spurious "current version" rows (e.g.
      `log4j-core [2.17.1 -> 2.26.1]`), that these are known plugin limitations not real updates, and verify the note
      appears in the right section

## 2. Document the known limitation in README.md

- [x] 2.1 In `README.md` Dependency Updates section, add a short paragraph describing the build-environment-constraint
      noise row, its root cause (SpotBugs `spotbugs-annotations` Log4Shell constraint reported as the current version
      by `checkBuildEnvironmentConstraints`), and that it is not a real update; verify the paragraph renders correctly

## 3. Capture the finding as an ADR

- [x] 3.1 Create a new ADR under `docs/adr/` (e.g. `docs/adr/0007-dependency-updates-build-environment-constraint-noise.md`)
      in Nygard format (Status/Context/Decision/Consequences) recording the root cause, the evidence trail, the
      decision to treat the row as a documented known plugin behavior, and a reference to upstream
      ben-manes/gradle-versions-plugin#755; verify the file is well-formed and consistent with existing ADRs

## 4. Verify the change artifacts

- [x] 4.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors
