# Design — adopt the IDE's built-in MCP server

## Context

- The repository's IDE guidance grew around MCP Steroid because it was the only way an agent could reach a running
  IntelliJ: `steroid_execute_code` runs Kotlin in the IDE process, and `runInspectionsDirectly` is its inspection
  helper.
- The idea parked in `docs/ideas.md` on 2026-11-06 established that Steroid's script runtime is broken on IDEA 2026.3
  and that the IDE ships its own MCP server, whose transport is a remote HTTP endpoint. The idea was gated on
  `assert-paketo-pin-coverage` shipping; that change is archived at
  `openspec/changes/archive/2026-11-06-assert-paketo-pin-coverage/`, so the gate is met.
- The premise was re-verified against the running IDE, not taken from the idea note: the failure reproduces, the
  built-in server answers, and its tool list contains what the repository uses an IDE for. The claims table below
  carries each check and its source.
- The idea carried a second half — running OpenCode inside the bundled JetBrains Air as an ACP agent — and the owner
  rejected parking it: the two wirings are independent, and the reader should choose where they run the agent. The owner
  also rejected leaving the README silent, since its `/setup-agent-tools` row promised "optional extras" while naming
  none.

## Decisions

### D1 — The built-in server is the primary IDE server; Steroid stays as the fallback

- The built-in server's tool list covers inspections (`lint_files`, `get_file_problems`), refactoring
  (`rename_refactoring`), formatting (`reformat_file`, `optimize_imports`), semantic navigation (`analyze_calls`,
  `search_symbol`, `get_symbol_info`), builds (`build_project`), run configurations, the terminal, sixteen debugger
  tools, coverage, the profiler, VCS, and `apply_patch`.
- It has **no** screenshot and no UI-input tool. Steroid's `steroid_take_screenshot` and `steroid_input` therefore have
  no built-in counterpart.
- The Steroid bridge is also how a headless backend is started and provisioned (`devrig backend`). A frontend IDE runs
  its own built-in server; a backend managed by `devrig` is reached through the bridge.
- Therefore the guidance names the built-in server first and keeps the Steroid procedure as a fallback for those cases —
  an IDE the built-in server cannot serve, a `devrig`-provisioned backend, and screenshot or UI-input work. Deleting the
  Steroid procedure would remove the only documented path to them.

### D2 — The skill teaches the URL from the IDE, never a hard-coded one

- The built-in server's address is per-instance: this machine answers on `http://127.0.0.1:64342/stream`, and the
  settings page carries a `Port:` field, so the port is configurable and can differ per machine.
- The skill therefore tells the reader to enable the server and copy the URL the IDE shows (the status-bar widget's
  "Copy server URL", or the settings page), rather than writing this machine's port into the repository.
- It also recommends pinning the port in the settings page, so the URL does not change on the next start. A stale URL is
  the likely cause of an `intellij` server that reads `failed`: `/reconnect-mcp` reconnects a server, it cannot retarget
  a changed port.

### D3 — `run_ide_script` is documented as an opt-in, not as the default IDE path

- `run_ide_script` runs Kotlin in the IDE process with the whole IDE classpath — the built-in counterpart of
  `steroid_execute_code` — and it is absent from a default `tools/list`: its toolset reports enabled only when the
  registry key `mcp.ide.script.toolset.enabled` is set or the IDE runs unattended, and the tool's own description says
  it is available only in Brave Mode. The gate is that registry key, so the skill names it (reached through the IDE's
  Registry dialog) and, as a separate surface, the MCP Server settings page where the exposed tools are chosen — rather
  than implying the tool appears on its own.
- Its confirmation is a per-script dialog ("MCP client wants to run this Kotlin script with all IDE permissions"), which
  Brave Mode skips — the same switch that skips terminal-command and run-configuration confirmations.
- Inspections therefore do not need a script at all: `lint_files` and `get_file_problems` are direct tools, and
  `AGENTS.md` names those. The skill mentions `run_ide_script` only as the escape hatch for work no direct tool covers,
  with the masking, the confirmation, and Brave Mode's wider effect stated beside it.

### D4 — The gotcha that names `steroid_execute_code` stays as recorded

The bullet "Before describing an MCP server, skill, or subagent in a durable doc, read that artifact's own definition"
names `runInspectionsDirectly` as an IntelliJ helper called inside `steroid_execute_code`. That sentence reports a
historical mis-description of a helper as an MCP tool. It stays true — the helper is still a helper of that tool — and
the sweep rule for a superseded example keeps a live artifact's historical incident as recorded. Editing it would
rewrite the incident, not correct a claim.

