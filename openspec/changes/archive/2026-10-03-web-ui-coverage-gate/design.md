# Design

## Context

See `proposal.md` — Why. Verified against the repository:

- **The frontend has no coverage measurement today**: `showcase-web-ui/package.json` has no `coverage` script and no
  `@vitest/coverage-v8` dependency; `vite.config.ts`'s `test` block sets only `environment`, `globals`, `setupFiles`,
  and `include`.
- **`npmTest` is wired into the frontend `check`**: `build-logic/src/main/kotlin/frontend-conventions.gradle.kts`'s
  `tasks.named("check") { dependsOn(npmLint, npmFormatCheck, npmTypeCheck, npmTest) }`; each check is a `NpmTask`
  wrapping a `package.json` script (`args "run", "test"`). `npmTestCoverage` is a strict superset of `npmTest` (the same
  `vitest run`, plus coverage), so it **replaces** `npmTest` in the `check` set — running both would run the suite
  twice.
- **The JVM baseline shape**: `config/jacoco/coverage-baseline.properties` holds `coverage.instruction.minimum = 0.80`,
  read by `code-coverage-conventions.gradle.kts` and enforced by `jacocoTestCoverageVerification` — the model a frontend
  baseline mirrors.
- **Measured coverage** (resolved Vitest `5.0.3` — declaring `@vitest/coverage-v8@^5.0.1` fixed the runner at its exact
  peer — with the V8 provider; 22 files / 103 tests): statements **91.6%**, branches **80.71%**, functions **84%**,
  lines **93.83%**. So the tree already sits well above the JVM's 80% floor.

The frontend `npmTest` output dir is `build/reports`; the coverage report is directed under the same module build dir
(`coverage.reportsDirectory: 'build/coverage'`), **not** Vitest's default `<module>/coverage`, which is untracked and
not gitignored and would otherwise leave a stray directory a directory-wide `git add` could sweep in.

## Goals / Non-Goals

**Goals:**

- Fail `check` when the frontend's measured coverage drops below a committed threshold, with the coverage report written
  to a build directory.
- Keep the threshold a single committed value, mirroring the JVM baseline.

**Non-Goals:**

- Raising the frontend's coverage to a specific target — the gate holds the current level, not a new one.
- A merged JVM+frontend coverage figure — the two are separate toolchains (JaCoCo vs V8) and stay separate, like the JVM
  modules' per-module gates.
- Coverage for the Playwright e2e suite (a separate opt-in task that boots the stack) — coverage is measured on the
  Vitest unit suite, where a threshold is meaningful and CI runs it.

## Decisions

### D1: Vitest's built-in `--coverage` with the V8 provider, not a separate tool

`@vitest/coverage-v8` is Vitest's first-party provider, so `vitest run --coverage` measures in the same run the tests
already do. It is declared with the same caret range as `vitest` (`^5.0.1`), but its peer is **exact**
(`vitest: "5.0.3"`), so resolving it fixed the runner at 5.0.3 — the provider and the runner move together, and a future
bump touches both.

- **Alternative — `@vitest/coverage-istanbul`:** heavier and slower; V8 is Vitest's default and needs no extra config.
- **Alternative — a separate coverage tool (c8/nyc):** duplicates what Vitest's provider already integrates and can
  disagree with the test run's instrumentation.
- **Alternative — no threshold, report only:** the idea's open question, but a report nothing reads does not close the
  gap the way the JVM gate does; the threshold is the point.

### D2: The threshold lives in `config/web-ui-coverage/coverage-baseline.properties`, read by the Gradle task

Mirroring `config/jacoco/coverage-baseline.properties`, a committed properties file holds the minimum (e.g.
`coverage.statements.minimum = 0.80`). The Gradle `npmTestCoverage` task reads it and injects the value into the npm
script via an environment variable (`VITEST_COVERAGE_MIN`), which the script forwards to Vitest's `thresholds`, so the
value is tuned in one committed place, read by one reader (Gradle, as it already reads the JVM baseline), and injected
rather than duplicated in `vite.config.ts`. The measured statement coverage (91.6%) leaves headroom above an 80% floor.
The baseline keeps the JVM's **fraction** form (`0.80`), while Vitest's `thresholds` are **percentages** (0–100) — the
config scales the fraction by 100, since a fraction compared against a percentage never trips the gate.

- **Alternative — `vite.config.ts` reads the `.properties` file directly:** a Vite config parsing a Java-style
  properties file is awkward and puts the read beside build config rather than in the Gradle task that already reads the
  JVM baseline; injecting via an env var keeps one committed source and one reader.
- **Alternative — the threshold inline in `vite.config.ts`:** buries a policy value in build config and diverges from
  the JVM baseline's committed-file shape.
- **Alternative — a Gradle property (`-Pcoverage.minimum=`):** per-invocation, not a committed policy.

### D3: Gate on statements (one metric), matching the JVM gate's single instruction metric

The JVM gate enforces one figure (instruction coverage). The frontend gate enforces **statement** coverage, the closest
V8 equivalent, so the two gates read alike and the threshold is one number to maintain.

- **Alternative — a four-metric `thresholds` object (statements/branches/functions/lines):** more precise but four
  values to tune and four ways to fail; start with statements and widen only if a real gap appears.
- **Alternative — branches:** lower today (80.71%) and noisier; statements is the closer analogue to the JVM metric.

### D4: A new `code-quality` requirement, matching the web-module check requirements

`code-quality` owns each web-module build check as its own requirement ("The web module's formatting is applied by the
build", "The web UI is type-checked by the build", and so on). The coverage gate is an `ADDED` requirement with a
standard-check scenario, a regression scenario, and a passes scenario — the sibling shape.

## Risks / Trade-offs

- [The threshold is set too close to current coverage, so ordinary refactors trip it] → the floor is 80% against a
  measured 91.6%, leaving ~11 points of headroom; it holds the level, not the exact figure.
- [Coverage varies run to run] → V8 coverage is deterministic for a fixed test set; the suite is deterministic (no
  timing-dependent assertions gated on coverage).
- [A new frontend dependency churns the lockfile] → one dev dependency pinned to the runner's version, reviewed in the
  diff, reported by the existing npm-monitoring as any dependency is.
- [The gate slows `check`] → coverage adds instrumentation to a ~3.5s suite; the cost is seconds and local.

## Migration Plan

The dependency and script land with the change; the first run measures and, if the threshold is met, the gate is green
from the start (no grandfathering needed, since the tree is already above the floor). Rollback is reverting the task
wiring.

## Open Questions

None.
