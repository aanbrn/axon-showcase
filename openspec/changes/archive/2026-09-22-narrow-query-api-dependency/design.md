## Context

See `proposal.md` - Why. Verified against the tree before designing:

- `showcase-query-api/build.gradle.kts:12` declares `api(project(":showcase-command-api"))`; its main source uses no
  `showcase.command.*` type — only `showcase.identifier.KSUID` (`FetchShowcaseListQuery`, `FetchShowcaseByIdQuery`).
- `showcase-command-api` re-exports `showcase-identifier-extension`, `axon.modelling`, `hibernate.validator`, and
  `jackson2.databind` via `api(...)`, so every query-api consumer inherits all four.
- ADR-0010 records the edge as sanctioned _because_ of that transitive `hibernate-validator`, and names the narrowing as
  a parked idea.
- A build experiment (swap the dependency, then `compileJava compileTestFixturesJava compileTestJava --continue` across
  every module) fails **only** `showcase-query-client:compileJava`, on one import
  (`ShowcaseQueryClientProperties.java:6`, `org.hibernate.validator.constraints.URL`).

## Goals / Non-Goals

**Goals:**

- Stop `showcase-query-api` re-exporting the write-side API to its consumers, keeping only what its own source uses.
- Have each consumer declare what it actually uses, so the graph is honest and the build proves it.

**Non-Goals:**

- Any runtime behaviour change — this is a compile-time graph change; the experiment shows every module but one still
  compiles, and the one is fixed by declaring the dependency it imports.
- Constraining contract-to-contract edges in `verifyModuleDependencies` — ADR-0010 deliberately leaves them
  unconstrained, and the narrowing removes the only one that mattered.

## Decisions

### Narrow the edge rather than leave it sanctioned

ADR-0010 chose to sanction the edge in place. That was the right call while the consumer depended on the transitive
`hibernate-validator`, but it is not free: `axon.modelling`, `hibernate.validator`, and `jackson2.databind` are compiled
against and resolved by _every_ query-api consumer for no consumer's stated need. The narrowing is cheap — the
experiment bounds the breakage to one import in one module — so the cost of leaving the edge now exceeds the cost of
narrowing it.

**Alternatives considered.** (a) _Leave it and record the sanction_ — the status quo; rejected because the leak is real
and the fix is bounded. (b) _Narrow and add the transitive to every consumer_ — rejected: only one consumer uses it, and
"declare what you use" means the one that does, does.

### `showcase-query-client` declares `implementation(libs.hibernate.validator)`

`ShowcaseQueryClientProperties` imports `org.hibernate.validator.constraints.URL` in its own `main`, so the module needs
it to compile. `implementation`, not `api`: consumers do not reference the annotation through query-client's API — the
runtime validator is the consumer's own Spring context concern — and the sibling module that does the same
(`showcase-api-gateway`) declares it `implementation` too. This matches the repo's `avoid redundancy` convention rather
than re-exporting a dependency no consumer needs transitively.

### Amend ADR-0010 in place; no capability spec changes

ADR-0010 states the deferral in three places — its Context (the edge was flagged and the correction deferred), its
Decision (the edge is sanctioned, narrowing remains parked), and its Consequences (the parked narrowing) — so the change
updates all three to the resolved state (the edge is narrowed; contract-to-contract edges stay unconstrained).
`code-quality`'s graph requirement enumerates the _forbidden_ edges and a contract-to-contract edge is not among them,
so no requirement's behaviour changes — `skip_specs`.

## Risks / Trade-offs

- **A consumer might rely on another piece of the re-export that the experiment missed.** Mitigation: the experiment ran
  `compileJava`/`compileTestFixturesJava`/`compileTestJava` for every module, not just the suspected one; `check` also
  runs `verifyModuleDependencies`, and the integration suites exercise the runtime graph.
- **The ADR is the record of a deferred decision, so amending it rewrites history.** Mitigation: keep the original
  Context (the deferral happened and why) and add the resolution, rather than deleting the deferral's account.

## Migration Plan

None — no data, API, or deployment change.

## Open Questions

None.