### D5 — The README names the integrations; the skill owns their procedures

- The README's `Tooling MCP Servers` subsection already explains what MCP servers give the agent and lists two of them —
  the project-configured Playwright and the optional per-user GitHub — so an optional per-user server is inside its
  scope today.
- The repository likewise documents IDE-bound developer tooling in `### Formatting and IDE Setup` (`setup-idea.sh`, the
  palantir/ktfmt plugins, IntelliJ's Prettier), so "IDE-specific" is not by itself a reason to stay out.
- The `/setup-agent-tools` row of the slash-command table promised "optional extras" while naming none, so a reader
  could not tell what the command offers. That is the defect this decision fixes.
- The `AGENTS.md` rule is therefore rewritten rather than bypassed: the README names the integrations and their
  user-facing benefit, while the skill owns configuration, fallback selection, and the hosting path. The Steroid bridge
  stays a fallback documented in the skill, because it needs `devrig` and a third-party plugin install — unlike the
  bundled server, which ships with the IDE.
- The README bullet states what the server gives the agent and points at `/setup-agent-tools`; it carries no port, tool
  count, registry key, or JSON recipe, which are the skill's and vary per machine.

### D6 — No spec delta

No capability spec names the per-user agent-tooling MCP setup: `grep -rn "setup-agent-tools" openspec/specs/` returns no
hits, and the corpus's `mcp` mentions are the `agent-skills` Purpose, the technical advisor's tool-deny list, and the
failed-server recovery requirement — none of which this change alters. `showcase/quality/code-quality` states that
quality verification does not require an IDE, which stays true. The change is `skip_specs: true`.

### D7 — Both hosting paths, chosen by the reader at setup

- Two independent wirings reach the IDE from OpenCode: the client-config entry (OpenCode in a terminal or the desktop
  app) and Air's ACP registration (OpenCode hosted inside the IDE).
- They are not two candidates for one slot. The client entry is `"intellij"` in the flat `mcp` map; Air's is a local
  agent under `agent_servers` in `~/.jetbrains/acp.json`, and `use_idea_mcp` makes Air inject the IDE server itself.
- The skill therefore asks where the reader wants to run OpenCode — terminal/desktop, inside the IDE through Air, or
  both — instead of choosing for them. Declining IDE integration stays supported, as it is today.
- `use_idea_mcp` is parsed into an `IdeaMcpMode` (`OFF`, `ON`, `ON_DEMAND`) and `use_custom_mcp` reads as "let the
  session use the agent's own MCP configuration"; `idea_mcp_allowed_tools` is read beside them. What each field does to
  a session is the field's reading, not a verified behaviour — including whether a session with `intellij` also in the
  global config sees the IDE server twice. The live check settles all three.
- The combinations the skill states follow from that:
  - Air only — ask Air to inject the IDE server (`use_idea_mcp` on) and let the session use the agent's own config
    (`use_custom_mcp` on), with no `intellij` entry in the global config, so there is nothing to duplicate;
  - both — the provisional default is to let Air use the agent's own config and not inject (`use_idea_mcp` off,
    `use_custom_mcp` on), so both hosts see one IDE server; if the live check shows Air leaves the session's own IDE
    entry alone, the skill records the inject-on combination instead;
  - terminal/desktop only — the `intellij` entry alone.
- `idea_mcp_allowed_tools` on the same entry is the lever if a session's tool list proves too wide.
- The file also carries file-level defaults (`default_mcp_settings`) that the factory reads beside the entry's own
  fields, so a file already enabling `use_idea_mcp` there may inject the IDE server for a registered agent. The skill
  reads and reports that block before writing; when it contradicts the reader's choice it offers, with confirmation, to
  set it to match and otherwise leaves it as found. The live check records the block's values so a widened tool list is
  attributable to the entry the skill wrote rather than to a pre-existing default.
- The live check settled the field values, and the skill states the observed rule rather than the options: with Air's
  injection on and no `intellij` entry, the Air session reached the IDE through Air's injected server — visible there as
  `jetbrains_air_ide`, whose `execute_tool` routes to the IDE's tools — and with the injection off and the entry
  present, one IDE server served both hosts with no doubling.
