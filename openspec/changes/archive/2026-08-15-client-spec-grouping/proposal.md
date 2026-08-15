## Why

The spec taxonomy groups capabilities by architectural layer, with `showcase/clients/` holding the two consumer-facing
client library specs (`command-client`, `query-client`). This placement was chosen during the "grouped showcase specs by
architectural role" reorganization but was never explicitly reasoned about. The alternative — folding each client into its
service's group (`write-side/command-client`, `read-side/query-client`) — is defensible and worth a recorded decision, so
the structure is intentional rather than accidental.

## What Changes

- Record the design decision to keep the client specs in a separate `showcase/clients/` group, with the rationale and the
  rejected alternative documented in `design.md`.
- Add cross-references from each client spec to the service spec that defines the shared contract vocabulary (error codes,
  command/query types), so contract drift between a service and its client is easier to spot while browsing.
- No system behavior changes: this change touches only OpenSpec artifacts (`openspec/`), not application code.

## Capabilities

### New Capabilities

None. No new capability is introduced.

### Modified Capabilities

None. No requirement behavior changes; only spec-internal cross-references are added.

This change sets `skip_specs: true` in `.openspec.yaml` because it is a documentation/tooling change with no spec-level
behavior delta.

## Impact

- **Code**: none — no application modules change.
- **Specs**: `openspec/specs/showcase/clients/command-client/spec.md` and
  `openspec/specs/showcase/clients/query-client/spec.md` gain a cross-reference to their service-side counterparts.
- **Build/tests**: unaffected.