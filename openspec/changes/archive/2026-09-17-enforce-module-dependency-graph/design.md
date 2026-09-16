## Context

`#262` was drafted as ArchUnit fitness functions. Working the mechanism showed that is the wrong tool for almost all of
it, and the issue was re-scoped before this change was written:

- **"No service depends on another service" is a Gradle-graph property** — a service's test classpath contains no other
  service's classes, so an ArchUnit rule has nothing to inspect, and making it inspectable would mean a central module
  depending on the very edges under test.
- **The compiler already enforces direction** once dependencies are declared: the residual risk is a forbidden line
  added to a `build.gradle.kts`, which is a build-script property, best checked where it is introduced.
- **There are no `internal`/`impl` packages** under any `src/main/java`, so the "internals rather than `-api`" rule has
  no convention to bind to.

## Goals / Non-Goals

- **Goal:** a forbidden dependency edge fails `check`, naming the edge, so drift is caught where it is introduced.
- **Goal:** the sanctioned shape is recorded as a decision (ADR), not inferred from the build.
- **Non-Goal:** reproducing what Gradle already enforces — undeclared module access is a compile error, and this adds
  nothing to it.
- **Non-Goal:** an ArchUnit suite, or a central module that depends on every module.

## Decisions

**Forbidden edges, not an allow-list.** An allow-list must enumerate every legitimate edge and would fail on the tree
the day it lands (the gateway depends on two `-client` modules; the query side depends on `-proto`; `load-tests` depends
on a contract API). The rules instead name what must never appear, which is derivable from the documents and stable:

- a module depends on a **service application** (`showcase-command-service`, `-query-service`, `-projection-service`,
  `showcase-api-gateway`);
- an **`-extension`** module depends on anything but `platform` and the `showcase-test` dependency;
- a **contract module** (`showcase-command-api`, `showcase-query-api`, `showcase-projection-model`,
  `showcase-query-proto`) depends on a `-client` or a service application.

**What the verification walks.** Every module's production source sets, across every module: `main`, which is what
ships, and `testFixtures`, which other modules consume as an artifact. For each, its declaration configurations — `api`,
`implementation`, `compileOnly`, `compileOnlyApi`, `runtimeOnly` and `annotationProcessor` by the `SourceSet` accessors,
so a dependency cannot escape by the configuration it is declared in. Two exclusions, both from the declaration rather
than a hand-maintained list:

- the `platform` BOM, by its declared target — it is injected into every java module by `java-conventions`, and while
  the rules would permit it as an unclassified target anyway, excluding it keeps the BOM from reading as a structural
  edge at all.
- a self-edge (source equals target), which is how the `java-test-fixtures` plugin's reference to its own project —
  Gradle resolves `project()` to the current project — avoids reading as a structural edge.

**Modules are classified explicitly.** Sources are the java modules; targets classify as service application,
`-extension`, contract, or _unclassified_. An unclassified target is permitted, because the rules name what must never
appear rather than what may: `load-tests` → `showcase-command-api` passes without being enumerated, and a new module
stays permitted until the shape is decided for it. The classifier is a plain map in the pure rule object, so an omission
is visible in one place.

**The rule logic is pure.** Classification and validation live in a plain object taking edges as data — so a unit test
covers the rules without a Gradle runtime, which is also `build-logic`'s first test. The task is a thin adapter over it,
mirroring `VerifyInfraImageVersionsTask`'s shape (a `@CacheableTask` with `@Input` data and a report file).

**The root gathers the edges.** Each subproject's declared `ProjectDependency`s are collected into a serializable edge
list — the same pattern the infra-image check uses for its `checks` input — and set on the task, which is wired into
`check` beside `verifyInfraImageVersions` and `workflowLint`.

**The tests must actually run.** `build-logic` is an **included build** (`includeBuild`), so its `test` is not part of
the root's `check`, which adds only `verifyInfraImageVersions` and `workflowLint`, and CI runs `./gradlew check`. The
root `check` therefore gains a dependency on the included build's test task, and `build-logic/build.gradle.kts` declares
the test dependency — without both, the new tests would be dead code and the "unit-tested rules" claim would be hollow.

**The auditor's boundary checks become redundant, deliberately.** The `agent-skills` spec requires an auditor to not
re-check a property an existing gate already enforces, so the three newly gated classes need no auditor edit — the
decision is recorded rather than left implicit.

**`query-api` → `command-api` is sanctioned for now, and narrowed by its own idea.** It is a contract-to-contract edge,
outside the three forbidden classes, so the rules neither permit nor forbid it explicitly. The ADR records that it
stands on the rationale in `AGENTS.md` (the consumers' transitive `hibernate-validator`), and narrowing it belongs to
the parked idea that would declare those consumers' real needs.

**Configuration cache stays off.** The task reads declared dependencies only, but the repo already forbids enabling the
configuration cache (the Helm plugin is incompatible), so no cache-safety refactor is owed here.

## Risks / Trade-offs

- **Risk:** the classes are coarse and could miss a narrower drift (a contract module depending on another contract
  module). Mitigation: the ADR states the sanctioned shape, so a narrower rule has recorded intent to grow from.
- **Risk:** a broken walk reads as a green gate — the first implementation gathered **one** edge instead of eighteen and
  passed. Mitigation: the task reports how many edges it inspected, the positive control (task 4.1) adds a real
  forbidden edge and must fail, and the unit tests cover the rules the walk feeds; a passing check on the untouched tree
  is not evidence by itself.
- **Trade-off:** another root task in `check`. Accepted: it resolves no configurations, and it is cacheable on its
  inputs.
- **Risk:** a new module kind needs a classification decision. Mitigation: the classifier is explicit and unit-tested,
  and unclassified targets are permitted by design rather than silently mis-typed.

## Migration Plan

Implementation: the task, rules, tests, the root wiring (including the included build's test), and the ADR commit
together; the spec syncs at archive, with the capability's `## Purpose` refreshed in that commit since the requirement
widens the capability's scope. The implemented idea leaves `docs/ideas.md` in the same PR, along with the enumeration
that still calls it a candidate.

## Test suites are deliberately out of scope

The verification reads each module's production source sets only. A test suite's configurations are populated when that
suite's test tasks are realized — which happens under `check`, because `java-conventions` wires the suites into it, but
not when `verifyModuleDependencies` runs alone. Measured: a walk over every source set inspected 36 edges standalone and
47 under `check`, the 11-edge difference being exactly the suite-declared dependencies.

Covering them was tried and rejected as disproportionate: realizing the suites eagerly from the convention plugin,
forcing every module's evaluation with `evaluationDependsOn`, collecting the suite edges where the `testing` accessor is
in scope (the plumbing reached the root intact, but the configurations held one edge of six at that point), and reading
every configuration instead of each source set's (36 → 39, the remaining factor not isolated).

Excluding them is the correct scope rather than a coverage loss: the graph this enforces is the one that ships, a
suite's dependencies reach no artifact, and a suite depending on a service application is a legitimate way to exercise
that service. With the suites out, the walk's input is a function of the repository — both invocations inspect the same
34 edges, and the report lists every one of them.

## Open Questions

None. The one decision the audit left open — the read side's dependency on the write side's contract — is resolved above
(sanctioned, with narrowing parked).
