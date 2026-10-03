# Tasks

## 1. Extend the OpenCode probe

- [x] 1.1 In `.github/workflows/ci.yml`, extend the "Probe the OpenCode config's V1 read path" step: widen the
      changed-path check from `.opencode/opencode.json*` to `.opencode/`, and — after the existing `debug config`
      exit-status check — assert the resolved inventory with `python3` (stdlib `json`) **file-driven**: for every
      `.opencode/agent/*.md` and `.opencode/commands/*.md` the resolved `debug config` must have a matching entry with a
      non-empty `description`, and every `.opencode/skills/*/SKILL.md` must appear in `opencode debug skill` (match by
      `location`) — so a definition the loader drops entirely is caught, not only one whose fields degrade. Fail with
      `::error::` naming each definition. Verify with `./gradlew workflowLint`.
- [x] 1.2 Refresh the docs the change falsifies: `README.md`'s Continuous Integration paragraph (the probe now covers
      the agent/command/skill inventory and triggers on any `.opencode/` change); `AGENTS.md`'s `ci.yml` gate bullet
      (lines ~635-642); the config-read-path gotcha twice — its "take the signal from the process exit status" clause
      (its blind spot: a silently-degraded definition passes the load) and its "no gate validates that frontmatter"
      clause (~line 2353), now false; and the scratch-files bullet's "the `build` gate's OpenCode config probe only
      loads the configuration" clause (~line 1274), which now also loads the definition inventory (it still never
      invokes `openspec`). Remove the implemented idea from `docs/ideas.md` (the 2026-10-02 "Guard the agent-loader read
      path" entry — the section's only remaining content, so remove the now-empty `## 2026-10-02` heading too). Verify
      by reading the edited passages and confirming the probe's documented scope is the inventory and its trigger is
      `.opencode/`; `grep -n "no gate validates that frontmatter\|probe only loads the configuration" AGENTS.md` and
      `grep -n "Guard the agent-loader read path\|^## 2026-10-02" docs/ideas.md` each return nothing.
- [x] 1.3 Run `./gradlew workflowLint spotlessApply spotlessCheck` and `openspec validate --changes`, and confirm all
      pass.
- [x] 1.4 Prove the assertion with a known-bad and a known-good input under the pinned consumer: install `v1.18.34`
      (`curl -fsSL https://opencode.ai/install` resolves it, or the `opencode-darwin-*` release asset) under a temporary
      `HOME` and invoke it by absolute path against the repository. Confirm it **fails** for a malformed multi-line
      `description` containing `: ` in an agent (fields dropped), in a command (its `description` dropped), and in a
      skill (absent), and **passes** for the unchanged inventory — reading the actual `debug config`/`debug skill`
      output each time, not just the exit code. Record the commands and observations on this task; delete the scratch
      definitions afterwards (`git status --porcelain` clean of them).

## 2. Verification

- [x] 2.1 Confirm the pull request's own `build` run exercises the probe (it edits `.github/workflows/ci.yml`, so the
      gate includes it) and passes.
- [x] 2.2 Prove the run-and-fail path in CI at the merge stage: temporarily commit a malformed `.opencode/agent/*.md`
      definition, push to the pull request, watch the probe step fail and name the definition, then revert and push;
      record both runs' outcomes.
- [x] 2.3 Run the per-unit `lesson-capture` subagent over the change and apply its durable proposals; record the applied
      net `AGENTS.md` delta on this task.
