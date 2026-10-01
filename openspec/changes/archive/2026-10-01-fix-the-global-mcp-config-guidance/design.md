# Design

## Context

See `proposal.md` — Why. Facts measured while investigating the IDEA failure (2026-10-01):

- The global config has **two** consumers on this machine: the local CLI (`opencode` 2.0.21, Homebrew `opencode-v2`) and
  the OpenCode ACP agent IDEA's AI launches — **1.18.15**, installed from JetBrains' ACP registry
  (`~/Library/Caches/JetBrains/IntelliJIdea2026.2/acp-agents/opencode/1.18.15/opencode`). The 1.x line is also what npm
  `opencode-ai@latest` publishes (`1.18.34`; no `2.x` versions under that name).
- 1.x rejects the v2-only spellings: a `permissions` array exits 1 with `Unrecognized key: permissions`, and an
  `mcp.servers` key with `Missing key mcp.servers.enabled`. A **flat `mcp` map plus `permission`** exits 0 under 1.x and
  loads under 2.x (which is what this repo's project config already uses).
- `opencode mcp add` (2.0.21) defaults to the **project** config — `--global` writes the global one — and it writes the
  `servers` key: into an empty global config as `mcp.servers`, and into an existing flat `mcp` map as a nested
  `mcp.servers` _inside_ it. Either result breaks 1.x.
- Upstream: `anomalyco/opencode#49904` (the `mcp add` placement; commented with the 1.x impact and reproduction),
  `#52177` (the V2 MCP docs page disagrees with the published schema and the runtime on this shape), `#43748` (the
  published `$schema` is a hybrid V1 shape).
- **Two prior decisions reconciled this same drift.** `migrate-opencode-config-to-v2` (#391) deliberately changed the
  README's global snippet **from** the flat `mcp` object **to** `mcp.servers` (its task 2.2), and
  `fix-cloud-agent-config-for-v1` (#438) then re-established the one-shape V1 decision for the project config. This
  change follows #438's direction and reverses #391's README edit — the V2 docs page #391 followed is what `#52177` now
  reports as the outlier.
- **The probe that established `mcp add`'s default also wrote into this repository.** With no `--global`,
  `opencode mcp add probe -- gh mcp` appended a `probe` server to `.opencode/opencode.json` (the project config); the
  stray entry was reverted with `git checkout --` (the file carried no other uncommitted edits). That write is why the
  guidance names the `--global` flag and the probe hazard is recorded in `AGENTS.md`.

## Goals / Non-Goals

**Goals:**

- The told path to a working global config produces one shape, and that shape is the flat `mcp` map plus `permission`.

**Non-Goals:**

- Changing any config file on a contributor's machine (the skill already asks before touching files outside the repo).
- Upstream fixes (`#49904`, `#52177`, `#43748`) — reported/commented, not solved here.
- The project config's own shape: it is already the flat/V1 shape, which is why the IDEA agent reads it.

## Decisions

- **Keep the global config in the V1 shape (flat `mcp`, `permission`) rather than the v2-native spellings.** It is the
  one shape both lines load; the v2 spellings buy nothing here and cost the 1.x consumer. Options considered: the
  v2-native shape (`mcp.servers`, `permissions`) — rejected, it breaks 1.x (`#49904`/`#43748` are the upstream
  symptoms); maintaining two configs (v1 for the agent, v2 for the CLI) — rejected, the CLI reads the V1 shape too.
- **The skill adds the entry by editing the flat `mcp` map, not by `opencode mcp add`.** The command's output must be
  normalized anyway, and its written shape is itself under dispute (`#52177`). Options considered: `mcp add --global`
  then move the entry out of `servers` — kept as the fallback for a user who prefers the CLI, but not the primary path.
  Retire this decision when `#49904` makes `mcp add` emit the flat shape.
- **Correct the `mcp add` claims where they stand instead of deleting them.** The `AGENTS.md` gotcha that probed
  `mcp add`'s behaviour ("writes the global config", "the `-- <command>` form is not shown in `--help`") is now stale
  for 2.0.21 — the command defaults to the project config and does list the form — and it is the very claim that led the
  skill astray; it is corrected in place.
- **Re-probe and re-state the "legacy/native" passage (`AGENTS.md:2456-2457`) rather than leave it.** It calls the flat
  `mcp` form "legacy" and `mcp.servers.<name>` "native", which sits against the rule this change establishes, and pins a
  `disabled: false` detail to v2.0.15. Options considered: leaving it (rejected — a durable artifact contradicting the
  change's subject); deleting it (rejected — the normalized-document difference it records is still useful).

## Risks / Trade-offs

- **The upstream fix may land and make the flat-shape instruction redundant.** → The decision records the retirable
  condition (`#49904`), so the instruction can go back to the one-liner when `mcp add` emits the flat shape.
- **A contributor may still run `mcp add` themselves.** → The skill says what it writes and how to correct it, so the
  result is recoverable rather than silently broken.

## Migration Plan

- Docs only: the skill, `README.md`, and `AGENTS.md`. No rollout; nothing to roll back.
