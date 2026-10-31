# Decision record — the session-move permission anomaly

Date: 2026-10-31

Status: a bounded exploration. It binds no standing rule; it records what the investigation established.

## The question

The reuse pilot's sandbox control set `permission.session_move: deny`, the rule normalized, and the
`opencode.session_move` tool still moved the session to `/tmp`. Is that a security report, a functional bug, a
configuration-validation defect, or a documented limitation — and where does it belong?

## The evidence

- **The tool definition.** `packages/core/src/tool/plugin/opencode.ts` (the `dev` branch, fetched 2026-10-31) defines
  `session_move` with `options: { namespace: "opencode", codemode: true, pinned: true }` — no `permission` option and no
  permission assertion. `worktree_create` carries the same options. `worktree_remove` carries
  `options: { namespace: "opencode", codemode: true, pinned: true, permission: "edit" }` and asserts that action.
- **The schema is intentionally open.** The V1 `permission` object decodes with `Schema.ObjectKeyword`
  (`packages/core/src/v1/config/permission.ts`), and the V2 `permissions` rules carry `action` as a string, not an enum
  (`https://opencode.ai/config.json`, `Permission.Rule`). The V2 permissions page states it: "V2 action names are
  strings, so plugins may define more actions."
- **The docs' built-in action list.** The V2 permissions page lists the built-in actions (`read`, `edit`, `glob`,
  `grep`, `shell`, `subagent`, `skill`, `question`, `webfetch`, `websearch`, `external_directory`, …); `session_move` is
  not among them.
- **The tools doc states the behavior.** `services/www/src/docs/content/tools.mdx` says under Sessions: "These utilities
  do not request a built-in permission action." Under Worktrees: "`worktree_remove` uses the `edit` permission with the
  worktree directory as its resource. The other worktree utilities do not request a built-in permission action."
- **The observation.** On OpenCode v2.2.5, `opencode debug config` normalized
  `{"action":"session_move","resource":"*","effect":"deny"}`, and the control called
  `opencode.session_move({ directory: "/tmp" })`, which returned
  `{"sessionID":"ses_e701c34b4ffevvM1CxvpM0gsbr","directory":"/tmp"}`. The evidence is
  `docs/decisions/2026-10-31-process-reuse-pilot.md` finding 3 and `docs/process-defects/probe-pause-resume-reuse.md`
  #2.
- **The owning repository.** `npm view @opencode-ai/cli repository.url` returns
  `git+https://github.com/anomalyco/opencode.git`, and its `homepage` is `https://github.com/anomalyco/opencode#readme`.
  The observed binary is the Homebrew `opencode-v2` formula at 2.2.5.
- **The threat model.** `SECURITY.md` says: "OpenCode does **not** sandbox the agent. The permission system exists as a
  UX feature to help users stay aware of what actions the agent is taking … it is not designed to provide security
  isolation." Its out-of-scope table lists "Sandbox escapes" and "Malicious config files".

## The class

- **A documented limitation, not a defect.** The tool does not request a permission action, and the tools doc says so.
  The schema's open action set is deliberate — plugins may define actions — so accepting `session_move` is not a
  validation bug. A rule for an action no built-in tool checks is inert, and nothing in the contract says otherwise.
- **Not security.** `SECURITY.md` states the permission system is not a sandbox and lists sandbox escapes as out of
  scope; the observation must not be framed as a security issue.
- **No upstream report.** A feature request ("warn when a rule names an action no built-in tool checks") is conceivable,
  but the investigation establishes no defect: the behavior is documented and the open set is intentional. Reporting it
  would ask the maintainers to change a documented design.

## The verdict

No report. The `docs/ideas.md` entry is removed as investigated and decided against. The durable lesson is recorded in
`AGENTS.md`'s agent-tooling gotchas: a permission action is enforced only by a tool that consults the engine, so a
`session_move` rule is not sandbox isolation.

## Reopening condition

- **A release gates `session_move` / `worktree_create`** — recheck the limitation and update the `AGENTS.md` lesson
  (external coordinate: the `anomalyco/opencode` release notes).
- **A release warns on a rule for an action no built-in tool checks** — the diagnostic concern is satisfied; retire it
  (external coordinate: the `anomalyco/opencode` release notes).
- **A maintainer states the inert rule is unintended** — reconsider a report (external trigger: a maintainer's reply).
- **The docs list `session_move` as a built-in action and the tool consults it** — retire the lesson (external
  coordinates: `opencode.ai/v2/docs/permissions` and `packages/core/src/tool/plugin/opencode.ts`).