- Each case was proved by one IDE operation against an identified project (`get_project_modules` on this worktree), not
  by a connection status: the client path returned the module list from OpenCode, and both Air configurations did the
  same in Air.

### D8 — The guidance is version-qualified and discovery-first

- The recipe is derived from one machine: IDEA 2026.3 (IU-263.7307.200, its bundled MCP Server 263.7307.200), Air
  263.7307.200-mac-arm64, and OpenCode 2.2.5. Another release can rename a tool, move the tool set, or change the ACP
  schema, and nothing in the README or the skill would notice.
- The skill therefore records the versions it was verified against, and instructs the reader to read the installed
  server's tool list and the applicable ACP configuration before applying the recipe — discovering `lint_files`,
  `get_file_problems`, and the rest rather than assuming they exist under those names.
- An unknown version or schema yields an explicit unsupported or unverified result, never a silent inheritance of this
  machine's assumptions. No version matrix is maintained: the boundary is one sentence plus the discovery step.

### D9 — The Steroid defect owes an upstream disposition

- The reproduced failure is an upstream defect, and `AGENTS.md`'s upstream-report rule makes a third-party defect the
  reason to report it, not an exemption from reporting it.
- The change therefore searches the owning project's tracker for the reproduction and either cites the existing report
  in the skill's fallback section — the section the constraint bites in, and a path the upstream-reference report scans
  — or drafts the report for the owner's decision and the review gate.
- Leaving the fallback documented without that reference is the gap the rule exists to close; the fallback itself stays
  either way.

## Non-goals

- Committing either per-user configuration file. `~/.config/opencode/opencode.jsonc` and `~/.jetbrains/acp.json` are the
  reader's own state, and neither is committed.
- Editing those files by any route other than the setup skill's procedure. On the owner's instruction this unit's
  delivery does two such edits on their machine: it switches the global config from the `steroid` entry to the built-in
  server (task 6.4) and registers the ACP agent to observe a session (task 6.5), each with their confirmation and each
  reversible by restoring the entry.
- Extending the guidance to IDEs other than IntelliJ IDEA, or to agent hosts other than Air.
- Maintaining a version matrix. D8's boundary is the tested versions plus a discovery step; a release outside them is
  reported unsupported, not researched.

## Claims table

