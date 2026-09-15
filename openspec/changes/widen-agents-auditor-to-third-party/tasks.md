## 1. Widen the auditor definition

- [ ] 1.1 Add the third-party-inconsistency advisory class to `.opencode/agent/agents-auditor.md`: a separate advisory
      section (not among the fix findings), the harm test with an in-test and an out-of-test example, and the named
      routes (report upstream / re-vendor / change our usage); state that the skipped files stay out of the fix scope
      and that no local edit to one is ever proposed
- [ ] 1.2 Update the frontmatter `description` to note the advisory class
- [ ] 1.3 Verify the provenance partition and the skip rule are textually unchanged (the files stay out of the fix
      scope), and that the new class reads as advisory-only
- [ ] 1.4 Verify with `./gradlew spotlessApply` and `spotlessCheck` that only the intended prose changed
      (`.opencode/agent/**/*.md` is in the root markdown Spotless target)

## 2. Capture the class as a spec requirement

- [ ] 2.1 Extend the `agents-auditor` requirement in the change's delta spec for `showcase/quality/agent-skills` with
      the third-party-inconsistency advisory class and its harm test — MODIFIED must carry every existing scenario the
      main spec still has, so copy the current requirement block in full and edit it
- [ ] 2.2 Confirm the delta does not carry a `## Purpose` (a delta cannot); decide whether the `agent-skills` Purpose
      needs a mention and, if so, record the archive-commit refresh as an explicit task

## 3. Align the descriptions

- [ ] 3.1 Update `.opencode/commands/audit-agents.md` so its report step names the new advisory class (it already defers
      to the report contract)
- [ ] 3.2 Update the `AGENTS.md` `agents-auditor` bullet to note the class where it describes the audit's scope and
      output
- [ ] 3.3 Confirm the `README.md` `agents-auditor` row and the `/audit-agents` slash-command row still read accurately
      (an expected no-op — one-line rows)

## 4. Verify

- [ ] 4.1 Confirm the change adds no code, workflow, or deployment edit, and that `openspec validate --all` passes
- [ ] 4.2 Smoke-run `/audit-agents` seeded with a known third-party mismatch it must report and a known-explained
      surface it must not (a generated file that merely differs from our prose) — reload OpenCode first so the edited
      definition is loaded, and treat the smoke-run as a precondition for archiving
- [ ] 4.3 Apply the `## Purpose` refresh in the archive commit if 2.2 found it necessary — recorded as an explicit task
      because a delta cannot carry a Purpose
