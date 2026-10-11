# Proposal

## Why

The `anomalyco/opencode/github` action runs OpenCode **v1**, whose handler resolves no non-`question` permission `ask`,
so an unattended cloud run that hits an `ask` hangs (the scheduled `audit` hung 2h13m on 2026-10-10). Upstream declined
the fix for v1 — `anomalyco/opencode#54477` is `wont_fix`: v2's `opencode run` resolves asks, but the action has no v2
path, and v1 takes critical fixes only — and recommended the workaround: resolve every permission to `allow`/`deny` in
the config the action uses so nothing asks, and bound the job with `timeout-minutes`. The `audit` job's bound landed
with the fix (`timeout-minutes: 60`); the `/oc` job has none, and three config defaults still ask in a cloud run —
`read` `.env`, `external_directory`, and `doom_loop`, the first and last resolved here.

## What Changes

- `.opencode/opencode.json`: resolve the ask-defaults whose internal allows are re-expressible — `read` of `.env` files
  to `deny` (keeping the `*.env.example` allow), and `doom_loop` to `deny` — so they no longer raise an `ask` in a cloud
  run. `external_directory` keeps its grants: denying its `*` would override the defaults' scratch/skill/reference
  allows, which the config cannot re-express.
- `.github/workflows/opencode.yml`: bound the `opencode` (`/oc`) job with `timeout-minutes`, so a residual `ask` fails
  within the bound rather than hanging.
- `AGENTS.md`: keep the `anomalyco/opencode#54477` reference's retire-when clause keyed on the handler resolving a
  non-`question` `ask` (the action is v1-coupled; there is no v2 action path), record the `wont_fix` and the workaround,
  and add the workaround to the captured gotcha.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. No capability spec owns the config's permission rules — the specs cover the config's _shape_
(`code-quality`'s formatting, `merge-governance`'s loadability probe), not the rule values — and resolving a rule
changes no specified scenario's outcome. (Cf. `retire-opencode-permission-plugin`.)

## Impact

- Files: `.opencode/opencode.json`, `.github/workflows/opencode.yml`, `AGENTS.md`.
- No Java, build, test, or deployment impact; the cloud workflows' triggers and the audit report are unchanged.
- Effect: in a cloud run (v1) the `read` `.env` and `doom_loop` asks become denials; locally (v2) a `.env` read is
  denied (was prompted) and `doom_loop` is denied (was allowed).
- Residual: an unlisted external-directory path and the `git push *` guard still ask in a cloud run, both bounded by the
  job timeouts.
