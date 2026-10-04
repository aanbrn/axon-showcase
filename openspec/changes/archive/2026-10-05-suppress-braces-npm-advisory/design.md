# Design

## Context

See `proposal.md` — Why. The `npmAudit` task (`build-logic/src/main/kotlin/frontend-conventions.gradle.kts`) currently
runs `npm audit --audit-level=high` and passes npm's exit code straight through, so any high-severity finding — fixable
or not — fails it. The Snyk scan has the `.snyk` policy for the unfixable case; npm has no equivalent and `npm audit`
exposes no ignore flag (its only severity lever is `--audit-level`). ADR-0014 already recorded this as an owed
mechanism. `build-logic` already depends on Jackson (`libs.jackson2.databind`) and parses JSON in `DashboardJsonRules`,
so a parse-and-filter task needs no new dependency.

## Goals / Non-Goals

**Goals:**

- Let an npm advisory with **no available fix** be suppressed explicitly, with a reason and an expiry, so the check is
  green while the finding is knowingly accepted — and red again once the expiry passes.
- Keep the unsuppressed remainder a hard failure: suppression must not weaken the scan for fixable findings.

**Non-Goals:**

- Suppressing fixable advisories, or suppressing by severity threshold rather than by advisory id (would hide unrelated
  findings).
- Excluding dev dependencies (`--omit=dev`) — ADR-0014 already rejected it; dev advisories are part of the supply chain.
- An auto-updating ignore list, or one keyed on package name rather than advisory id (a package can accrue a second,
  fixable advisory).

## Decisions

**Decision: a JSON suppression file consulted by the task, not a wrapper dependency.**
`showcase-web-ui/npm-audit-ignores.json` holds an array of `{ id, reason, expires }`. The task runs `npm audit --json`,
parses the report, and removes findings that trace (directly or transitively) to a listed, unexpired advisory id, then
fails if any high-severity finding remains. Alternatives considered: adopting `better-npm-audit` (a third-party wrapper)
— rejected for adding a dependency to solve something `build-logic`'s existing Jackson and npm's `--json` already cover;
a `.snyk` mirror by package name — rejected because keying on the package would also hide a _future, fixable_ advisory
for that package, while the advisory id pins exactly the accepted one.

**Decision: suppress the advisory's transitive closure.** `npm audit --json` reports each affected package in a
`vulnerabilities` map; a package's `via` entries are either advisory objects (the package itself carries the advisory)
or the **names of the flagged packages it depends on**. Only the leaf `braces` carries the GHSA id; `micromatch`,
`@boundaries/elements`, and `eslint-plugin-boundaries` each name their flagged dependency in `via`, so the chain is
walked **downward** — from a flagged package through its `via` package names until an advisory object is reached. A
package is suppressed when every advisory it reaches this way is listed and unexpired, so the whole closure is removed
and the summary count drops to zero. Alternative: suppressing only the leaf package — rejected; the audit's own summary
counts the dependents, so the task would still fail.

**Decision: an expiry is enforced, not advisory.** Every suppression carries `expires`; a suppression past its date is
treated as absent, so the task fails and the finding re-surfaces for re-assessment. This mirrors `.snyk`'s `expires` and
prevents a suppression becoming permanent by neglect. Alternative: a reason-only list — rejected; it hides the finding
indefinitely, the same "no non-expiring suppression" concern the Snyk policy handles.

**Decision: the task parses the report and decides, ignoring npm's exit code.** `npm audit` exits non-zero whenever any
finding exists, so its exit code cannot distinguish "suppressed" from "unsuppressed". The task takes the JSON body as
data and computes the verdict itself; npm's exit code is not consulted.

## Risks / Trade-offs

- **A mistyped or wrong advisory id silently suppresses nothing, or suppresses the wrong finding.** → The file is
  validated on read: an entry missing `id`/`reason`/`expires`, or whose `id` matches no advisory in the report, fails
  the task naming the entry (fail-closed — a stale entry whose advisory has been fixed or whose id is a typo cannot sit
  silently), and the task reports each applied suppression by id with its expiry so a mistake is visible in the log.
- **The suppression list accumulates permanent entries.** → The enforced expiry re-surfaces each finding; the task
  prints applied suppressions and their dates on every run.
- **Parsing `npm audit --json` couples us to npm's report shape.** → A shape change surfaces as a parse failure (the
  task fails, naming it), not as a silent pass — the parse is a gate input, not a gate, mirroring the
  `DashboardJsonRules` caution that a successful parse is not by itself validation.
- **Upstream fixes `braces`.** → The expiry (short, like `.snyk`'s) re-runs the check soon; the entry is removed when a
  fixed `braces` resolves, and the check passes on its own.

## Open Questions

None.
