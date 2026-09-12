## Why

The first `/audit-specs` run (the `specs-auditor` subagent added in `add-specs-auditor-agent`) found verified structural
drift in the `openspec/specs/` corpus — drift that passes `openspec validate` because it is not the corpus-level
consistency the validator checks. Left unaddressed, the specs misdescribe the system to both readers and the agent that
loads them: two `#` titles do not match their capability path, one Purpose describes a pre-web-UI scope, one requirement
enumerates a subagent set a later change outgrew, one client's Purpose claims a resilience behavior the spec never
formalized, one spec mixes `MUST` into a corpus that otherwise uses `SHALL`, and one enumerates coordinates with
inconsistent spelling.

## What Changes

- **Two stale `#` titles (title-only, no delta):** `showcase/quality/ide-config` and
  `showcase/quality/infra-image-versions` carry non-path titles (`# Ide Config Specification`,
  `# Infra Image Versions Specification`), a leftover that predates the 2026-08-14 role-group restructure. Edit each
  first line to `# showcase/<path> Specification` in the main spec, as `fix-stale-spec-headers` did (a `skip_specs`
  precedent: a title is not a requirement, so no delta block covers it).
- **`deployment/helm-chart` Purpose:** "the four showcase services" is outgrown — the spec's own `Service deployments`
  requirement enumerates five (command, query, projection, api-gateway, web-ui), and the chart renders five. Correct the
  Purpose to describe the services it actually deploys.
- **`quality/agent-skills` local-subagents requirement:** the
  `Per-change quality-gate and analysis subagents are available` requirement enumerates `review-quick`,
  `review-thorough`, `lesson-capture`, `vision`, `diagrammer` — omitting `experience-analyzer` and the two auditors,
  each of which has its own requirement in the same spec. Reframe the requirement so it no longer carries a stale
  enumeration (describe the per-change set and cross-reference the standalone requirements), rather than adding a fourth
  list to keep in sync.
- **`clients/command-client`:** the Purpose claims "Resilience4j time limiter, circuit breaker, and conditional retry",
  and the client does apply `@TimeLimiter` — but the spec has no `Time limiter` requirement (the query-client has one).
  Add the missing `Time limiter` requirement (mirroring query-client's), so the spec holds the behavior its Purpose
  claims.
- **`quality/code-quality`:** two requirement bodies use `MUST`/`MUST NOT` where the corpus (320 `SHALL` vs 2 `MUST`)
  uses `SHALL`. Change them to `SHALL`.
- **`quality/dependency-management`:** the OpenSearch coordinates are spelled `org.opensearch.client: spring-data-...`
  (stray space) in several places, inconsistent with the standard `group:artifact` form used elsewhere. Normalize all
  five coordinate mentions in the requirement to `group:artifact`.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the per-change-subagents requirement no longer carries a stale enumeration.
- `showcase/clients/command-client`: adds the missing `Time limiter` requirement.
- `showcase/quality/code-quality`: `MUST` → `SHALL` in two requirement bodies.
- `showcase/quality/dependency-management`: coordinate spelling normalized.

`showcase/deployment/helm-chart` is listed in "What Changes" but is not a modified _capability_: its
`Service deployments` requirement is already correct (it names five services); only its Purpose is stale, and a Purpose
cannot ride a delta, so it is refreshed in the archive commit (task 3.2).

## Impact

- `openspec/specs/showcase/quality/ide-config/spec.md` and `.../infra-image-versions/spec.md` — title-only edits (no
  delta).
- Delta specs in this change for `agent-skills`, `command-client`, `code-quality`, and `dependency-management`.
- `openspec/specs/showcase/deployment/helm-chart/spec.md` — Purpose refreshed in the archive commit (no delta).
- `AGENTS.md` — the spec-header gotcha's "two specs still carry non-path titles" sentence is fixed by this change, so it
  is updated (docs refresh).
- No code, build, or deployment change — spec text only.
