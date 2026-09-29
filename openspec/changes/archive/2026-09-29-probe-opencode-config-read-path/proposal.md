# Proposal

## Why

`.opencode/opencode.json` must stay loadable by the consumer the `opencode` GitHub action installs — the action resolves
`releases/latest` (the V1 line) and its V1 binary rejects a V2-only `permissions` array at startup — but no in-repo
check loads it: `workflowLint` reads YAML only and Prettier parses the JSON without V1 semantics, so a V2-only key
passes every gate and silently kills both cloud workflows until a dispatch (they were dead 2026-09-23→09-28, recorded in
#438/#440). The `build` gate already probes the OpenSpec config's read path; the OpenCode config has no equivalent.

## What Changes

- `.github/workflows/ci.yml` — the `build` job gains a probe step: on a pull request that changes
  `.opencode/opencode.json*` or the probe's own `.github/workflows/ci.yml`, install the same consumer the action
  installs (`curl -fsSL https://opencode.ai/install | bash`) and run `opencode debug config`, failing on a non-zero
  exit. The step exits early when neither changed, so no install runs otherwise, and it is PR-scoped (a config is probed
  at the pull request that changes it, and an edit to the probe is probed by its own pull request).
- `openspec/specs/showcase/quality/merge-governance/spec.md` — delta: the fast-gate requirement's configuration
  verification covers the OpenCode config's read path, with a scenario making a V1-unloadable config fail the check (the
  requirement's description, the trigger and failure scenarios, and a new scenario change).
- `AGENTS.md` — the PR-gate enumeration gains the probe, the V1-shape bullet's "no in-repo gate loads the config with
  the action's v1 binary" absence claim is corrected, and the `main`-gate sentence that says it runs "the same … config
  probe as the pull-request path" is qualified (the OpenCode probe is PR-scoped).
- `README.md` — the Continuous Integration section names the probe.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/merge-governance`: the "Pull requests run the fast quality gate" requirement gains the
  OpenCode-config read-path probe.

## Impact

- `.github/workflows/ci.yml` — one probe step in `build`.
- `openspec/specs/showcase/quality/merge-governance/spec.md` — the requirement's description, the trigger and failure
  scenarios, and a new scenario.
- `AGENTS.md` / `README.md` — the gate's description and the corrected absence claim.
- No application code, build, runtime, or deployment change.
