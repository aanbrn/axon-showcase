# ADR-0010: Enforce the module dependency graph in the build

Date: 2026-09-16

Status: Accepted

## Context

The architecture has a _decide_ layer (`docs/adr/`), a _describe_ layer (the capability specs under `openspec/specs/`),
and a _review_ layer (the `architecture-auditor` subagent), but nothing **enforced** the structure: a `build.gradle.kts`
could add a dependency the project's shape does not sanction, and only a later audit would notice. That gap has already
cost something — `showcase-query-api` → `showcase-command-api` was flagged as an unsanctioned direction and the
correction deferred, because narrowing it then broke `:showcase-query-client:compileJava` (that consumer received a
transitive `hibernate-validator` through it) — with no artifact recording whether the direction is allowed. That
deferral has since been resolved (`narrow-query-api-dependency`).

The gap was first framed as ArchUnit fitness functions. Working the mechanism showed that is the wrong tool: "no service
depends on another service" is a property of the Gradle graph, and a service's test classpath contains no other
service's classes, so such a rule would have nothing to inspect — making it inspectable means a central module depending
on the very edges under test. The compiler already enforces direction once dependencies are declared, so the residual
risk is a forbidden line in a build script, and that is where the check belongs.

## Decision

The build verifies the module dependency graph and fails `check` when a module takes a forbidden edge, naming both ends.
The forbidden edges are:

- a module depending on a **service application** (`showcase-command-service`, `showcase-query-service`,
  `showcase-projection-service`, `showcase-api-gateway`) — services talk via a `-client`, Kafka, or HTTP;
- an **`-extension`** module depending on anything but `platform` and the `showcase-test` dependency — they are leaves;
- a **contract module** (`showcase-command-api`, `showcase-query-api`, `showcase-projection-model`,
  `showcase-query-proto`) depending on a `-client` or a service application.

The verification walks the declaration configurations of every module's production source sets (`main` and
`testFixtures`), excluding the `platform` BOM by its declared target and dropping any edge whose source and target are
the same module when it decides violations. That last comparison is how the `java-test-fixtures` plugin's reference to
its own project — Gradle resolves `project()` to the current project — avoids reading as a structural edge. An
allow-list was considered and rejected: it enumerates every legitimate edge and would fail on the tree the day it
landed, where naming the forbidden edges is stable and derivable from the documents. Contract-to-contract edges are
outside the forbidden classes. `showcase-query-api` → `showcase-command-api` was one, sanctioned on the transitive
`hibernate-validator` its consumers needed (recorded in `AGENTS.md`); it has since been **narrowed** to
`showcase-query-api` → `showcase-identifier-extension`, the `KSUID` edge its own source actually uses, with
`showcase-query-client` declaring the `hibernate-validator` it imports (`narrow-query-api-dependency`).

## Consequences

- Structural drift fails where it is introduced, naming the offending edge, rather than surfacing at the next audit.
- The rules assert properties the graph already has, so they constrain rather than add capability: a legitimate new edge
  lands without touching them, while a forbidden one requires a deliberate decision and an ADR amendment.
- Contract-to-contract edges are unconstrained, so a dependency between the `-api` modules would pass unnoticed; a
  narrower rule has this ADR's stated shape to grow from. The `query-api` narrowing has since landed
  (`narrow-query-api-dependency`) without changing this rule.
- The `architecture-auditor`'s boundary checks for these three classes are now redundant: the repo already requires an
  auditor not to re-check a property an existing gate enforces, so no auditor change is owed.
