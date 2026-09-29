# Tasks

## 1. Add the check

- [x] 1.1 Add `build-logic/src/main/kotlin/InstallCommandRules.kt`: a pure object that parses the
      `helm install <name> <chartRef> --version <v>` lines of a markdown block into `(chartRef, version)` pairs and
      returns a mismatch reason for a chart whose version differs from its catalog pin (or that has none).
- [x] 1.2 Add `build-logic/src/main/kotlin/VerifyInstallCommandsTask.kt`: a cacheable task taking the `AGENTS.md` file
      and the `chartRef → pinnedVersion` checks as inputs, parsing the file's helm install commands, and failing on a
      mismatch; write a result file.
- [x] 1.3 Register `verifyInstallCommands` in `build.gradle.kts` with `helmChartChecks` and the `AGENTS.md` file as
      inputs, and add `dependsOn("verifyInstallCommands")` to the root `check` task.

## 2. Capture the behavior in the spec

- [x] 2.1 Write the delta at
      `openspec/changes/check-agents-install-commands/specs/showcase/quality/infra-image-versions/spec.md`: one
      `MODIFIED` requirement carrying the main spec's `Infrastructure image references are single-sourced` block
      verbatim (all five existing scenarios, in order, byte-identical header), with the `AGENTS.md` install-command
      clause added to the description and a new scenario verifying the block against the catalog. Verify with
      `openspec validate --changes`.

## 3. Docs the change owns

- [x] 3.1 Remove the "Check the `AGENTS.md` manual install commands against the catalog" entry from `docs/ideas.md`
      (implemented by this change).
- [x] 3.2 Add `verifyInstallCommands` to the `check`-note enumeration and the `verifyInfraImageVersions` prose in
      `AGENTS.md`, and to `README.md`'s verification-command block. Verify by reading the sites.

## 4. Verification

- [x] 4.1 Add `build-logic/src/test/kotlin/InstallCommandRulesTests.kt` covering the parser and the mismatch rule (a
      matching command, a mismatched one, a chart with no pin, and the app-chart line skipped). Run
      `./gradlew -p build-logic test`.
- [x] 4.2 Prove the check fails on a known-bad input and passes on a known-good one: temporarily set an `AGENTS.md`
      command's `--version` to a wrong value, run `./gradlew verifyInstallCommands` and confirm it fails naming the
      chart, then restore it and confirm it passes. Record both runs. Done: with the kps command perturbed to
      `--version 91.0.0`, `verifyInstallCommands` failed with "AGENTS.md installs chart
      'prometheus-community/kube-prometheus-stack' at '91.0.0', but the catalog pins '91.8.1'."; restored, it passes
      (exit 0).
- [x] 4.3 Run the implementation `review-quick` loop over the diff; fix its findings and re-run until it reports nothing
      new.
- [x] 4.4 Run the per-unit `lesson-capture` over this change and apply its durable proposals, recording the applied net
      `AGENTS.md` delta on this change's record. Done: one addition to the Javadoc bullet (the rule covers every
      declaration in a touched file), +4 lines.
- [x] 4.5 Run `./gradlew spotlessApply` after the last edit and confirm `spotlessCheck` passes; run the manual
      120-character check over every changed file a formatter does not own — the fenced `bash` blocks in `AGENTS.md`
      (the `check` note) and `README.md` (the verification block), and the change dir's `.openspec.yaml`.
- [x] 4.6 Request the user's manual review pass — the step before committing.
