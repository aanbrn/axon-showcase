## 1. The auditor reports accretion

- [x] 1.1 Add the accretion class to `.opencode/agent/agents-auditor.md`: its criterion (a rule about the agent, its
      tooling, the per-change workflow, or the documentation, as opposed to a fact about the product), the origin
      reporting, and the git queries (`git blame`, `git log -S`) in its read-list, with the in-prose marker as the
      authoritative source going forward.
- [x] 1.2 Update its frontmatter `description` and its report-contract verdict line to name the accretion count.
- [x] 1.3 Update `.opencode/commands/audit-agents.md`: the read-list and the output description.

## 2. Captured rules carry their origin

- [x] 2.1 Add the origin-marker obligation to `.opencode/agent/lesson-capture.md` — the change for an implementation
      capture, the change and PR for the post-merge one.
- [x] 2.2 Add the marker rule to the capture convention in `AGENTS.md`, extending the existing consolidation paragraph
      rather than adding a bullet.

## 3. Docs and spec

- [x] 3.1 Update the `agents-auditor` bullet in `AGENTS.md` to name the accretion class.
- [x] 3.2 Update `README.md`: the auditor's description, its `agents-auditor` agent-table row, and its slash-command
      table row where the report's shape is described.
- [x] 3.3 The delta spec carries the three `MODIFIED` requirements with every existing scenario preserved.
- [x] 3.4 The capability `## Purpose` refresh is deferred to the archive commit, as a delta cannot carry a Purpose for
      an existing capability; it is applied there, not here. The wording to apply: add the accretion class — the meta
      rules the audit reports with their origins — to the Purpose's description of what the tooling audit reports.
- [x] 3.5 The `docs/ideas.md` sweep found no reference to remove. The one follow-up this change surfaces — retro-marking
      the 46 existing capture-introduced bullets — is a non-goal here and is parked as its own docs PR from `main` after
      this change merges, per that file's header rule that a newly parked idea does not ride a change branch.

## 4. Verification

- [x] 4.1 `./gradlew spotlessApply` after the final edit, then `spotlessCheck`.
- [x] 4.2 `openspec validate --all` green, and the CI config probe's two warnings absent.
- [x] 4.3 `review-quick` over the proposal, then over the implementation, looping until clean before asking for the
      manual pass.
- [x] 4.4 Smoke-run `/audit-agents` after the definition change with seeded controls — a known meta rule it must report
      and a known product fact it must not — and confirm the accretion section appears and behaves as the delta
      specifies: the verdict line names the accretion count, each item carries its origin and the source used, and no
      item proposes removing or merging the rule; restart OpenCode first if the changed definition is not picked up. Do
      not archive while this is unchecked.
- [x] 4.5 Verify the marker survives the formatter: add a `captured:` marker to a rule whose paragraph is long enough
      that `spotlessApply` actually rewraps it, confirm the reflow occurred, then confirm `grep -n "captured:"` still
      finds it — an already-wrapped rule would prove nothing.
