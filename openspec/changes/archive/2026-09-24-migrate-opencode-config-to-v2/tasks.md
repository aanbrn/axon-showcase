# Tasks

## 1. Migrate the project config

- [x] 1.1 In `.opencode/opencode.json`, rewrite `small_model` to its native slot — `agents.title.model` — keeping the
      same model id.
- [x] 1.2 Rewrite the flat `mcp.playwright` entry to `mcp.servers.playwright`, dropping the v1 `enabled` flag.
- [x] 1.3 Confirm the file is valid JSON, that no legacy key remains (`small_model`, a flat `mcp.<name>` entry, the v1
      `permission` object), and that `model`, `permissions`, and `$schema` are untouched.

## 2. Docs the change owns

- [x] 2.1 Update the `AGENTS.md` model-pin bullet, whose `.opencode/opencode.json` key enumeration names `model` and
      `small_model` — the latter is now `agents.title.model`.
- [x] 2.2 Update the README's global-config MCP snippet to the v2-native `mcp.servers` shape.
- [x] 2.3 Update the project-authored `.opencode/skills/setup-agent-tools/SKILL.md`: its fallback says to add an entry
      "under the `mcp` object" (native is `mcp.servers`), and its Notes say an entry with no `enabled` key is enabled by
      default (the native field is `disabled`).
- [x] 2.4 Update the README's Prerequisites OpenCode row: require v2 (or later) and point the Install cell at the v2
      package — `anomalyco/tap/opencode-v2` is v2.0.15 while `brew install opencode` (and `anomalyco/tap/opencode`) is
      v1.18.x, which rejects the config's `permissions` key outright; verify the Desktop path before writing it.
- [x] 2.5 (Folded capture) Merged the three `lesson-capture` rules into the `opencode debug config` gotcha (its output
      is a _per-source normalized_ document, not a literal input, and its `info` can render credentials) and the scratch
      bullet (a v2-only key makes the tool's major version a repo prerequisite), each carrying
      `captured: migrate-opencode-config-to-v2`.

## 3. Verify the surrounding artifacts (no edit expected — confirm, do not assume)

- [x] 3.1 Confirm the change owes no spec delta: the `code-quality` scenario "The .opencode configuration JSON is
      format-gated" covers the file's formatting, not its keys, and this change keeps it Prettier-formatted — so
      `skip_specs` is correct. (Do not claim "no spec covers the config"; one does, for a different property.)
- [x] 3.2 Grep `AGENTS.md`, `README.md`, and the project-authored `.opencode/` files for `small_model`, a flat `mcp`
      entry, and the `enabled` MCP key, and reconcile each the change makes stale.

## 4. Verification

- [x] 4.1 Captured `opencode debug config`'s project document before and after: `agents.title.model` is identical, and
      `mcp.servers.playwright` is behaviourally equivalent (the native form omits the default `disabled: false` the
      legacy form emitted); no `omitted unsupported legacy …` diagnostic appears for the project file.
- [x] 4.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` after the final edit (the change is agent
      tooling, so no code tests are affected).
- [x] 4.3 Ran `review-quick` over the implementation diff to clean (three rounds; the finding was the literal-identity
      claim, swept from every site) — the manual review pass is being requested before committing.
