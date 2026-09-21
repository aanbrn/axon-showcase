# Tasks — add an upstream-reference report

## 1. The check

- [x] 1.1 `build-logic` — an `UpstreamReferencesTask` that scans the four documents for `owner/repo#NNN`, resolves each
      through `gh api`, and writes a report of `reference<TAB>state<TAB>citations`; the `gh` lookup resolves the tool's
      absolute path from `PATH` (the repo's exec convention) and reports an unresolvable reference rather than failing.
- [x] 1.2 The extraction pattern is an `internal object` (`UpstreamReferencePattern`) with unit tests asserting the
      load-bearing constraints — a reference is extracted, a bare `owner/repo`, a non-numeric issue part, and the
      corpus's id-shaped tokens (`ADR-0002`, `CVE-2021-44228`, `UTF-8`, `ISO-8601`, `KAFKA-18281`) are not.
- [x] 1.3 Register the `upstreamReferences` task in the root build with its source file set, reported under
      `build/upstream-references/report.txt`.

## 2. The workflow

- [x] 2.1 `.github/workflows/upstream-references.yml` — weekly and dispatch triggers, `issues: write`, runs the task and
      opens or updates the "Upstream references" issue, mentioning the owner when a reference closed or did not resolve.
- [x] 2.2 Its schedule is its own slot (`06:00` Mondays): `01`/`02`/`03`/`04`/`05` are taken by the other checks, and a
      shared minute would contend on the runner pool.

## 3. The spec delta

- [x] 3.1 An `ADDED` requirement to `showcase/quality/merge-governance` for the report, with the scheduled, manual,
      all-open, replace-the-notification, cited-for-another-reason, and not-a-gate scenarios.

## 4. Docs

- [ ] 4.0 Record the deferred `merge-governance` `## Purpose` refresh for the archive commit: its parenthetical names
      "the update checks and the repository audits", and this change adds a third observational family (the
      upstream-reference report), so the Purpose needs widening — a delta cannot carry a Purpose.

- [x] 4.1 `docs/ideas.md` — the implemented upstream-reference report idea is removed.
- [x] 4.2 `AGENTS.md` / `README.md` — record the new check in the observational-workflow enumerations (the four update
      checks become five checks) and in the CI section.

## 5. Verification

- [x] 5.1 The extraction's positive control: adding a known reference to a corpus file makes it appear in the report and
      resolve; a bare `owner/repo` does not match; reverting returns the report to its baseline.
- [x] 5.2 The unit tests' negative control: weakening the pattern (dropping the `/` requirement) turns 3 of 6 red, and
      restoring them green — so the tests can fail.
- [x] 5.3 `./gradlew check` on the affected modules and `workflowLint`; `openspec validate --all` with the delta.
- [ ] 5.4 Dispatch `gh workflow run upstream-references.yml` after the merge to exercise the workflow end to end — a
      dispatch verification cannot live as a change-dir task (archived changes are invisible and no gate reads them), so
      it is named in the report and parked in `docs/ideas.md` if not run at the merge.
