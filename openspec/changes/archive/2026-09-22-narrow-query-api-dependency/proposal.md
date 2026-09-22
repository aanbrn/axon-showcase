## Why

`showcase-query-api` declares `api(project(":showcase-command-api"))` while its main source needs only
`showcase.identifier.KSUID`, so every query-api consumer inherits the entire write-side API — `axon.modelling`,
`hibernate.validator`, `jackson2.databind` — none of which it asked for. The first `/audit-architecture` run flagged the
edge, ADR-0010 deferred the correction (the consumer `showcase-query-client` was leaning on the transitive
`hibernate-validator`), and both retrospectives named the narrowing as their top parked product candidate. A build
experiment (swap the dependency, compile every module) confirms only one consumer actually breaks, on one import.

## What Changes

- `showcase-query-api/build.gradle.kts`: replace `api(project(":showcase-command-api"))` with
  `api(project(":showcase-identifier-extension"))` — the module whose `KSUID` its main source uses.
- `showcase-query-client/build.gradle.kts`: declare the `hibernate-validator` it imports directly, instead of inheriting
  it through the re-export it no longer receives.
- `docs/adr/0010-enforce-module-dependency-graph.md`: record that the deferred narrowing has landed.
- `AGENTS.md`: the `project(...)` gotcha's worked example is this edge; update it to its resolved form while keeping the
  rule.
- `docs/ideas.md`: remove the implemented idea, and sweep the "product phase" entry, which names this narrowing as a
  still-parked candidate.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `code-quality`'s graph requirement enumerates the _forbidden_ edges, and a contract-to-contract edge is not one
of them, so narrowing it changes no specified behaviour. (`skip_specs: true`.)

## Impact

- **Build**: two `build.gradle.kts` files; `verifyModuleDependencies` must still pass (contract → `-extension` is not a
  forbidden edge; `showcase-command-api` already declares the same edge).
- **Docs**: ADR-0010, the `AGENTS.md` gotcha's example, and the parked idea.
- **Runtime**: none — a compile-time graph change; the experiment showed only `showcase-query-client:compileJava`
  failing, and it is fixed by declaring what it uses.
