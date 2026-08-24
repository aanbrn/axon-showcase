## 1. Add the Checkstyle rules

- [x] 1.1 Add `EqualsHashCode`, `StringLiteralEquality`, `SimplifyBooleanReturn`, `UnusedLocalVariable`, and
      `AvoidStarImport` to `config/checkstyle/checkstyle.xml`

## 2. Green the gate

- [x] 2.1 Run the checkstyle tasks across all modules; fix genuine violations, or drop any rule that proves noisy
      rather than suppressing it

## 3. Make IDE inspections optional

- [x] 3.1 Rewrite the AGENTS.md "IDE inspections" convention as optional — required verification is
      `./gradlew spotlessApply` + the module's quality gates — and tidy the stale inspection-specific guidance
      (`NewClassNamingConvention` ignore, `CodeBlock2Expr`)

## 4. Document the no-IDE guarantee

- [x] 4.1 Add an explicit statement to `README.md` that all quality gates run in `./gradlew check` with no IDE
      required, and the IDE is an optional convenience

## 5. Verify the change artifacts

- [x] 5.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors
- [x] 5.2 Run `./gradlew build -x e2eTest` and confirm all gates pass with the added rules