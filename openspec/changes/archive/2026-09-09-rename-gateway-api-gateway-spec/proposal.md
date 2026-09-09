# Proposal: Rename the gateway/api-gateway capability to rest-api

## Why

The capability path `showcase/gateway/api-gateway` is redundant — "gateway" appears twice (the `gateway` role group
contains the `api-gateway` capability, which is its REST entry point, beside the `live-events` SSE capability). The
capability is the REST facade (`/showcases` command and query endpoints); `rest-api` names it without repeating the
role. The rename also fixes stale internal drift: the spec's `#` header still says the pre-restructure path
`showcase/api-gateway` (the 2026-08-14 restructure moved the file but never updated the title).

## What Changes

- Rename the capability directory `openspec/specs/showcase/gateway/api-gateway/` → `.../gateway/rest-api/` (via
  `git mv`, preserving history).
- Update the spec's internal header from `# showcase/api-gateway Specification` to
  `# showcase/gateway/rest-api Specification` (fixing the stale pre-restructure title to match the new path).
- No requirement, scenario, or behavior text changes inside the spec — pure relocation + title fix, declared via
  `skip_specs: true`.
- The _module_ `showcase-api-gateway` and all deployment/chart references to the api-gateway service stay unchanged —
  only the capability path (and its title) renames.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/gateway/rest-api` — the capability is renamed from `showcase/gateway/api-gateway` (same content, new path
  and title). This is a path/capability rename, not a behavior change.

## Impact

- **Specs**: the capability path `showcase/gateway/api-gateway` → `showcase/gateway/rest-api`; future deltas must cite
  the new path. No active changes reference the old path (verified).
- **Docs**: no `docs/ideas.md` or `AGENTS.md`/`README.md` change (the module and deployment naming is unaffected).
- **Behavior**: none — a spec relocation with a title fix.
