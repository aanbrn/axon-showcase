## Context

The `openspec/specs/showcase/gateway/` group holds two capabilities: `api-gateway` (the REST entry point) and
`live-events` (the SSE stream). The name `gateway/api-gateway` repeats "gateway", and the spec's `#` header still says
the pre-restructure path `showcase/api-gateway` — drift from the 2026-08-14 restructure that moved the file without
updating its title. This change renames the capability to `rest-api` and fixes the title.

## Goals / Non-Goals

**Goals:**

- Rename the capability path `showcase/gateway/api-gateway` → `showcase/gateway/rest-api` via `git mv` (history kept).
- Fix the spec's internal header to the new path (`# showcase/gateway/rest-api Specification`).
- Keep all requirement/scenario/behavior text unchanged.

**Non-Goals:**

- No change to the `showcase-api-gateway` module name or any deployment/chart references to the api-gateway service
  (those name the deployed service, not the spec capability).
- No rename of the `gateway` role group or the `live-events` capability.

## Decisions

### D1: Pure relocation with a title fix, `skip_specs: true`

`git mv` the directory, update the `#` header to the new path, change nothing else. Like the 2026-08-14 restructure,
this is a pure relocation, so the change declares `skip_specs: true` (no delta spec; the artifact graph skips `specs`).

### D2: Leave the module/deployment naming alone

The `api-gateway` service (module `showcase-api-gateway`, image `aanbrn/axon-showcase-api-gateway`, chart references) is
unchanged — the rename targets the spec capability path only. Deployment specs continue to reference the api-gateway
service by name; they are not affected.

## Risks / Trade-offs

- **Future delta path references** → any change that would have cited `showcase/gateway/api-gateway` must now cite
  `showcase/gateway/rest-api`. No active changes reference the old path, so the switch is clean; `openspec validate` at
  archive confirms the capability resolves under its new path.
