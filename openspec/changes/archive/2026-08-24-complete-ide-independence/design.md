# Design: Optional IDE inspections and additional Checkstyle rules

## Context

See proposal.md — Why. Step 3 retires the last required IDE step (AGENTS.md's IDE-inspections convention) and adds a
small set of build-gated Checkstyle rules that recover the meaningful coverage those inspections used to provide.

## Goals / Non-Goals

**Goals**

- No IDE step is required to verify a change; the build gates are canonical.
- A curated set of low-noise Checkstyle rules replaces the most valuable inspection coverage.
- AGENTS.md and README.md reflect the optional-IDE workflow.

**Non-Goals**

- Full parity with the IntelliJ inspection set — that is explicitly out of scope (diminishing returns, endless
  rule-tuning).
- New analyzers (PMD and the like) — the existing Checkstyle/ErrorProne/SpotBugs stack already covers the valuable
  ground; adding a third analyzer is not warranted now.
- Enabling more ErrorProne checks — ErrorProne's default set is broad; selecting further off-by-default checks risks
  noise and Lombok conflicts. Revisit only if a specific gap matters.

## Decisions

- **The required per-edit verification is the build gates.** `./gradlew spotlessApply` + the touched module's gates
  (compile, checkstyle, errorprone, spotbugs, tests). IDE inspections become explicitly optional ("if the IDE is
  available, you may run them; they are not required"). Rationale: this is the actual end-state of IDE independence —
  a contributor needs only the Gradle build.
- **Add five low-noise Checkstyle rules** to `config/checkstyle/checkstyle.xml`:
  - `EqualsHashCode` — classes defining `equals` must define `hashCode` (and vice versa); real correctness catch.
  - `StringLiteralEquality` — `==` on string literals; real bug catch.
  - `SimplifyBooleanReturn` — `if (x) return true; return false;` → `return x;`; readability.
  - `UnusedLocalVariable` — unused locals; the inspection-equivalent gap.
  - `AvoidStarImport` — no wildcard imports; matches the single-import convention.
  Rationale: each maps to a common IntelliJ inspection, is low-noise on this codebase, and has real value. Rules that
  prove noisy against the existing code are dropped during implementation rather than suppressed — the bar is a clean
  gate with no whack-a-mole.
- **Tidy stale inspection guidance in AGENTS.md.** The `NewClassNamingConvention` ignore note is obsolete (checkstyle
  owns naming now); the `CodeBlock2Expr` note is formatter-adjacent style and moves to optional guidance. The
  assertion-style preference (`assertThat(x).isNotNull()` over `Objects.requireNonNull`) stays as style guidance, not
  an enforced gate.
- **No new dependency or analyzer.** Checkstyle rules only; no build-logic or catalog changes.

## Risks / Trade-offs

- [A rule flags existing code, forcing a fix or suppression] → Mitigation: the rule set is curated to be low-noise;
  any rule that needs more than a handful of targeted fixes is dropped instead. No blanket suppressions.
- [Dropping the inspection step loses some IntelliJ-only warnings] → Mitigation: accepted and explicit — the goal is
  no required IDE step; the meaningful coverage is recovered by the added rules and the existing gates.

## Migration Plan

1. Add the five rules to `config/checkstyle/checkstyle.xml`.
2. Run the checkstyle tasks across all modules; fix genuine violations or drop any noisy rule.
3. Rewrite the AGENTS.md inspections convention as optional and tidy stale guidance.
4. Add the no-IDE statement to README.
5. Run `./gradlew build -x e2eTest` to confirm all gates pass.
6. Rollback: remove the added rules and revert the docs edits.

## Open Questions

None — the rule selection is settled; any rule that proves noisy is dropped during implementation.