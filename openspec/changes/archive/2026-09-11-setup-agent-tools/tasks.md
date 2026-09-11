## 1. The setup skill and command

- [x] 1.1 Add `.opencode/skills/setup-agent-tools/SKILL.md`: frontmatter (`name` matches the directory, `description`
      front-loads "set up the GitHub/JetBrains MCP tooling") plus the procedure — detect state (`opencode mcp list`,
      `gh auth status`, `gh extension list`, `which devrig`) → GitHub core (`gh auth login` handed to the user,
      `gh extension install shuymn/gh-mcp`, wire it with `opencode mcp add github -- gh mcp`) → Steroid optional (only
      when the user uses IDEA — install `devrig` if missing, add the MCP Steroid IDE plugin, then wire with
      `opencode mcp add steroid -- devrig mcp`) → verify the entries connect with `opencode mcp list` (an entry with no
      `enabled` key is enabled by default) → tell the user to restart
- [x] 1.2 Add `.opencode/commands/setup-agent-tools.md` (`/setup-agent-tools`) with the standard
      `--- description: … ---` frontmatter that triggers the same procedure, and verify the skill is discovered
      (`opencode debug skill` lists `setup-agent-tools`)

## 2. Documentation

- [x] 2.1 Update the README "Tooling MCP Servers" section to present the agent-driven setup as the preferred, easiest
      path (today only the manual wiring is documented), distinguish **GitHub (core)** from **Steroid (optional, IDEA
      only)**, and reword the closing "All are optional" line so it does not contradict GitHub being core
- [x] 2.2 Add the `/setup-agent-tools` row to the README Slash Commands table
- [x] 2.3 Add a short note to `AGENTS.md` describing the command/skill and the GitHub-core / Steroid-optional split,
      noting `setup-agent-tools` is project-local (not one of the vendored `axon4to5-*` skills)
- [x] 2.4 Surface the on-request agent tooling setup in the README intro and the "Technologies → In the Process" list

## 3. Verify

- [x] 3.1 Run `./gradlew spotlessApply spotlessCheck`, `openspec validate --changes`, and confirm the skill is listed by
      `opencode debug skill` and the command is well-formed
