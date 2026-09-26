# Design

## Context

See `proposal.md` — Why. Current state: 168 Java files, 80 import `lombok.val` (777 declarations), none import
`lombok.var`; the rest mix explicit local types. The build compiles Java 21; Spotless (palantir) formats it and does not
manage imports, and no gate enforces local-variable style.

## Goals / Non-Goals

**Goals:**

- State the rule once, and make the tree conform.
- Keep the change behavior-preserving and verified by compilation.

**Non-Goals:**

- Not a gate: nothing enforces `val`/`var` mechanically (a formatter cannot type-check), so this stays an `AGENTS.md`
  convention rather than a spec requirement or a lint rule.
- Not touching fields, parameters, return types, or the lombok annotations already in the convention.

## Decisions

- **D1 — `val` for a non-reassigned local, the language's `var` for a reassigned one.** `val` is `final`, so a
  reassigned local cannot use it; Lombok's `var` is not an option on Java 10+ (the compiler rejects `import lombok.var;`
  as a restricted type), so the reassignable counterpart is the language's `var`. An explicit type stays where inference
  is impossible.
- **D2 — Convert with an AST codemod, not text substitution.** Types appear in many contexts; a regex would corrupt
  them. The codemod parses each file (JavaParser at the Java 21 language level), rewrites only local-variable
  declarations, and adds `import lombok.val;` where the conversion introduces a `val` (a `var` needs no import).
- **D3 — Skip a declaration when inference would change the type or is impossible.** A declaration is left explicit
  when: it has no initializer; the initializer is `null`, a lambda/method reference, or an array initializer; the type
  is a diamond (`new TreeMap<>()`, whose type arguments would have to be re-spelled); the declared type is a boxed
  primitive (`Integer`, `Long`, …); the declared primitive is wider than the initializer (`double d = 1`); or the
  declaration has more than one declarator. `val`/`var` infer the _initializer's_ type, so each of these would silently
  narrow or widen the static type.
- **D4 — Verify by compiling every source set.**
  `./gradlew compileJava compileTestFixturesJava compileTestJava compileGatlingJava` (and the e2e/component/integration
  source sets) must pass; a skipped-case mistake that makes a `val` reassigned is a compile error, so the compiler is
  the safety net. `spotlessApply` runs after.
- **D5 — `var` for reassigned locals is the language's, not Lombok's.** The repo's existing `val` is `lombok.val`;
  Lombok's `var` cannot be imported on Java 10+ (`import lombok.var;` is an illegal reference to a restricted type), so
  a reassigned local uses the language's `var`. Both are supported for local declarations, for-each loops, classic `for`
  loops, and try-with-resources; `val` stays for the non-reassigned case because it keeps the local `final`.

## Risks / Trade-offs

- **Static type narrowing.** `List<String> x = new ArrayList<>()` would infer `ArrayList<String>`. → Skipped (diamond);
  where the initializer's type is a concrete subtype, D3 leaves the declaration explicit unless the codemod can see the
  types match. Residual risk is caught by compilation, not by review of a large diff.
- **A large mechanical diff?** Only the files that still declare explicit local types change. → The change is
  behavior-free and compiler-verified; the review reads the convention and spot-checks the codemod's output rather than
  every line.
- **Lombok `var` availability in every source set.** Lombok is on every module's annotation-processor path already (val
  is used); if a source set lacks it, its files keep explicit types rather than failing.

## Migration Plan

- Add the convention, run the codemod over the modules, compile every source set and fix anything it surfaces, format,
  then review. Rollback is reverting the branch; no deployed artifact or behavior changes.
