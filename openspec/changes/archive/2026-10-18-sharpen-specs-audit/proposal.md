# Proposal

## Why

The spec-corpus audit reports structural and consistency problems across `openspec/specs/`, but its method
under-specifies the two judgments it makes most: whether a reference is really dead, and whether two similar
requirements are really duplicates. A false-positive case such as a generated class, an external tool's command, or a
conceptual reference reads as a dead reference; a shared convention repeated so a capability stays self-contained reads
as a duplicate — both cost the owner a triage. Its boundaries are also implicit: it never states that the corpus's
capability decomposition belongs to the architecture audit, that an issue an existing gate already enforces is not its
to re-report, or that a proposed change's internal quality is the review loop's, and its correction guidance does not
require a merge to say why two requirements are equivalent.

## What Changes

- **`showcase/quality/agent-skills`** — the spec-corpus requirement gains the dead-reference verification (the
  verified-dead vs unresolved distinction and the false-positive classes), the four-way overlap taxonomy with its
  verify-before-consolidating steps, three boundary exclusions (the capability decomposition; an issue an existing gate
  enforces; the internal quality of a proposed change or change delta), the requirement-structure nuances (heading,
  SHALL, scenario), the confidence-as-a-filter rule, the one-root-cause-one-finding dedup, the behavior-preserving
  correction guidance, and the staged method (one `MODIFIED` block).
- **`.opencode/agent/specs-auditor.md`** — the same in the definition's own words.
- **`.opencode/commands/audit-specs.md`** — step 1 names the new rules.
- **`AGENTS.md`** — the `specs-auditor` bullet names the routing-relevant rules and delegates the rest to its spec
  pointer.
- **`README.md`** — the audit prose names the routing-relevant rules.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the spec-corpus audit requirement (one `MODIFIED` block).

## Impact

- A future spec-corpus audit verifies a dead reference before reporting it, classifies a duplication before reporting
  it, and leaves the capability decomposition, any already-enforced issue, and a proposed change's internal quality to
  their owners; nothing about the product changes.
- **Build / tests / services**: no code, build, dependency, or deployment change — one agent definition, its command,
  `AGENTS.md`, `README.md`, and the spec delta.
- **Verification**: `openspec validate --all` and `spotlessApply`/`spotlessCheck` for the artifacts; a definition's
  behavior is only exercised by a run, which needs the edited definition picked up (a reload or a new session).
