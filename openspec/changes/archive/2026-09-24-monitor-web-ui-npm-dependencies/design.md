# Design

## Context

See `proposal.md` — Why. Current state that shapes the approach:

- `dependencyUpdates` (`dependency-versions-conventions.gradle.kts`) reports catalog-owned Gradle coordinates only.
- The `dependencySecurityCheck` task (`dependency-security-conventions.gradle.kts`) is an `Exec` running the Snyk CLI
  (`snyk test --all-sub-projects --policy-path=.snyk`) from the project root; `.snyk` carries only `SNYK-JAVA-*`
  ignores. `snyk.yml` runs it, authenticated with `SNYK_TOKEN`, and fails on findings (no issue tracker).
- The web UI is a node module built through `com.github.node-gradle.node` (`frontend-conventions.gradle.kts` registers
  `npmCi`, `npmBuild`, `npmLint`, `npmTest`, … as `NpmTask`s). Node is pinned in the version catalog (`node = 24.20.0`),
  downloaded by the plugin, and cached in CI via `gradle-home-cache-includes: nodejs`.
- `dependency-updates.yml` runs `./gradlew dependencyUpdates`, extracts the actionable sections, and opens/updates the
  "Dependency updates" issue. The `toolingUpdates` check (`build.gradle.kts`) tracks the Snyk CLI pin in `snyk.yml`.

## Goals / Non-Goals

**Goals:**

- Report the web UI's outdated npm packages, and fail on its high-severity npm vulnerabilities.
- Local parity with the JVM checks (a Gradle entry point) and one Node version (the catalog's, via the node plugin).
- Surface both through the existing update/security workflows.

**Non-Goals:**

- Wiring Snyk to npm, or adding a dedicated web-UI workflow.
- Any application runtime change, or gating the merge on either check.

## Decisions

- **`npm audit` for the web UI, not Snyk-on-npm.** `npm audit` runs inside the frontend module against the module's own
  npm — no Snyk CLI, account, or policy file — though it, like Snyk, queries a registry-served advisory endpoint and so
  needs network access. Extending Snyk to `showcase-web-ui/package-lock.json` would route the UI through Snyk's npm
  support for a second ecosystem. Rejected: Snyk-on-npm, and doing both.
- **Both checks are `NpmTask`s in `frontend-conventions`**, not workflow-only shell steps. Local parity
  (`./gradlew :showcase-web-ui:npmOutdated` / `:npmAudit`) and the plugin's catalog-pinned Node — the alternative,
  `actions/setup-node` or the runner's preinstalled Node, would put a second Node version in play, which the repo's
  single-sourcing avoids.
- **The update report is npm's own output, written to files by an npm script, and keyed on the exit code, not stderr.**
  A `showcase-web-ui` npm script (`outdated:report`) runs `scripts/outdated-report.sh`, which redirects `npm outdated`'s
  stdout to `build/npm-outdated.txt`, stderr to `build/npm-outdated.err.txt` (diagnostics only), and `$?` to
  `build/npm-outdated.exit`, then exits `0`; the `npmOutdated` task runs that script via `NpmTask`, so a non-zero npm
  exit never fails the task, and in a `doLast` logs the report file — and prints the error file when the exit code is
  non-zero — so a local run shows the outdated packages like the JVM update tasks and a failure is not a silent empty
  run. The script first creates the module's `build/` directory (`mkdir -p build`), since a fresh checkout has none (the
  task declares no outputs under it) and the redirect would otherwise fail before npm runs. The workflow's rule: a
  non-empty report is actionable; an empty report with exit `0` is "no updates"; an empty report with a non-zero exit is
  an npm error that fails the step. stderr cannot be the error signal — npm writes warnings (unknown config,
  deprecations) there on clean runs too, as an empty-stdout, non-empty-stderr, exit-`0` run demonstrates. Rejected: a
  custom task parsing `npm outdated --json` — it needs a JSON parser in `build-logic`, which the update tasks
  deliberately avoid — and `tee`-capturing Gradle's stdout.
- **Extend the existing workflows; rename `snyk.yml` to `dependency-security.yml`.** The workflow's role is dependency
  security — Snyk for the JVM classpaths and `npm audit` for the UI — and the owning `merge-governance` requirement is
  already titled "Dependency security scan …"; the file names one of its two scanners. Rejected: keeping `snyk.yml`
  (misnames the workflow), a display-name-only change (file/name mismatch), and a dedicated web-UI workflow (out of
  scope).
- **`npm audit --audit-level=high`, full tree.** Fail on high/critical findings across production and development
  dependencies (no `--omit=dev`), since dev dependencies are part of the supply chain. Rejected: `--omit=dev` (hides
  devDependency advisories) and npm's default audit level (no `--audit-level`, which npm resolves to `low`, so it fails
  on low-and-above — noisier, and the repo has no npm suppression mechanism yet).
- **Record the choice as an ADR.** The npm-audit-over-Snyk decision is cross-cutting (it matches the precedent of
  ADR-0006, which records the analogous Snyk-scan choice), so it belongs in `docs/adr/`, not only in this archived
  design — a reader asking "why not Snyk for npm?" should find it without the change history.

## Risks / Trade-offs

- **npm's exit code and output are not guaranteed stable** → the pinned Node/npm version keeps the table stable; the
  implementation task runs the task against a known-outdated dependency and reads the output before wiring the workflow.
- **An audit failure with no suppression path could block the check on an unfixable transitive advisory** → start at
  `high`; if a genuine unfixable finding appears, add an ignore mechanism (npm has no `.snyk` analogue) as its own
  change.
- **Renaming the workflow resets its Actions identity and no CI job exercises it** → run
  `gh workflow run dependency-security.yml` once after it lands on `main`; `toolingUpdates`'s exact-match pin on the
  renamed file fails loudly if a reference is missed.
- **The `npmOutdated` task must not fail the workflow when updates exist** (npm exits non-zero) → the task tolerates the
  exit and records it; the workflow reads the report and the recorded exit code, so "updates exist" (non-empty report)
  and a genuine npm error (empty report, non-zero exit) are distinguished.

## Migration Plan

- Apply: add the tasks, extend the two workflows, rename the workflow file, repoint `build.gradle.kts`, update the docs.
- Rollback: revert the change (the checks are additive; nothing consumes their output in the build).