| #   | Claim                                                                                                                                                                                                                           | Source                                                                                                                                                                                                                                                                                                                                                                           |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Steroid 0.102.0 fails on IDEA 2026.3 with `NoClassDefFoundError: com/fasterxml/jackson/databind/ObjectMapper`                                                                                                                   | A `steroid_execute_code` call of `println(project.name)` on 2026-11-06 returned the error in `McpScriptContextImpl.<init>`, naming plugin version `0.102.0-r-c68d8f15d`                                                                                                                                                                                                          |
| 2   | IDEA 2026.3 bundles `com.intellij.mcpServer` 263.7307.200 (the user-installed copy is 263.7307.206)                                                                                                                             | `META-INF/plugin.xml` read from `IntelliJ IDEA.app/Contents/plugins/mcpserver-plugin/lib/*.jar` and from the user plugin directory                                                                                                                                                                                                                                               |
| 3   | The server answers `initialize` on `http://127.0.0.1:64342/stream` at protocol `2025-06-18`                                                                                                                                     | The `initialize` response: `serverInfo.name` = `IntelliJ IDEA MCP Server`, `serverInfo.version` = `2026.3`                                                                                                                                                                                                                                                                       |
| 4   | `tools/list` returns 66 tools, including the inspection, refactoring, formatting, build, and debugger tools the text names                                                                                                      | The `tools/list` response over the same session; the count is the length of its `tools` array                                                                                                                                                                                                                                                                                    |
| 5   | No screenshot and no UI-input tool among them                                                                                                                                                                                   | The same 66 names, read in full                                                                                                                                                                                                                                                                                                                                                  |
| 6   | `run_ide_script` is absent from a default `tools/list` and gated by `mcp.ide.script.toolset.enabled`                                                                                                                            | `javap -c` of `IdeScriptToolset.isEnabled` in the plugin jar, which reads that registry key and the `idea.mcp.server.ide.script.unattended` property                                                                                                                                                                                                                             |
| 7   | The tool description says `run_ide_script` is "Available only in Brave Mode"                                                                                                                                                    | `tool.description.run_ide_script` in the plugin's `McpServerBundle.properties`                                                                                                                                                                                                                                                                                                   |
| 8   | The settings page is named "MCP Server" and carries "Enable MCP Server", `Port:`, "Copy server URL", "Copy HTTP Stream Config", and an Exposed Tools section                                                                    | `McpServerBundle.properties` keys `mcp.server.configurable.name`, `enable.mcp.server`, `mcp.server.global.port.label`, `mcp.server.popup.copy.config.menu.url`, `copy.mcp.server.stream.configuration`, `configurable.mcp.tool.filter`                                                                                                                                           |
| 9   | OpenCode is not among the IDE's auto-detected clients                                                                                                                                                                           | No `opencode` string in any of the plugin's jars                                                                                                                                                                                                                                                                                                                                 |
| 10  | A `remote` server entry is the established shape for this client                                                                                                                                                                | The `amplicode` entry in `~/.config/opencode/opencode.jsonc`: `{"type": "remote", "url": "http://127.0.0.1:64443/stream"}`                                                                                                                                                                                                                                                       |
| 11  | `assert-paketo-pin-coverage` shipped, so the idea's gate is met                                                                                                                                                                 | `openspec/changes/archive/2026-11-06-assert-paketo-pin-coverage/`, merged as commit `9ac45da7` (#848)                                                                                                                                                                                                                                                                            |
| 12  | No capability spec names the agent-tooling MCP setup                                                                                                                                                                            | `grep -rn "setup-agent-tools" openspec/specs/` returns no hits; the corpus's `mcp` mentions are three passages in `showcase/quality/agent-skills` (its Purpose, the technical advisor's tool-deny list, and the failed-server recovery requirement)                                                                                                                              |
| 13  | Every live Steroid reference is one this change names or a test fixture                                                                                                                                                         | `git grep -ni steroid -- ':!openspec/changes/archive' ':!docs/units' ':!openspec/changes/adopt-builtin-ide-mcp-server'` lists `AGENTS.md`, the setup skill, the setup command, `docs/ideas.md`, and `scripts/test-reconnect-mcp.py`, whose server names are fixtures; the change's own artifacts and the IDE-generated, git-ignored `.idea/mcp-steroid.md` are outside the scope |
| 14  | The IDE-script confirmation is a per-script dialog, and Brave Mode skips the terminal-command and run-configuration confirmations                                                                                               | `label.do.you.want.to.run.ide.script`, `ide.script.confirmation.allow.for.session`, and `checkbox.enable.brave.mode.skip.command.execution.confirmations` in `McpServerBundle.properties`                                                                                                                                                                                        |
| 15  | JetBrains Air is bundled with IDEA 2026.3 as `com.intellij.air`                                                                                                                                                                 | `META-INF/plugin.xml` from `IntelliJ IDEA.app/Contents/plugins/air-plugin/lib/air-plugin.jar`: `<id>com.intellij.air</id>`, `<version>263.7307.200-mac-arm64</version>`                                                                                                                                                                                                          |
| 16  | The installed OpenCode exposes an ACP server subcommand                                                                                                                                                                         | `opencode --version` = `2.2.5`; `opencode --help` lists `acp — Start an Agent Client Protocol server`                                                                                                                                                                                                                                                                            |
| 17  | Air reads `~/.jetbrains/acp.json`, whose keys are `agent_servers`, `default_mcp_settings`, and `remote`, and whose agent entry carries `command`, `args`, `env`, `use_idea_mcp`, `use_custom_mcp`, and `idea_mcp_allowed_tools` | `user.home`, `.jetbrains`, and `acp.json` in `com.intellij.air.acp.runtime.config.AcpConfigurationLoader`; the descriptor strings in `AcpConfiguration$$serializer`; the fields of `AcpConfiguration` and `AgentServerConfig`                                                                                                                                                    |
| 18  | The parked idea's JSON is an agent-entry body, not a file                                                                                                                                                                       | It carries only the entry's keys and omits the `agent_servers` wrapper the serializer's descriptor names (row 17)                                                                                                                                                                                                                                                                |
| 19  | The README already documents an optional per-user MCP and IDE-bound tooling                                                                                                                                                     | `README.md`'s `#### Tooling MCP Servers` lists the project-configured Playwright and the optional per-user GitHub, and `### Formatting and IDE Setup` documents `setup-idea.sh` and the IntelliJ plugins                                                                                                                                                                         |
| 20  | `use_idea_mcp` is parsed into `IdeaMcpMode` (`OFF`, `ON`, `ON_DEMAND`), and the ACP config factory reads `use_idea_mcp`, `use_custom_mcp`, and `idea_mcp_allowed_tools` from the agent entry and from `default_mcp_settings`    | `com.intellij.air.acp.runtime.settings.IdeaMcpMode` and its serializer in the Air plugin; `LocalAcpAgentConfig$Companion` reading `AgentServerConfig.getUseIdeaMcp`/`getUseCustomMcp`/`getIdeaMcpAllowedTools` and `DefaultMcpSettings`'s same getters; what each field does to a session is unverified until the live check                                                     |
| 21  | The IDE build under test is `IU-263.7307.200`                                                                                                                                                                                   | `product-info.json` in `IntelliJ IDEA.app/Contents/Resources`: `productCode` = `IU`, `buildNumber` = `263.7307.200`, `version` = `2026.3`                                                                                                                                                                                                                                        |
| 22  | The Steroid plugin's owning project is `jonnyzzz/mcp-steroid`, whose issue tracker is the upstream-report venue                                                                                                                 | The plugin's `META-INF/plugin.xml` names `<vendor>jonnyzzz.com</vendor>` and `<id>com.jonnyzzz.mcp-steroid</id>`, and the plugin's generated `.idea/mcp-steroid.md` names `https://github.com/jonnyzzz/mcp-steroid/issues`; task 6.7 confirms the repository resolves before searching it                                                                                        |
| 23  | Air's injected IDE server appears to an Air session as `jetbrains_air_ide`, whose `execute_tool` routes to the IDE's tools, and with the injection off one IDE server serves both hosts                                         | An Air session on IDEA 2026.3, 2026-11-06: with `use_idea_mcp: true` and no `intellij` entry, `get_project_modules` through `jetbrains_air_ide.execute_tool` returned the worktree's 83 modules; with `use_idea_mcp: false` and the entry present, the same call returned the module list and the tool set was not doubled                                                       |
| 24  | OpenCode 2.2.5 reflects a new `mcp` entry without a restart, and an IDE MCP endpoint that accepts connections but never answers is restored by toggling the IDE's MCP Server                                                    | This session: after the config edit `opencode mcp list` reported `intellij` connected and the session's tool list gained its 66 tools with no restart; POST `initialize` timed out for several minutes until the owner toggled the status-bar MCP Server, after which it answered in ~14 ms                                                                                      |

