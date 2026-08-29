## 1. Vendor the skills

- [x] 1.1 Download the `axoniq-migration` plugin skills at version 0.2.2 from `AxonIQ/agent-skills` (the
      `plugins/axoniq-migration/skills/` tree) and place them under `.opencode/skills/` as
      `axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`, copying each directory verbatim
      (SKILL.md, assets, references, scripts) and verifying each skill's `SKILL.md` exists with the upstream
      frontmatter intact
- [x] 1.2 Verify opencode discovers the vendored skills — `ls .opencode/skills/` shows the three skill directories
      and each contains `SKILL.md` (the `## Requirements` from `agent-skills/spec.md`: discoverable entry points)

## 2. Provenance and docs

- [x] 2.1 Add a short note to `AGENTS.md` under the workflow/conventions section recording that the three
      `axon4to5-*` skills are vendored from `AxonIQ/agent-skills` plugin `axoniq-migration` 0.2.2 (Apache-2.0), and
      how to refresh them, verifying the note stays within the 120-character line limit
- [x] 2.2 Confirm the vendored skills are not referenced by any Gradle module or Docker image — grep the build
      files/`build.gradle.kts` and Docker configs for `axon4to5` and verify no matches, and confirm `./gradlew
      help` (or a light Gradle task) still succeeds with the skills present

## 3. Spec compliance

- [x] 3.1 Run `openspec validate vendor-axoniq-migration-skills` and confirm the change is valid, then run
      `openspec validate --all` and confirm the whole spec set still passes (16 items)