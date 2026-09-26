# Proposal: Make Lombok's `val`/`var` the explicit local-variable convention

## Why

`val` is the repo's de-facto local-variable style — 80 of 168 Java files already import `lombok.val` (777 declarations)
— but it is nowhere stated, so a new file (like the freshly merged `KneeFinder`) can ship explicit local types and no
reviewer can point at a rule. The Lombok convention names `@RequiredArgsConstructor`/`@Data`/`@Builder`/`@Value` but
says nothing about inferred locals. Make the rule explicit and bring the tree into line with it.

## What Changes

- `AGENTS.md`: extend the Lombok convention to state the local-variable rule — declare a local with Lombok's `val` when
  its initializer infers the type and the local is not reassigned, and with the language's `var` when it is reassigned
  (Lombok's `var` is illegal on Java 10+); keep an explicit type where inference is impossible (fields, parameters,
  returns, diamonds without a target type, no initializer, `null`/lambda/array initializers, and a declared type wider
  than the initializer's).
- Every Java source under the repo's modules: convert local-variable declarations to `val`/`var` per that rule (a
  codemod with the exceptions above), so the convention holds across the tree rather than only in new code.
- `openspec/config.yaml`: no fact moves, so no change.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none) — a local-variable style convention is not a capability behavior, and nothing the build enforces changes; this
change sets `skip_specs: true`.

## Impact

- **Build**: the Java sources that still declare explicit local types change; no build file, dependency, or gate
  changes. `spotlessApply` runs after the sweep (palantir reformats `val`/`var` declarations).
- **Tests**: no behavior change — `val`/`var` are local type inference, and the full compile (`compileJava`,
  `compileTestJava`, `compileTestFixturesJava`, `compileGatlingJava`) plus the quality gates verify the sweep.
- **Deployment**: none.
