# Proposal

## Why

The 2026-10-24 spec-corpus audit found two `## Purpose` paragraphs that no longer describe what their capability's
requirements cover. The `clients/query-client` Purpose attributes `INVALID_QUERY` to the `read-side/query-service` spec,
but that spec produces `INVALID_ARGUMENT`; `INVALID_QUERY` is the client's own derived code, produced from an
`INVALID_ARGUMENT` status by this spec's "Query error translation" requirement. The `quality/infra-image-versions`
Purpose still scopes the capability to the PostgreSQL, Kafka, and OpenSearch components and the Bitnami charts, while
its "Infrastructure image references are single-sourced" requirement also governs the observability charts, the
application chart's `common` subchart, and the Paketo builder/buildpack pins. Both are drift `openspec validate` cannot
see: a `## Purpose` is not a requirement, so no delta carries it.

## What Changes

- `openspec/specs/showcase/clients/query-client/spec.md` — the Purpose's "Contract source" sentence names
  `INVALID_ARGUMENT` as the query service's code and `INVALID_QUERY` as the code this client derives from it, mirroring
  the `command-client` Purpose's form.
- `openspec/specs/showcase/quality/infra-image-versions/spec.md` — the Purpose names the scope the requirement already
  holds: the infrastructure components' images, every pinned Helm chart (the Bitnami charts, the observability charts,
  and the application chart's `common` subchart), and the Paketo builder/buildpack pins documented in the
  `deployment-operations` pack, with the build gate that verifies them.
- No requirement, scenario, or behavior text changes — a Purpose-only refresh, declared via `skip_specs: true` (a
  `## Purpose` cannot ride a delta).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — this corrects two capabilities' `## Purpose` prose, which is not a requirement and so cannot ride a delta; the
edits are made in the archive commit as a `skip_specs` change, as `refresh-agent-skills-purpose` did.

## Impact

- `openspec/specs/showcase/clients/query-client/spec.md` — the Purpose paragraph's contract-source sentence only.
- `openspec/specs/showcase/quality/infra-image-versions/spec.md` — the Purpose paragraph only.
- No requirement, code, build, dependency, test, deployment, `AGENTS.md`, or `README.md` change; `docs/ideas.md` carries
  no idea for either edit.
