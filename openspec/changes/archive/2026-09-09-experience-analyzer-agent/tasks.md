## 1. Add the experience-analyzer agent

- [x] 1.1 Add `.opencode/agent/experience-analyzer.md` (frontmatter `mode: subagent`, cheap model, `temperature: 0`):
      given a gathered digest (merged PRs, archived changes, AGENTS.md gotchas, docs/ideas.md, plus a context note),
      produces a retrospective (PRs grouped by theme, lessons, went-well/went-wrong) and improvement suggestions each
      classified as system (→ ideas.md/proposal) or process (→ AGENTS.md); never edits files itself.
- [x] 1.2 Add the deterministic gather step: a `scripts/experience-analysis.sh` (or documented shell/gh commands) that
      prints the digest (`gh pr list --state merged --search "merged:>=<window>"`, `git log --since <window>`, archived
      changes, AGENTS.md gotchas, docs/ideas.md) for feeding to the subagent.
- [x] 1.3 Add a user-facing trigger: the `/retrospective` opencode command (`.opencode/commands/retrospective.md`) that
      gathers the digest and invokes the `experience-analyzer` subagent, so users can run the analysis without knowing
      the internals.

## 2. Docs and spec

- [x] 2.1 Update `AGENTS.md` — document the `experience-analyzer` agent and when to run it (on demand / periodic), and
      the `docs/retrospectives/<date>.md` storage convention.
- [x] 2.2 Sync the `agent-skills` spec delta (MODIFIED vendored-skills requirement + ADDED experience-analysis
      requirement) — per the sync-at-archive workflow.
- [x] 2.3 Update `docs/ideas.md` — the parked `experience-analyzer` idea is captured by this change (remove it), shipped
      as a separate docs PR per the docs-refresh convention (never bundled with the change's PR).

## 3. Verify

- [x] 3.1 Run a trial invocation of the agent on the recent session's PRs (e.g. the last week) and sanity-check the
      retrospective + suggestions.
- [x] 3.2 Run `./gradlew spotlessApply` / `spotlessCheck` (new docs/md/scripts are formatted) and
      `openspec validate --all` / `--changes`.
