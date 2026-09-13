## 1. Widen the auditor definition

- [x] 1.1 Add the intent-clarification advisory class, the four-surface sweep, and the question-per-item output contract
      to `.opencode/agent/architecture-auditor.md`, update its frontmatter `description` to name the new advisory class,
      and verify every line is ≤120 characters (`.opencode/` is outside Spotless)
- [x] 1.2 Extend `.opencode/commands/audit-architecture.md`'s step-1 read-list to name the intent-clarification
      surfaces, and fix the manual 120-character check's vacuous `.opencode/*.md` glob in both that command and
      `.opencode/commands/audit-agents.md` so it scopes to the `.opencode/` files the audit edited rather than every
      markdown file under `.opencode/` (`node_modules`, the generator-written `opsx-*` commands and `openspec-*` skills,
      and the vendored skills are out of scope; the project-authored `opsx-tool-update.md` stays in scope); verify both
      files are ≤120 characters per line
- [x] 1.3 Verify the widened definition keeps the drift finding classes and the two-section report structure unchanged,
      and states that the new class is advisory-only
- [x] 1.4 Verify the definition names the same four surfaces the delta spec enumerates (dependency `exclude(...)`
      declarations; major-version-suppressed coordinates in `config/dependency-updates/major-disabled.properties`;
      suppression annotations that encode a design choice (`@SuppressWarnings`) and deprecated-API usages the project
      still carries; and deferrals or band-aids recorded in an ADR or parked in `docs/ideas.md`)

## 2. Align the descriptions

- [x] 2.1 Update the `AGENTS.md` architecture-auditor bullet to name the new advisory class and its surfaces, and verify
      with `./gradlew spotlessApply` plus `git diff` that only the intended prose changed
- [x] 2.2 Update `README.md`'s architecture prose, the `architecture-auditor` agent-table row, and the
      `/audit-architecture` slash-command table row to reflect the widened advisory output, preserving the section order
      and the one-line row style
- [x] 2.3 Re-read the AGENTS.md auditor-justification bullet and adjust it only if this widening makes its text
      inaccurate
- [x] 2.4 Apply the capability `## Purpose` refresh in the archive commit — the current Purpose scopes the architecture
      audit to the ADRs and the dependency/decomposition surface, which the intent-clarification sweep extends; recorded
      as an explicit task because a delta cannot carry a Purpose

## 3. Docs refresh

- [x] 3.1 Park the auditor's identified items in a separate `docs/ideas.md` PR after this change merges (the owner's
      chosen order): the intent questions the first widened run surfaced — the `org.axonframework` major pin with no
      recorded rationale and its dead `major-disabled.properties` pointer, the `ClassCanBeRecord` suppressions, the
      OpenSearch high-level-client exclusions, the `axon-server-connector` exclusions outside the routing pair, and the
      lower-confidence `FutureReturnValueIgnored` item — plus the ADR-0002 `showcaseCache` contradiction the run found,
      the stale ADR-0007 `spotbugs-annotations` version, and the incomplete no-spec module enumeration in AGENTS.md

## 4. Verification

- [x] 4.1 Run the widened audit over the repository and verify it reports intent-clarification items for the known
      unrecorded rationales while reporting none for the already-explained surfaces (Flyway, JGroups,
      spring-data-opensearch, springdoc, ADR-0004, the routing-pair `axon-server-connector` exclusions per ADR-0009, and
      the `CodeBlock2Expr` convention)
- [x] 4.2 Run `./gradlew spotlessApply` after the final edit, then confirm `./gradlew spotlessCheck`, the PR gate
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, and `openspec validate --all` are clean, and that a
      manual `perl -CSD -lne 'print if length > 120'` check over the three edited `.opencode/` files
      (`.opencode/agent/architecture-auditor.md`, `.opencode/commands/audit-architecture.md`,
      `.opencode/commands/audit-agents.md`) reports nothing
- [x] 4.3 Smoke-test the subagent after an OpenCode reload (a changed subagent definition is read at session start) and
      record the result — do not archive while this is unchecked