## Dismissals

`python3 scripts/claim-scan.py --base origin/main` seeds the table above; its candidates are accounted for by kind
rather than one by one:

- **`[path]`** — each candidate is the coordinate its sentence names, resolved by opening that path or by re-running the
  row's command; it asserts no fact on its own.
- **`[count]`** — each count is a value the sentence sources from a row, or a coordinate into the unit's own records (a
  task or round number); none states a fact the row or record it names does not carry.
- **`[absolute]`** — each limit or absence is the subject of the decision that states it, bounded by the row or the
  artifact its sentence names.
- **`[cross-ref]`** — each names a file, section, task, or row that the repository or this change holds, and is resolved
  by reading it.

## Verification

- The two `AGENTS.md` rules, the README entry, and the skill/command text are read back against the source tables above;
  a claim whose row does not reproduce is deleted rather than reworded.
- `./gradlew spotlessApply` then `spotlessCheck` cover the markdown reflow, and `./gradlew check` covers the
  definition-inventory assertion over the edited command and skill.
- The `grep` in row 13 is re-run after the edits: no live file still presents Steroid as the repository's IDE path.
- The acceptance set settles what no artifact can, with one harmless IDE operation against an identified project in each
  case: the client path from a session, the Air cases from Air — with the global `intellij` entry removed for the test
  and re-added afterwards (the only case that proves Air's own injection), and in the reader's final configuration —
  plus a check that the hand-added agent appears in Air's list. The observed cases and the agent listing are recorded on
  the task, and the skill states them — not the alternatives D7 lists while the question is open.
- The client switch is verified on its own surface rather than by reading the file back: after the edit and an OpenCode
  restart, `opencode mcp list` reports the `intellij` server connected and one IDE operation from a session returns a
  result; a connected server alone does not prove the wiring.
- The skill states the versions its recipe was verified against and the discovery step that reads the installed server's
  tools and ACP schema, with the unsupported branch for an unverified release (D8), and it carries the upstream
  reference or the recorded decision not to file one (D9).
