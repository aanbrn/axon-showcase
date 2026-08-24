# Proposal: Complete IDE independence — optional IDE inspections and more build-gated checks

## Why

This is step 3 of the roadmap toward a fully IDE-independent development environment: **Checkstyle → Spotless →
complete IDE-independence**. Formatting, naming, imports, line length, correctness, security, and coverage are all
build-gated already. The last *required* IDE step is AGENTS.md's "IDE inspections" convention — running IntelliJ
inspections through the Steroid MCP after every edit. This change makes that step optional and adds a small
set of low-noise build-gated Checkstyle rules that close the meaningful gaps those inspections used to cover, so the
build gates become the canonical — and fully IDE-independent — verification of a change.

## What Changes

- AGENTS.md: the "IDE inspections" convention becomes optional (a convenience when the IDE is available); the required
  per-edit verification is `./gradlew spotlessApply` + the module's quality gates. Stale inspection-specific guidance
  (`NewClassNamingConvention` ignore, `CodeBlock2Expr`) is tidied accordingly.
- Checkstyle: add low-noise rules that replace the most valuable inspection coverage: `EqualsHashCode`,
  `StringLiteralEquality`, `SimplifyBooleanReturn`, `UnusedLocalVariable`, `AvoidStarImport`. Any that prove noisy
  against the codebase are dropped rather than suppressed.
- README: an explicit statement that all quality gates run in `./gradlew check` with no IDE required; the IDE is an
  optional convenience for interactive editing and debugging.
- Spec: `showcase/quality/code-quality` gains a requirement that the standard check verifies all quality gates without
  an IDE.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/code-quality` — ADDED requirement: the standard check verifies every quality gate with no IDE.

## Impact

- **Code**: `config/checkstyle/checkstyle.xml` (added rules).
- **Docs**: `AGENTS.md` (workflow + tidying), `README.md` (no-IDE statement).
- **Build**: `check` now also runs the added Checkstyle rules; no new dependencies.
- **Tests**: no test changes; existing suites must pass with the added rules.