## Why

`AGENTS.md`'s promotion gate already routes a rule whose rationale is **normative in a spec** to a pointer rather than a
copy (added with the growth discipline). It has no rule for the neighbouring case: the specs describe a rule's subject
**only as an outcome**, with the mechanism that delivers it stated nowhere spec-level. That gap has a live cost — a
measured, load-bearing runtime invariant (the WebFlux blocking-execution routing) lives only in an `AGENTS.md` gotcha,
while the gateway and query-service specs state the `400`/`404` outcomes it delivers without the routing that makes them
resolve, so a future "simplification" could remove it and break a spec'd guarantee with nothing in the corpus to stop
it.

## What Changes

- Extend the `agent-skills` promotion-gate requirement's existing pointer clause to cover the outcome-only case: when
  the specs describe a rule's subject only as an outcome, the mechanism belongs in the capability spec and the bullet
  keeps a pointer.
- Fold the configurer invariant into the requirements it underpins, as a modified clause rather than a new requirement:
  - `specs/showcase/gateway/rest-api/spec.md` — `Command error translation` and `Query error translation`
  - `specs/showcase/read-side/query-service/spec.md` — `Query validation` and `Fetch showcase by ID query`
- Rewrite the `AGENTS.md` configurer gotcha (16 lines) as a rule + one-line why + pointer to those specs.
- Add the outcome-only half to the `AGENTS.md` promotion-gate bullet's pointer sentence.

## Capabilities

### New Capabilities

None — the boundary rule extends an existing requirement, and the invariant folds into existing error-path requirements.

### Modified Capabilities

- `showcase/quality/agent-skills`: the existing pointer clause is extended to the outcome-only case (a rule whose
  subject the specs state only as an outcome).
- `showcase/gateway/rest-api`: `Command error translation` and `Query error translation` each gain the routing
  dependency that makes their `400` validation path resolve (their `404`/`409` mappings do not depend on it).
- `showcase/read-side/query-service`: `Query validation` gains the routing dependency for its `400` path, and
  `Fetch showcase by ID query` gains it for its `404` path.

## Impact

- **Specs**: one `MODIFIED` delta on `agent-skills`; two on `gateway/rest-api`; two on `read-side/query-service`.
- **Docs**: `AGENTS.md` — the promotion-gate bullet's pointer clause gains the outcome-only half, and the 16-line
  configurer gotcha becomes a short pointer, so the file's net change is a decrease (the rule stays; the rationale moves
  to the specs that own it).
- **Build / tests / deployment**: none — no code, workflow, configuration, or pinned-version change. The invariant is
  documented, not altered; the configurers stay exactly as they are.
