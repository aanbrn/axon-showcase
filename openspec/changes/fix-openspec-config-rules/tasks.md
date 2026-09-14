## 1. Repair the configuration

- [x] 1.1 Quote the two `openspec/config.yaml` rule items whose unquoted `: ` makes YAML parse them as mappings
      (`proposal`'s first and `tasks`' third), and verify with `openspec instructions proposal --change <probe> --json`
      and `... tasks ...` that the `rules` key is now present with the same wording (create a probe change, then remove
      it)
- [x] 1.2 Verify all four artifacts' rules load (`proposal`, `specs`, `design`, `tasks`), with `specs`/`design`
      unchanged as the control, and that `openspec validate --all` is still clean

## 2. Guard it

- [x] 2.1 Extend the CI `build` job's OpenSpec step in `.github/workflows/ci.yml` with a probe that creates a change,
      fails if the CLI reports it is ignoring an artifact's rules, and removes the probe via a `trap` on `EXIT`; verify
      the guard detects the defect by temporarily reverting one quoted item and watching the step fail, then restoring
      it
- [x] 2.2 Add the same check to `.opencode/commands/opsx-tool-update.md`, **with a positive control** (temporarily
      unquote a rule and confirm the check fails), since a renamed CLI warning would otherwise pass silently; verify by
      following the command's steps against a deliberately broken config and confirming it fails as described

## 3. Record the pitfall and refresh the stale texts

- [x] 3.1 Add to `AGENTS.md` the YAML pitfall (an unquoted scalar containing `: ` parses as a mapping, so a declared
      rule list stops being an array of strings) and the lesson that a CLI warning dismissed as noise was reporting a
      live defect; verify with `./gradlew spotlessApply` plus `git diff` that only the intended prose changed
- [x] 3.2 Audit the `openspec/config.yaml` `context:` block against the project facts it duplicates, correcting the
      Build-&-test drift already found: the Docker-image dependency belongs to `showcase-api-gateway`'s `e2eTest` suite
      rather than its `integrationTest` (`showcase-api-gateway/build.gradle.kts:196-201`, and AGENTS.md's gotcha), and
      the "must run after the client integration tests" clause is obsolete — the client modules register only
      `componentTest`, so drop it or replace it with `e2eTest`'s actual `shouldRunAfter(integrationTest)`. Also fix the
      architecture header, which reads "4 services + API gateway + web UI" for a list of five (three services, the
      gateway, and the web UI) — match AGENTS.md's phrasing, which does not add the web UI separately — and its
      suite-order line (`test -> componentTest -> integrationTest`), which omits the `e2eTest` tier AGENTS.md documents.
      Verify the headline facts too (Java 21, Spring Boot 3.5.16, Gradle 9.7.1, 19 modules — 18 JVM plus
      `showcase-web-ui`)
- [x] 3.3 Refresh the CI-gate enumerations the new step makes stale — `AGENTS.md`'s "Continuous Integration" bullets and
      `README.md`'s "Continuous Integration" paragraph both describe the gate as the Gradle check plus
      `openspec validate --all`, so name the OpenSpec configuration check there too

## 4. Verification

- [x] 4.1 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (which lints the edited workflow via actionlint)
      and `./gradlew spotlessCheck` after the final edit, plus `openspec validate --all`; run the manual
      `perl -CSD -lne 'print if length > 120'` check over the edited `ci.yml` and `config.yaml`, which the formatter
      does not cover
- [ ] 4.2 Record the `showcase/quality/merge-governance` `## Purpose` check for the archive commit — confirm it still
      fits (it already describes the CI gates) and edit it only if the modified requirements make it inaccurate;
      recorded as an explicit task because a delta cannot carry a Purpose
