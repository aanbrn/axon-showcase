# Proposal

## Why

An OpenCode agent definition whose frontmatter YAML fails to parse is not dropped from the inventory — it loads with its
fields silently discarded, so `readme-auditor` lost its `mode: subagent` and `/audit-readme` failed with "cannot run as
a subagent" until the clause was rephrased by hand. The CI probe that loads `.opencode/` through the action's consumer
runs only when `.opencode/opencode.json*` or `.github/workflows/ci.yml` changes, so the pull request that broke the
agent never exercised it, and the probe asserts the load only (exit status) — which a silently-degraded definition
passes. The same multi-line shape silently strips a command's `description` and removes a skill from the inventory (exit
0), and none of the three is gated today.

## What Changes

- `.github/workflows/ci.yml`: widen the OpenCode probe's changed-path gate from `.opencode/opencode.json*` to all of
  `.opencode/`, and extend the probe to assert every project agent, command, and skill definition loads with its
  `description` intact — reading the resolved inventory from `opencode debug config` (its `agent`/`command` maps) and
  `opencode debug skill` — while keeping the existing exit-status check that already catches a malformed command or
  config.
- `README.md`: correct the Continuous Integration paragraph — the probe's scope (the inventory, not just the config) and
  its trigger (any `.opencode/` change).
- `AGENTS.md`: correct the `ci.yml` gate bullet and the config-read-path gotcha, so the documented trigger/scope matches
  the probe and the "exit status is the signal" clause names its blind spot.
- `docs/ideas.md`: remove the implemented idea.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/quality/merge-governance`: the pull-request fast gate's OpenCode verification broadens from "the
  `.opencode/opencode.json*` configuration is loadable" to "the configuration and the agent/command/skill inventory are
  loadable", with its trigger widened to any `.opencode/` change; a new scenario covers a definition the loader silently
  degrades.

## Impact

- **Files**: `.github/workflows/ci.yml`, `README.md`, `AGENTS.md`, `docs/ideas.md`, plus the change dir and the
  `merge-governance` delta.
- **Build / tests / services**: no Java, dependency, or service change; `check` is unaffected (the probe needs the
  `opencode` binary, which `check` must not require, so it stays in `ci.yml`).
- **CI cost**: the probe's binary install (a network fetch) now runs on any pull request that touches `.opencode/`, not
  only config/`ci.yml` ones.
- **Verification**: the probe is proven against the pinned consumer (`releases/latest`, currently `v1.18.34`) with a
  known-bad and a known-good definition, and its run-and-fail path locally; the pull request's own run exercises
  run-and-pass.
