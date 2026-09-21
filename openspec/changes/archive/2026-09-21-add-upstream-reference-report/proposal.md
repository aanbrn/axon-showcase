# Add an upstream-reference report

## Why

Durable artifacts cite upstream issues we are waiting on, and no gate reads them. Two closures went unnoticed until the
first closure was found by hand (`ben-manes/gradle-versions-plugin#755`,
`spring-projects/spring-data-elasticsearch#3334`), and an audit closure later surfaced a third
(`paketo-buildpacks/nginx` — the pack version). The corpus spans `AGENTS.md`, `README.md`, `docs/adr/`, and
`docs/ideas.md`, and a closure is a _trigger to check_, not an answer: the fixing release may not reach us, and several
references are cited for reasons other than waiting (a counter-example, a documentation reference), so the report
surfaces state for the owner rather than declaring a finding actionable.

## What Changes

- **An `upstream-references` Gradle task** scans `AGENTS.md`, `README.md`, `docs/adr/**`, and `docs/ideas.md` for
  `owner/repo#NNN` references, resolves each through the GitHub API, and writes a report naming every reference with its
  state (open / closed, and the closure date) plus the file and line it is cited at — so a closure is visible where it
  was claimed.
- **An `upstream-references` workflow** runs the task weekly and on `workflow_dispatch`, then opens or updates an
  "Upstream references" issue from the report, mentioning the owner when a reference has closed — the same observational
  pattern as the four update checks, with no merge-gate role.
- **The `merge-governance` spec** gains the requirement for both, and the `.snyk`-style suppression concern does not
  apply here: the corpus is resolved live, so there is no pinned expected state to drift.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/merge-governance` — a new requirement for the upstream-reference report (the capability already owns
  the observational scheduled workflows).

## Impact

- **Gradle**: a new task in the root build (or `build-logic`), reported under `build/upstream-references/report.txt`,
  resolvable by the `toolingUpdates` pattern.
- **CI**: one new observational workflow; no change to the merge gate.
- **Docs**: the `docs/ideas.md` entry is removed (it is implemented by this change).
