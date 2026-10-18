# Proposal

## Why

The architecture audit verifies each finding against the repository and surfaces unrecorded intent, but its method does
not say how to resolve a disagreement between records — an accepted ADR against the implementation, the spec
decomposition, or `AGENTS.md` — so a reader cannot tell a contradiction from a stale supersession or a deliberate
exception. It can also read a dependency edge as a violation without first establishing the boundary rule that forbids
it, repeat one mismatch across both sections, and bound neither its exploration nor its cohesion/coupling advisories.
The deferral check holds a condition against the repository where it names a checkable fact but gives no conclusion when
that verification cannot be completed, so an unverifiable external condition can read as an expiry claim, and the
parked-idea check states no partial-landing case.

## What Changes

- **`showcase/quality/agent-skills`** — the architecture requirement gains an authority model, the "inspect both sides"
  evidence precision, the same-cause dedup rule, the bounded cohesion/coupling advisory, and a staged investigation; the
  intent requirement gains the non-ADR rationale search, the `Revisit when:` condition classification with its
  external-verification caveat, and the partial-landing distinction (two `MODIFIED` blocks, five and two scenarios).
- **`.opencode/agent/architecture-auditor.md`** — the same in the definition's own words.
- **`.opencode/commands/audit-architecture.md`** — step 1 names the new rules.
- **`AGENTS.md`** — the `architecture-auditor` bullet names the routing-relevant rules (the advisory-not-finding case,
  the condition classification) and delegates the rest to its spec pointer.
- **`README.md`** — the architecture prose names the routing-relevant rules.
- **`AGENTS.md`**, **`openspec/config.yaml`**, and **`docs/ideas.md`** — this unit's own capture (run before and after
  the review loop closed) merges clauses into `AGENTS.md`'s existing bullets (a dismissal's reason is a claim to verify;
  a `## Purpose`-refresh determination derived from the Purpose's clause; a review that falsifies a claim's reason
  falsifies its conclusion; a recurring `claim-unreproducible` means the change's own self-records were drafted from
  intent), widens `openspec/config.yaml`'s `rules.design` clause to match, updates two `docs/ideas.md` entries (the
  partly-landed deferral idea and the extended config-rules idea), and parks one new idea (the review-loop session
  trigger).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the architecture requirement gains the authority model, the evidence precision, the
  dedup and staging rules, and the bounded advisory; the intent requirement gains the non-ADR rationale search, the
  condition classification, and the partial-landing distinction.

## Impact

- A future audit resolves a record disagreement by authority (or reports it as advisory), reports a dependency edge only
  against a recorded rule, reports one mismatch once, and states no conclusion it cannot verify; nothing about the
  product changes.
- **Build / tests / services**: no code, build, dependency, or deployment change — one agent definition, its command,
  `AGENTS.md`, `README.md`, `docs/ideas.md`, `openspec/config.yaml`, and the spec delta.
- **Verification**: `openspec validate --all` and `spotlessApply`/`spotlessCheck` for the artifacts; a definition's
  behavior is only exercised by a run, which needs the edited definition picked up (a reload or a new session).
