# Design

## Context

See `proposal.md` — Why. `opencode debug config` shows the normalizer's output for the current file: `small_model`
becomes `agents: { title: { model: { providerID, model } } }`, and the flat `mcp.playwright` becomes
`mcp: { servers: { playwright: { …, disabled: false } } }`. The v2 config docs give the native **input** shapes
(`agents.<id>`, `mcp.servers.<name>`), and the binary emits four legacy diagnostics
(`omitted unsupported legacy model reference` / `… model variant` / `… setting`, and
`omitted enabled-only legacy MCP entry`) for values it cannot map — a silent drop with only a log line.

## Goals / Non-Goals

**Goals:**

- Replace the two legacy keys with the native shapes `opencode debug config` already produces, preserving behaviour.
- Leave nothing in the project config that depends on the compatibility shim.

**Non-Goals:**

- Migrating what is already native (`model`, `permissions`, `$schema`).
- Changing the user's global config, or which MCP servers exist — the change corrects the instruction surfaces that
  describe the shape (the README's global-config snippet and the `setup-agent-tools` skill), not the user's config.

## Decisions

**Target the shapes `debug config` emits.** They are the native forms the normalizer produces, so the migration is
verifiable: the normalized output must be behaviourally identical — the only delta is the `disabled` default the
normalizer materializes for the legacy form and the native form omits. _Alternative rejected:_ leave the legacy keys —
they work today, but only through the shim.

**`agents.title.model`, not an equivalent elsewhere.** v2 has no `small_model`; the normalizer maps it to the `title`
agent (the built-in agent that names sessions), so that is the native slot. _Alternative rejected:_ drop the setting —
the small model should stay pinned.

**Omit the MCP `enabled` flag.** v2's native field is `disabled` (the docs document it, defaulting to `false`), so
`disabled: false` is a valid native input too; it is omitted because the docs' example omits it when the server is
enabled, and doing so normalizes to an entry with no `disabled` key — behaviourally the same as the current file's
`disabled: false`. _Alternative rejected:_ leave the v1 `enabled` key for the normalizer to translate.

**Fix the instruction surfaces too** — the README's global-MCP snippet and the `setup-agent-tools` skill both show the
v1 flat shape or an `enabled` key; the native form is `mcp.servers` with `disabled`-when-off. They are instructions, not
repo settings, but they would otherwise teach the legacy shape.

**State the v2 requirement in the README.** The config carries the v2-only `permissions` key, which v1 rejects with a
hard error (`V2 permissions are not supported by OpenCode V1`), and the README's OpenCode row still installs v1
(`brew install opencode` → 1.18.30), so the row must state the requirement and point at the v2 formula
(`anomalyco/tap/opencode-v2` → 2.0.15). _Alternative rejected:_ leave the README version-generic — a v1 user's config
fails at startup.

## Risks / Trade-offs

- **A wrong native key silently changes behaviour** → a verification task compares `opencode debug config` before and
  after; the normalized `agents.title.model` must be unchanged and `mcp.servers.playwright` behaviourally equivalent
  (the native form omits the default `disabled: false`).
- **The instruction surfaces target the user's global config, not the repository** → fixed as instructions; they set no
  repo behaviour.
- **The shim keeps working meanwhile** → the change is behaviour-preserving and low-urgency, shipped for future-proofing
  and to remove the silent-drop hazard.
- **The Desktop v2 install path is unverified** (the TUI formula `anomalyco/tap/opencode-v2` = 2.0.15 is) → task 2.4
  verifies it against opencode.ai/v2 before writing it, and leaves that cell unchanged if no v2 path exists.

## Migration Plan

Not applicable beyond the config edit: v2 reads both shapes, so the change takes effect on reload with no behaviour
change. Rollback is reverting the commit.
