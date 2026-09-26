# Tasks

## 1. Convention

- [x] 1.1 Extend the Lombok convention in `AGENTS.md` to state the local-variable rule: `val` for a non-reassigned local
      whose initializer infers the type, the language's `var` for a reassigned one, and an explicit type where inference
      is impossible (fields, parameters, returns, diamonds, no initializer, `null`/lambda/method-reference/array
      initializers, boxed or widened primitives, a multi-declarator declaration). Verify the wording names each
      exception.

## 2. Sweep

- [x] 2.1 Write an AST codemod (JavaParser, Java 21) that rewrites local-variable declarations to `val`/`var` per the
      rule, adds `import lombok.val;` where a `val` is introduced, removes imports the conversion makes unused, and
      skips the D3 cases; verify it reports the files and declarations it changed and those it skipped.
- [x] 2.2 Run the codemod over the modules' Java sources; read its report and confirm the skipped set matches D3.
- [x] 2.3 Compile every source set — main, test, Gatling, and the component/integration/e2e suites — and fix any
      declaration the codemod mis-typed, re-running the codemod's rule by hand where needed.

## 3. Verification

- [x] 3.1 `./gradlew spotlessApply` then `spotlessCheck` pass; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
      passes.
- [x] 3.2 `openspec validate --changes` passes; a read of `git diff --stat` confirms the sweep touched only Java sources
      plus `AGENTS.md`.
