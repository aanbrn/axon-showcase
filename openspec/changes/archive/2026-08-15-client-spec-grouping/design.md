## Context

The showcase specs are organized under `openspec/specs/showcase/` by architectural layer: `gateway/`, `write-side/`,
`read-side/`, `clients/`, `platform/`, `deployment/`, `quality/`. The `clients/` group holds the two consumer-facing
client library specs. The open question is whether those specs belong in their own group or folded into the side they
serve. Motivation and scope are in proposal.md; this document records the grouping decision and the mitigation for the
contract-drift risk.

## Goals / Non-Goals

**Goals:**

- A spec taxonomy where each group is one coherent kind of artifact, so a reader navigating by artifact type lands
  predictably.
- Explicitly capture the rationale for `clients/` as a separate group so future reorganizations start from the recorded
  decision, not from scratch.
- Reduce the practical risk that a service contract and its client spec drift apart.

**Non-Goals:**

- Reorganizing any other spec group (`gateway/`, `platform/`, etc.).
- Changing requirement behavior in any spec — cross-references are documentation only.
- Standardizing on a single grouping axis across the whole tree; the tree already mixes layer and domain axes and that is
  accepted as-is.

## Decisions

### 1. Keep client specs in a separate `showcase/clients/` group

The two clients are consumer-facing reactive libraries with a behavioral shape that is fundamentally different from the
services they wrap: Resilience4j wiring (retry filters, circuit breaker isolation of business errors, time limiter),
auto-configuration, and error-translation philosophy. They share more of that shape with each other than either shares
with its wrapped service. The taxonomy is otherwise layer-based (`gateway/`, `platform/`, `deployment/`, `quality/`), so
`clients/` is the same kind of category.

- **Alternative considered — fold into sides** (`write-side/command-client`, `read-side/query-client`): wins on contract
  cohesion (client and service speak the same vocabulary: commands, error codes). Rejected because it makes
  `read-side/` a grab-bag of four artifact kinds — a server (query-service), a server (projection-service), an internal
  library (projection-model), and an external consumer library (query-client). It also breaks layer coherence: the
  projection-service differs from query-client more than command-client differs from query-client.

### 2. Mitigate contract drift with cross-references, not directory adjacency

- **Decision**: each client spec gains a short "Contract source" note pointing at the service spec that owns the shared
  vocabulary: `command-client` → `write-side/command-service`, `query-client` → `read-side/query-service`. The notes name
  the shared error codes and message/query types so a change to either side is discoverable from the other.
- **Alternative considered — rely on directory adjacency**: rejected because adjacency does not fix the drift risk; the
  folded layout would not be adopted, so the cross-reference is the active mitigation.

## Risks / Trade-offs

- [Cross-references can still go stale] → Mitigation: keep them short and precise (spec paths plus the named shared
  symbols), so any drift in either the path or the symbol list is immediately visible on a routine read.
- [A reader navigating by data flow (how a command flows through the system) may miss the split] → Mitigation: the
  `gateway/api-gateway` spec already narrates command/query flow end-to-end; no change needed here.

## Migration Plan

No deployment or rollback applies — this is documentation only. The cross-references are added directly to the existing
spec files.

## Open Questions

None.